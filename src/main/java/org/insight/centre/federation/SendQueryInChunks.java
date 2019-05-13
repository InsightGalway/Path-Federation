package org.insight.centre.federation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.StreamRDFWriter.WriterRegistry;
import org.insight.centre.RDFizePaths.PathRDFizer;
import org.insight.centre.topk.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import insight.dev.flushablemap.PathCache;
import insight.dev.flushablemap.SyncableMap;

import org.apache.jena.shared.uuid.JenaUUID;
import org.infinispan.Cache;
import org.infinispan.multimap.api.embedded.MultimapCache;

public class SendQueryInChunks {


	static Logger _log = LoggerFactory.getLogger(SendQueryInChunks.class);

	String sourceNode, targetNoe;
	static Model mdl;
	int K = 10;
	int MAX_QRY_PATTERN=1;


	Map<String, Set<String>> pathWithDatasets;
	// MultimapCache<Integer, SourceSelection.PathCache> cacheDB;
	SyncableMap<Integer, PathCache> cacheDB;

	public SendQueryInChunks(Model mdl, String sourceNode, String targetNode, Map<String, Set<String>> pathWithDatasets,
			SyncableMap<Integer, PathCache> cacheDB2) {
		this.sourceNode = sourceNode;
		this.targetNoe = targetNode;
		this.pathWithDatasets = pathWithDatasets;
		this.mdl = mdl;
		cacheDB = cacheDB2;
	}

	protected SendQueryInChunks init() {
		return this;
	}

