import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.insight.centre.RDFizePaths.PathRDFizer;

public class SourceSelection {

    String sourceNode, targetNoe;
    static Model mdl;
    

    public SourceSelection(Model mdl,String sourceNode, String targetNode) {
        this.sourceNode = sourceNode;
        this.targetNoe = targetNode;
        this.mdl=mdl;

    }

    protected SourceSelection init() {
        return this;
    }

    protected String startSourceSelection(Path pList) throws IOException, InterruptedException, ExecutionException {


    	ExecutorService pool= Executors.newFixedThreadPool(50);
    	
        List <?> lstOfEnpds = new ArrayList < > (pList.vertSeq);
        int count = 0;
        // first dataset from the path list
        String curDataset = lstOfEnpds.get(0).toString();
        //old- String Src = this.sourceNode; // original source
        List<String> Src= new ArrayList<String>();
        Src.add(this.sourceNode);
        
        String target = "";

        Map < String, List < PathMerger >> map = new LinkedHashMap < > ();

        // sublist start from 1 index as 0 index used to get first dataset
        List <?> tempList = lstOfEnpds.subList(1, lstOfEnpds.size());

        List <String> targets=null; 
        while (count <= tempList.size()) {

        	 boolean addPath=true;
            if (count != tempList.size()) {

                String nextDataset = tempList.get(count).toString();
                // get the nodes where two datasets are connected through
              //  Iterator<String> connectThrougRs = nodesWithProp(mdlEndpConnectedVia(curDataset, nextDataset), curDataset).iterator();
                ResultSet connectThrougRs= mdlEndpConnectedVia(curDataset, nextDataset);
                Set<String> connThr= new HashSet<String>();
               while(connectThrougRs.hasNext()){
            	   connThr.add(connectThrougRs.next().get("?o").toString());
               }
                
                List < String > pathRetrieved = new ArrayList < > ();
                targets= new ArrayList<>();
               
               int conThSize=1;
               for(String src: Src){
            	  // System.out.println(src);
            	   //check if source does exist in the current dataset
            	   // if doesnt don need to make any request since no benefit of connectedThrough
          if(checkIfExist (src, curDataset)){	   
            	//while(connectThrougRs.hasNext()){
            	for (String common: connThr) {
                    target = common;;
                	//target=connectThrougRs.next().get("?o").toString();
                    targets.add(target); // add each target to a targets List
                    
                    // get the path either from local or remote endpoints
                    //System.out.println(curDataset+conThSize++);
                  // System.err.println("endpoint:="+curDataset+" source: "+ src+" targer: "+target);
                  if(target.equals(targetNoe)){
                    if(src.equals(sourceNode)){
                    	Future<List<String>> future1= pool.submit(new PathRequest(curDataset, src, target, 2));
                    	
						pathRetrieved.addAll(future1.get());
                    }
                    addPath=false;
                   continue;
                   }else{
                   // pathRetrieved.addAll(getPaths(curDataset, Src, target, 2));
                    try {
                    	Future<List<String>> future= pool.submit(new PathRequest(curDataset, src, target, 2));
                    	
						pathRetrieved.addAll(future.get());
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                   }
                } // end of connectedThrough while loop 
              } else{continue;}
            } // end of Src loop
               // System.out.println("size: "+ conThSize);
               // if (!map.containsKey(curDataset)) {
                    List < PathMerger > lst = new ArrayList < > ();
                    LinkedList < String > set = new LinkedList < > (map.keySet());
                    lst.add(new PathMerger(curDataset, set, pathRetrieved, targets));
                    map.put(curDataset, lst);

                //}
                curDataset = nextDataset;
                //Src = !targets.isEmpty()?targets:Src;
                Src = targets;
            } else {

            	List < String > pathRetrieved = new ArrayList < > ();
                target = this.targetNoe; // original target
               for(String src: Src) { 
  
                List < String > pathRet = getPaths(curDataset, src, target, 2);
                pathRetrieved.addAll(pathRet);

               }
        	
                Future<List<String>> future2= pool.submit(new PathRequest(curDataset, this.sourceNode, this.targetNoe, 2));
                   	
				pathRetrieved.addAll(future2.get());
                  
                  
        	   
                //if (!map.containsKey(curDataset)) {
                    List < PathMerger > lst = new ArrayList < > ();
                    LinkedList < String > set = new LinkedList < > (map.keySet());
                    lst.add(new PathMerger(curDataset, set, pathRetrieved, targets));

                    map.put(curDataset, lst);
                //}
            //} // end of for loop 
            }

            count++;
        }

        buildPath(map);
pool.shutdown();
        return null;

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
    protected List < String > getPaths(String curDataset, String src, String target, int k) {
        return new RmoteQuery().FederateRequest(curDataset, src, target, k);
    }

    protected boolean checkIfExist(String node, String endpoint){
    /*	String qryASK = "ASK WHERE{?s ?p ?o "
    			+ "Filter(?s = <%subj> || ?o= <%obj> ) "
    			+ "}";
    	qryASK=qryASK.replace("%subj", node).replace("%obj", node);*/
     	
    	//Query query = QueryFactory.create(qryASK);
    	
    	Set<String> nodeWithProp= new HashSet<>();
    	
    	String qry = "Select * {<%subj> ?p ?o } limit 1".replace("%subj", node);
        
    	
    	QueryExecution exec= QueryExecutionFactory.sparqlService(endpoint, qry);
    	
    	if(exec.execSelect().hasNext())
    	{
    		return true;
    	}else{
    	 
    	return false; 
    	}
    	
    }
    
    
    protected Set<String> nodesWithProp(ResultSet rest, String curDataset){
	
    Set<String> nodeWithProp= new HashSet<>();
    while(rest.hasNext()){	
    	
    	String nodeCheck= rest.next().get("?o").toString();
    	
    	String qry = "Select * {<%subj> ?p ?o } limit 1".replace("%subj",nodeCheck );
    	 QueryExecution qryExec = QueryExecutionFactory.sparqlService(curDataset,qry);
    	 if(qryExec.execSelect().hasNext()){
    		 nodeWithProp.add(nodeCheck);
    	 }else{
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
    protected static ResultSet mdlEndpConnectedVia(String curDataset, String nextDataset) {

      //  Model modelTemp = ModelFactory.createDefaultModel();
      //  InputStream in = FileManager.get().open("data/index-2.nt");

     //   modelTemp.read( in , null, "N-TRIPLE");

    	 String query = "prefix feds: <http://vocab.org.centre.insight/feds#> SELECT distinct ?o WHERE { { <"+curDataset+"> feds:connectedThrough ?o. <"+nextDataset+"> feds:connectedThrough ?o.} UNION {<"+nextDataset+">feds:connectedThrough ?o. <"+curDataset+"> feds:connectedThrough ?o.} }";

        QueryExecution qryExec = QueryExecutionFactory.create(query, mdl);
        

        return qryExec.execSelect();

    }

    private void buildPath(Map < String, List < PathMerger >> map) throws IOException {

    	/* datasets involved in the contribution of a complete path*/
    	List<String> dtsContributed= new ArrayList<>();
    	
    	
        for (Entry < String, List < PathMerger >> build: map.entrySet()) {

            List < PathMerger > mergDataset = build.getValue();

            for (PathMerger pathBuild: mergDataset) {

                if (pathBuild.prvSet.isEmpty()) {

                    for (String path: pathBuild.pathRetrieved) {
                        if (path.endsWith(this.targetNoe)){
                            System.out.println("path from : "+pathBuild.curDataset+"="+path);
                            savePaths(path);
                           
                            Map<String, String> fPaths= new HashMap<>();
                            fPaths.put(pathBuild.curDataset, path);
                            
                        PathRDFizer.RDFizeAndSave(path, fPaths, pathFirstNode(path), pathLastNode(path), true);
                       
                        
                        }
                    }
                }

              //  if(!pathBuild.pathRetrieved.isEmpty()){
                if (!pathBuild.prvSet.isEmpty()) {

                    while (!pathBuild.prvSet.isEmpty()) {
                        String prevDataset = pathBuild.prvSet.removeLast();
                        int i = 0;

                        Iterator < String > iter = pathBuild.pathRetrieved.iterator();
                        while (iter.hasNext()) {
                            String currentPath = iter.next();

                            if(currentPath.startsWith(this.sourceNode) && currentPath.endsWith(this.targetNoe))
                            	{
                            	//System.out.println("single hope from: "+pathBuild.curDataset+" path:="+ currentPath);
                                  savePaths(currentPath);
                                
                                  Map<String, String> fPaths= new HashMap<>();
                                  fPaths.put(pathBuild.curDataset, currentPath);
                                  
                                  PathRDFizer.RDFizeAndSave(currentPath, fPaths, pathFirstNode(currentPath), pathLastNode(currentPath),true);
                                  
                            	}
                            
                            for (String previousPath: map.get(prevDataset).get(i).pathRetrieved) { 
                                if (!currentPath.endsWith(this.targetNoe) && !previousPath.endsWith(this.targetNoe) && currentPath.startsWith(pathLastNode(previousPath))) {
                                
                                	Map<String, String> pPaths= new HashMap<>();
                                	
                                	String concate= previousPath.concat("----").concat(currentPath);
                                	//System.out.println(concate);
                                	//System.err.println(pathBuild.pathRetrieved.indexOf(currentPath));
                                	//System.out.println(pathBuild.pathRetrieved.get(pathBuild.pathRetrieved.indexOf(currentPath)));
                                
                                	pathBuild.pathRetrieved.set(pathBuild.pathRetrieved.indexOf(currentPath), concate);
                                
                                	pPaths.put(prevDataset, previousPath);
                                	pPaths.put(pathBuild.curDataset, currentPath);
                             
                                	PathRDFizer.RDFizeAndSave(concate, pPaths,pathFirstNode(concate), pathLastNode(concate),false);
                        
                                } else {
                                	
                                  
									String p="";
									if (previousPath.startsWith(this.sourceNode) &&!previousPath.endsWith(this.targetNoe) && currentPath.startsWith(pathLastNode(previousPath)) && currentPath.endsWith(this.targetNoe))
									{p =previousPath.concat("----").concat(currentPath);
                                    
									Map<String, String> pPaths= new HashMap<>();
									
										System.out.println(p);
										savePaths(p.toString());
										pPaths.put(prevDataset, previousPath);
										pPaths.put(pathBuild.curDataset, currentPath);
										
										
										PathRDFizer.RDFizeAndSave(p, pPaths, pathFirstNode(p), pathLastNode(p),true);
									
										
									}
                                }

                            }

                        }
                        i++;

                    }
                }
           // }

            }
        }

    } // end of buildPath Function

    private String getSingleHopPath(String endp, String S, String O){
    	
    	String qryStr= "SELECT * { ?s ?p ?o. FILTER (?o=<"+O+"> || ?s=<"+O+"> )}";
    	
    	Query qry= QueryFactory.create(qryStr);
    	
    	QueryExecution exec= QueryExecutionFactory.sparqlService(endp, qry);
    	 String spo = null;
    	 
    	 ResultSet res= exec.execSelect();
    	 
    	while(res.hasNext()){
    		 QuerySolution sol = res.next();
    		 
    		spo= sol.get("?s").toString().concat(sol.get("?p").toString()).concat(sol.get("?o").toString());
    	}
    	
    	return spo;
    }
    
    
    private String pathLastNode(String path){
    	
    	return path.substring(path.lastIndexOf(">-") + 2);
    }
    
    private String pathFirstNode(String path){
    	return path.split("-<")[0];
    	
    }
    public void savePaths(String paths) throws IOException
    {
                 
        BufferedWriter writer = new BufferedWriter(new FileWriter("paths.txt",true));
        writer.write(paths);
        writer.write("\n");
        writer.close();
    }
    
    
    class PathMerger {

        LinkedList < String > prvSet = null;
        String curDataset = "";
        List < String > pathRetrieved = null;
        List < String > connectedVia;

        public PathMerger(String curDataset, LinkedList < String > prvSet, List < String > pathRetrieved,List < String > connectedVia) {
            this.prvSet = prvSet;
            this.curDataset = curDataset;
            this.pathRetrieved = pathRetrieved;
            this.connectedVia = connectedVia;
        }

    }
    
    /**
     * callable
     */
    
    class PathRequest implements Callable<List<String>> {

    	String curDataset="", src="",target="";
    	int topK=0;
    
    	
		public PathRequest(String curDataset, String src, String target, int i) {
			this.curDataset=curDataset;
			this.src=src;
			this.target=target;
			this.topK=i;
		}


		@Override
		public List<String> call() throws Exception {
			Thread.sleep(5);
			return getPaths(this.curDataset, this.src, this.target,this.topK);
		}

    	
    }

}