	public String startSourceSelection(Path pList) throws IOException, InterruptedException, ExecutionException {

		ExecutorService pool = Executors.newFixedThreadPool(5);

		List<?> lstOfEnpds = new ArrayList<>(pList.vertSeq);

		// System.err.println(lstOfEnpds);
		int count = 0, firstIter = 0;
		// first dataset from the path list
		String curDataset = lstOfEnpds.get(0).toString();
		// old- String Src = this.sourceNode; // original source
		Set<String> Src = new HashSet<String>();
		Src.add(this.sourceNode);

		String target = "";

		Map<String, List<PathMerger>> map = new LinkedHashMap<>();

		// sublist start from 1 index as 0 index used to get first dataset
		List<?> tempList = lstOfEnpds.subList(1, lstOfEnpds.size());

		Set<String> targets = null;
		while (count <= tempList.size()) {

			boolean addPath = true;
			if (count != tempList.size()) {

				String nextDataset = tempList.get(count).toString();
				// get the nodes where two datasets are connected through
				ResultSet connectThrougRs = null;

				QueryExecution exec = mdlEndpConnectedVia(curDataset, nextDataset);
				connectThrougRs = exec.execSelect();
				Set<String> connThr = new HashSet<String>();
				long start = System.currentTimeMillis();
				// System.out.println("iter starts");
				while (connectThrougRs.hasNext()) {
					connThr.add(connectThrougRs.next().get("?o").toString());
				}

				exec.close();
				long end = System.currentTimeMillis();

				long tot = end - start;
				// _log.info("iteratin time for resultset is {} ", tot);
				List<String> pathRetrieved = new ArrayList<>();
				targets = new HashSet<>();

				Set<String> afterFilter = null;
				afterFilter = checkIfExist(Src, nextDataset);

				if (!afterFilter.isEmpty()) {
					Src = afterFilter;
				}

				Set<String> nodeWithChilds = null;
				nodeWithChilds = ifNodeHasChild(Src, curDataset);

				Src = nodeWithChilds;

				// --------------

				Set<String> connThrWithParant = ifNodeHasParant(connThr, curDataset);
				// _log.info("nodes with parants size is {} ",
				// connThrWithParant.size());
				Set<String> conn = null;

				if (!connThrWithParant.isEmpty()) {
					conn = connThrWithParant;
				} else {
					conn = connThr;
				}

				// chunks by chunks of 10

				List<String> srcList = new ArrayList<>(Src);
				List<String> tgtList = new ArrayList<>(conn);
				
				for (int i = 0; i < srcList.size(); i += MAX_QRY_PATTERN) {

					for(int j = 0;j < tgtList.size();j += MAX_QRY_PATTERN){
					List<String> subSrc = srcList.subList(i, Math.min(srcList.size(), i + MAX_QRY_PATTERN));

					List<String> subTgt = tgtList.subList(j, Math.min(tgtList.size(), j + MAX_QRY_PATTERN));
					List<String> qryGenTgtList = new ArrayList<>();

					for (String subSrcStr : subSrc) {

						List<String> qryGenSrcList = new ArrayList<>();
						qryGenSrcList.add(subSrcStr); /// same source for
														/// multiple targets
														/// i.e., one to many
						for (String subTgtStr : subTgt) {

							targets.add(subTgtStr);
							int curDatasetHash = curDataset.hashCode();
							int srcHash = subSrcStr.hashCode();
							int targetHash = subTgtStr.hashCode();

							if (subTgtStr.equals(targetNoe)) {
								List<String> lst = new RmoteQuery(cacheDB).FederateRequest(curDataset, subSrcStr,
										subTgtStr, K);
								if (!lst.isEmpty())
									pathRetrieved.addAll(lst);
							}

							//if (checkInCache(curDatasetHash, srcHash, targetHash) == true)
								//continue;
							if (subSrcStr.equals(subTgtStr))
								continue;

							qryGenTgtList.add(subTgtStr);
						}

						if (!qryGenTgtList.isEmpty()) {
							Future<List<String>> future = pool
									.submit(new PathRequest(curDataset, qryGenSrcList, qryGenTgtList, K));

							List<String> lst = future.get();
							if (!lst.isEmpty())
								pathRetrieved.addAll(lst);
						
						}

					}

				}
				
			}//outerloop for src chunks
				if (!pathRetrieved.isEmpty()) {
					List<PathMerger> lst = new ArrayList<>();
					LinkedList<String> set = new LinkedList<>(map.keySet());
					lst.add(new PathMerger(curDataset, set, pathRetrieved, targets));

					if (map.containsKey(curDataset)) {

						map.get(curDataset).addAll(lst);
					} else {
						map.put(curDataset, lst);
					}
				}
				curDataset = nextDataset;
				// Src = !targets.isEmpty()?targets:Src;
				Src = connThr;

			} else {

				List<String> pathRetrieved = new ArrayList<>();
				target = this.targetNoe; // original target
				// _log.info("source size is {} in last dataset", Src.size());

				Set<String> nodeWithChilds = null;
				nodeWithChilds = ifNodeHasChild(Src, curDataset);
				Src = nodeWithChilds;

				// chunks by chunks of 10

				List<String> srcList = new ArrayList<>(Src);
				List<String> qryGenSrcList = new ArrayList<>();
				List<String> qryGenTrgtList = new ArrayList<>();
				qryGenTrgtList.add(target);// here target will always be same
											// i.e, many to one

				for (int i = 0; i < srcList.size(); i += MAX_QRY_PATTERN) {
					List<String> sub = srcList.subList(i, Math.min(srcList.size(), i + MAX_QRY_PATTERN));

					for (String sublis : sub) {
						int curDatasetHash = curDataset.hashCode();
						int srcHash = sublis.hashCode();
						int targetHash = target.hashCode();

						//if (checkInCache(curDatasetHash, srcHash, targetHash) == true)
							//continue;
						if (sublis.equals(target))
							continue;

						qryGenSrcList.add(sublis);

					}
					if(!qryGenSrcList.isEmpty()){
					Future<List<String>> future1 = pool
							.submit(new PathRequest(curDataset, qryGenSrcList, qryGenTrgtList, K));

					List<String> lst = future1.get();
					if (!lst.isEmpty())
						pathRetrieved.addAll(lst);
					}
				}
				// _log.info("cache size is after last dataset in current
				// datasets paths: ==> {} ",cacheDB.size());
				if (!pathRetrieved.isEmpty()) {
					List<PathMerger> lst = new ArrayList<>();
					LinkedList<String> set = new LinkedList<>(map.keySet());
					lst.add(new PathMerger(curDataset, set, pathRetrieved, targets));

					if (map.containsKey(curDataset)) {

						map.get(curDataset).addAll(lst);
					} else {
						map.put(curDataset, lst);
					}
				}
				// } // end of for loop
			}

			count++;
		}
		System.out.println("cache size after one complete path iteration: ==> " + cacheDB.size());
		buildPath(map);
		pool.shutdown();
		return null;

	}

	boolean ifExisted = false;

	/*
	 * private boolean avoidDuplicatesInCache(String key, PathCache value){
	 * 
	 * ifExisted=false; if (cacheDB.containsKey(key) != null) {
	 * 
	 * cacheDB.get(key).thenAccept(values-> { System.err.println("for key "+
	 * values.size()); if(values.contains(value.src) &&
	 * values.contains(value.target) && values.contains(value.passOrFail)){
	 * System.err.println("already there"); System.out.println(value);
	 * ifExisted= true;
	 * 
	 * }else{
	 * System.out.println("new dataset is: "+key+"  "+value.src+"--"+value.
	 * target+"--"+value.passOrFail); cacheDB.put(key, value);
	 * 
	 * } }); }
	 * 
	 * return ifExisted; }
	 */
	private boolean checkInCache(int Key, int src, int target) {
		// System.err.println("dataset is: "+Key+" "+src+"--"+target);
		boolean boolVal = false;

		// if (cacheDB.containsKey(Key) != null) {
		long start = System.currentTimeMillis();
		// CompletableFuture<Collection<PathCache>> datasetAsKey =
		// cacheDB.get(Key);
		List<PathCache> datasetAsKey = cacheDB.getValue(Key);
		long end = System.currentTimeMillis();
		// _log.info("loop time {}", end-start);
		// System.out.println("cache get get time :"+ (end-start));
		// Collection<PathCache> existingDataset;
		try {
			// existingDataset = datasetAsKey.get();

			if (datasetAsKey != null)
				for (PathCache cach : datasetAsKey) {

					 //System.out.println("dataset is: "+Key+" "+cach.getSrc()+"--"+cach.getTarget()+"--"+cach.isPassOrFail());
					if (cach.getSrc() == src && cach.getTarget() == target && cach.isPassOrFail() == false) {
						boolVal = true;
						// System.err.println("true");
						break;
					}
				}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * } else {
		 * 
		 * boolVal= false; }
		 */

		return boolVal;
	}

	/**
	 * make the federated request
	 * 
	 * @param curDataset
	 * @param src
	 * @param target
	 * @param k
	 *            path
	 */
	protected List<String> getPaths(String curDataset, List<String> src, List<String> target, int k) {
		return new RmoteQuery(cacheDB).FederateRequestWithChunks(curDataset, src, target, k);
	}

	protected Set<String> ifNodeHasChild(Set<String> nodeSet, String endpoint) {

		Set<String> filteredNodes = new HashSet<>();

		ExecutorService ex = Executors.newFixedThreadPool(20);
		for (String node : nodeSet) {
			ex.submit(new Runnable() {
				@Override
				public void run() {
					String qrySelect = "Select * {<%subj> ?p ?o } limit 1";
					try {
						QueryExecution exec = null;
						Query qry = null;

						String ASK = qrySelect.replace("%subj", node);
						// System.err.println("endp: "+ targetEndp.endpName
						// +"query: = "+ASK);
						qry = QueryFactory.create(ASK);
						exec = QueryExecutionFactory.sparqlService(endpoint, qry);
						if (exec.execSelect().hasNext()) {

							filteredNodes.add(node);

						}
						exec.close();

					} catch (QueryParseException e) {
						_log.error(e.getMessage());

					}

				}
			});
		}
		ex.shutdown();
		try {
			ex.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		return filteredNodes;

	}

	protected Set<String> ifNodeHasParant(Set<String> nodeSet, String endpoint) {

		Set<String> filteredNodes = new HashSet<>();

		ExecutorService ex = Executors.newFixedThreadPool(50);
		for (String node : nodeSet) {
			ex.submit(new Runnable() {
				@Override
				public void run() {
					String qrySelect = "Select * {?s ?p <%obj> } limit 1";
					try {
						QueryExecution exec = null;
						Query qry = null;

						String ASK = qrySelect.replace("%obj", node);
						// System.err.println("endp: "+ targetEndp.endpName
						// +"query: = "+ASK);
						qry = QueryFactory.create(ASK);
						exec = QueryExecutionFactory.sparqlService(endpoint, qry);
						if (exec.execSelect().hasNext()) {

							filteredNodes.add(node);

						}
						if (node.equals(targetNoe) || node.equals(sourceNode))
							filteredNodes.add(node);
						exec.close();

					} catch (QueryParseException e) {
						_log.error(e.getMessage());

					}

				}
			});
		}
		ex.shutdown();
		try {
			ex.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}

		return filteredNodes;

	}

	protected Set<String> checkIfExist(Set<String> src, String endpoint) {
		

		Set<String> filteredNodes = new HashSet<>();

		ExecutorService ex = Executors.newFixedThreadPool(1);
		for (String subObjSource : src) {
			ex.submit(new Runnable() {
				@Override
				public void run() {
					String qryASK1 = "ASK WHERE{?s ?p ?o " + "Filter(?s = <%subj> || ?o= <%obj> ) " + "}";

					String qryASK = "Select * WHERE{?s ?p ?o " + "Filter(?s = <%subj> || ?o= <%obj> ) " + "} limit 1";

					try {
						QueryExecution exec = null;
						Query qry = null;

						String ASK = qryASK.replace("%subj", subObjSource).replace("%obj", subObjSource);
						// System.err.println("endp: "+ targetEndp.endpName
						// +"query: = "+ASK);
						qry = QueryFactory.create(ASK);
						exec = QueryExecutionFactory.sparqlService(endpoint, qry);
						if (exec.execSelect().hasNext()) {

							filteredNodes.add(subObjSource);

						}
						exec.close();

					} catch (QueryParseException e) {
						_log.error(e.getMessage());

					}

				}
			});
		}
		ex.shutdown();
		try {
			ex.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
		return filteredNodes;
	}

	protected Set<String> nodesWithProp(ResultSet rest, String curDataset) {

		Set<String> nodeWithProp = new HashSet<>();
		while (rest.hasNext()) {

			String nodeCheck = rest.next().get("?o").toString();

			String qry = "Select * {<%subj> ?p ?o } limit 1".replace("%subj", nodeCheck);
			QueryExecution qryExec = QueryExecutionFactory.sparqlService(curDataset, qry);
			if (qryExec.execSelect().hasNext()) {
				nodeWithProp.add(nodeCheck);
			} else {
				System.err.println("no further properties...");
			}

		}
		return nodeWithProp;

	}

	/**
	 * @param curDataset
	 * @param nextDataset
	 * @return this method returns the nodes through which two datasets are
	 *         interlinked in index file
	 */
	protected static QueryExecution mdlEndpConnectedVia(String curDataset, String nextDataset) {

		// Model modelTemp = ModelFactory.createDefaultModel();
		// InputStream in = FileManager.get().open("data/index-2.nt");

		// modelTemp.read( in , null, "N-TRIPLE");

		Query query = null;
		QueryExecution qryExec = null;
		ResultSet rs = null;
		// String queryStr= "prefix feds:
		// <http://vocab.org.centre.insight/feds#> Select distinct ?o where{
		// <"+curDataset+"> feds:connectedTo <"+nextDataset+">.
		// <"+nextDataset+"> feds:connectedThrough ?o}";

		String queryStr = "prefix feds: <http://vocab.org.centre.insight/feds#> SELECT distinct ?o WHERE {  ?o feds:isCommonIn <"
				+ curDataset + ">; feds:isCommonIn <" + nextDataset + ">}";
		query = QueryFactory.create(queryStr);
		qryExec = QueryExecutionFactory.create(query, mdl);
		// rs= qryExec.execSelect();
		// qryExec.close();
		// qryExec.close();
		return qryExec;

	}

	private void buildPath(Map<String, List<PathMerger>> map) throws IOException {

		/* datasets involved in the contribution of a complete path */

		Map<String, String> pPaths = new HashMap<>();
		Set<String> dataset = new HashSet<>();

		JenaUUID uuid = JenaUUID.generate();

		for (Entry<String, List<PathMerger>> build : map.entrySet()) {

			List<PathMerger> mergDataset = build.getValue();

			for (PathMerger pathBuild : mergDataset) {

				if (pathBuild.prvSet.isEmpty()) {

					for (String path : pathBuild.pathRetrieved) {
						if (path.startsWith(this.sourceNode) && path.endsWith(this.targetNoe)) {

							Set<String> dataset1 = new HashSet<>();
							dataset.add(pathBuild.curDataset);

							if (pathWithDatasets.containsKey(path) && pathWithDatasets.containsValue(dataset1)) {

								// System.out.println("dataset already existed
								// against this path");
							} else {

								pathWithDatasets.put(path, dataset1);
								savePaths(path);
								// System.out.println(path );
								Map<String, String> fPaths = new HashMap<>();
								fPaths.put(pathBuild.curDataset, path);

								PathRDFizer.RDFizeAndSave(uuid, path, fPaths, pathFirstNode(path), pathLastNode(path),
										true);

							}

							/*
							 * if(!curPathLst1stDataset.contains(path)){
							 * curPathLst1stDataset.add(path);
							 * System.out.println("path from : "+pathBuild.
							 * curDataset+"="+path); savePaths(path);
							 * 
							 * Map<String, String> fPaths= new HashMap<>();
							 * fPaths.put(pathBuild.curDataset, path);
							 * 
							 * PathRDFizer.RDFizeAndSave(uuid, path, fPaths,
							 * pathFirstNode(path), pathLastNode(path), true); }
							 */

						}
					}
				}

				// if(!pathBuild.pathRetrieved.isEmpty()){
				if (!pathBuild.prvSet.isEmpty()) {

					while (!pathBuild.prvSet.isEmpty()) {
						String prevDataset = pathBuild.prvSet.removeLast();

						ListIterator<String> iter = pathBuild.pathRetrieved.listIterator();
						while (iter.hasNext()) {
							String currentPath = iter.next();

							if (currentPath.startsWith(this.sourceNode) && currentPath.endsWith(this.targetNoe)) {

								Set<String> dataset1 = new HashSet<>();
								dataset.add(pathBuild.curDataset);

								if (pathWithDatasets.containsKey(currentPath)
										&& pathWithDatasets.containsValue(dataset1)) {

									// System.out.println("dataset already
									// existed against this path");
								} else {

									pathWithDatasets.put(currentPath, dataset1);
									// System.out.println(currentPath );
									savePaths(currentPath);
									Map<String, String> fPaths = new HashMap<>();
									fPaths.put(pathBuild.curDataset, currentPath);

									PathRDFizer.RDFizeAndSave(uuid, currentPath, fPaths, pathFirstNode(currentPath),
											pathLastNode(currentPath), true);

								}

								/*
								 * if(!curPathLst2ndDataset.contains(currentPath
								 * )){
								 * System.err.println("path from : "+pathBuild.
								 * curDataset+"="+currentPath);
								 * curPathLst2ndDataset.add(currentPath);
								 * savePaths(currentPath); Map<String, String>
								 * fPaths= new HashMap<>();
								 * fPaths.put(pathBuild.curDataset,
								 * currentPath);
								 * 
								 * PathRDFizer.RDFizeAndSave(uuid,currentPath,
								 * fPaths, pathFirstNode(currentPath),
								 * pathLastNode(currentPath),true); }
								 */

							}

							List<PathMerger> keyV = map.get(prevDataset);

							for (Iterator<PathMerger> iterPMerger = keyV.iterator(); iterPMerger.hasNext();) {

								PathMerger pathMerger = iterPMerger.next();
								List<String> prevPLst = new CopyOnWriteArrayList<>(pathMerger.pathRetrieved);
								// System.err.println("size of temlist is: "+
								// prevPLst.size());
								// for(ListIterator<String>
								// prevPIter=tempLst.listIterator();prevPIter.hasNext();){

								// String previousPath= prevPIter.next();
								// }

								for (String previousPath : prevPLst) {

									if (!currentPath.endsWith(this.targetNoe) && !previousPath.endsWith(this.targetNoe)
											&& currentPath.startsWith(pathLastNode(previousPath))) {

										String concate = previousPath.concat("----").concat(currentPath);
										// System.out.println(concate);
										// System.err.println(pathBuild.pathRetrieved.indexOf(currentPath));
										// System.out.println(pathBuild.pathRetrieved.get(pathBuild.pathRetrieved.indexOf(currentPath)));

										if (pathBuild.pathRetrieved.contains(currentPath)) {
											pathBuild.pathRetrieved.set(pathBuild.pathRetrieved.indexOf(currentPath),
													concate);
										} else {

											iter.add(concate);
										}
										pPaths.put(prevDataset, previousPath);
										pPaths.put(pathBuild.curDataset, currentPath);

										dataset.add(prevDataset);
										dataset.add(pathBuild.curDataset);

										pathWithDatasets.put(currentPath, dataset);

										// PathRDFizer.RDFizeAndSave(uuid,concate,
										// pPaths,pathFirstNode(concate),
										// pathLastNode(concate),false);

									} else if (previousPath.startsWith(this.sourceNode)
											&& !previousPath.endsWith(this.targetNoe)
											&& currentPath.startsWith(pathLastNode(previousPath))
											&& currentPath.endsWith(this.targetNoe)) {
										String p = "";
										p = previousPath.concat("----").concat(currentPath);

										// Set<String> dataset= new HashSet<>();
										dataset.add(pathBuild.curDataset);

										// System.err.println(pathWithDatasets.containsKey(p));

										if (pathWithDatasets.containsKey(p)
												&& pathWithDatasets.containsValue(dataset)) {

											if (pathWithDatasets.values().containsAll(dataset)) {
												// System.err.println("path and
												// dataset are equal");
											}

											// System.out.println("dataset
											// already existed against this
											// path");
										} else {

											pathWithDatasets.put(p, dataset);

											// System.out.println(p);
											savePaths(p.toString());
											// pPaths.put(prevDataset,
											// previousPath);
											pPaths.put(pathBuild.curDataset, currentPath);

											PathRDFizer.RDFizeAndSave(uuid, p, pPaths, pathFirstNode(p),
													pathLastNode(p), true);

										}
									} else if (!previousPath.startsWith(this.sourceNode)
											&& !currentPath.endsWith(this.targetNoe)
											&& previousPath.startsWith(pathLastNode(currentPath))) {

										String p = "";

										p = currentPath.concat("----").concat(previousPath);

										if (p.startsWith(this.sourceNode) && p.endsWith(this.targetNoe)) {
											// System.out.println(p );
											pPaths.put(pathBuild.curDataset, currentPath);
											PathRDFizer.RDFizeAndSave(uuid, p, pPaths, pathFirstNode(p),
													pathLastNode(p), true);
										}
									}

								}

							} // iteratorPMerger
						}

					}
				}
				// }

			}
		}

	} // end of buildPath Function

	private String getSingleHopPath(String endp, String S, String O) {

		String qryStr = "SELECT * { ?s ?p ?o. FILTER (?o=<" + O + "> || ?s=<" + O + "> )}";

		Query qry = QueryFactory.create(qryStr);

		QueryExecution exec = QueryExecutionFactory.sparqlService(endp, qry);
		String spo = null;

		ResultSet res = exec.execSelect();

		while (res.hasNext()) {
			QuerySolution sol = res.next();

			spo = sol.get("?s").toString().concat(sol.get("?p").toString()).concat(sol.get("?o").toString());
		}

		return spo;
	}

	private String pathLastNode(String path) {

		return path.substring(path.lastIndexOf(">-") + 2);
	}

	private String pathFirstNode(String path) {
		return path.split("-<")[0];

	}

	public void savePaths(String paths) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter("paths.txt", true));
		writer.write(paths);
		writer.write("\n");
		writer.close();
	}

	class PathMerger {

		LinkedList<String> prvSet = null;
		String curDataset = "";
		List<String> pathRetrieved = null;
		Set<String> connectedVia;

		public PathMerger(String curDataset, LinkedList<String> prvSet, List<String> pathRetrieved,
				Set<String> targets) {
			this.prvSet = prvSet;
			this.curDataset = curDataset;
			this.pathRetrieved = pathRetrieved;
			this.connectedVia = targets;
		}

	}

	/**
	 * callable
	 */

	class PathRequest implements Callable<List<String>> {

		String curDataset = "";
		List<String> src;
		List<String> target;
		int topK = 0;

		public PathRequest(String curDataset, List<String> src, List<String> target, int i) {
			this.curDataset = curDataset;
			this.src = src;
			this.target = target;
			this.topK = i;

		}

		@Override
		public List<String> call() throws Exception {

			return getPaths(this.curDataset, this.src, this.target, this.topK);

		}

	}

	// public static class PathCache implements Serializable{
	//
	//
	// /**
	// *
	// */
	// private static final long serialVersionUID = 8011637879899586076L;
	//
	// int src, target;
	// boolean passOrFail;
	//
	// public PathCache(int src, int target, boolean bool) {
	// this.src=src; this.target=target;this.passOrFail=bool;
	// }
	//
	// }

}
