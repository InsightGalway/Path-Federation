import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;



public class NewApproach {

	
	 String sourceNode, targetNoe;
	    static Model mdl;
	    

	    public NewApproach(Model mdl,String sourceNode, String targetNode) {
	        this.sourceNode = sourceNode;
	        this.targetNoe = targetNode;
	        this.mdl=mdl;

	    }

	    protected NewApproach init() {
	        return this;
	    }

	    protected String startSourceSelection(Path pList) {


	    	ExecutorService pool= Executors.newFixedThreadPool(10);
	    	
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
	                ResultSet connectThrougRs = mdlEndpConnectedVia(curDataset, nextDataset);
	                List < String > pathRetrieved = new ArrayList < > ();
	                targets= new ArrayList<>();
	               
	               int conThSize=1;
	               for(String src: Src){
	                while (connectThrougRs.hasNext()) {
	                    target = connectThrougRs.next().get("?o").toString();
	                    
	                    targets.add(target); // add each target to a targets List
	                    
	                    // get the path either from local or remote endpoints
	                    System.out.println(conThSize++);
	                  if(target.equals(targetNoe)){
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
	                } // end of Src
	            }
	               // System.out.println("size: "+ conThSize);
	               // if (!map.containsKey(curDataset)) {
	                    List < PathMerger > lst = new ArrayList < > ();
	                    LinkedList < String > set = new LinkedList < > (map.keySet());
	                    lst.add(new PathMerger(curDataset, set, pathRetrieved, targets));
	                    map.put(curDataset, lst);

	                //}
	                curDataset = nextDataset;
	                Src = targets;
	            } else {

	            	List < String > pathRetrieved = new ArrayList < > ();
	                target = this.targetNoe; // original target
	               for(String src: Src) { 
	                List < String > pathRet = getPaths(curDataset, src, target, 2);
	                pathRetrieved.addAll(pathRet);
	               }
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

	        String query = "prefix feds: <http://vocab.org.centre.insight/feds#> SELECT distinct ?o WHERE { { <" +
	            curDataset + "> feds:connectedThrough ?o. <" + nextDataset + "> feds:connectedThrough ?o.} UNION {<" +
	            nextDataset + "> feds:connectedThrough ?o. <" + curDataset + "> feds:connectedThrough ?o.} } limit 5";

	        QueryExecution qryExec = QueryExecutionFactory.create(query, mdl);

	        return qryExec.execSelect();

	    }

	    private void buildPath(Map < String, List < PathMerger >> map) {

	        for (Entry < String, List < PathMerger >> build: map.entrySet()) {

	            List < PathMerger > mergDataset = build.getValue();

	            for (PathMerger pathBuild: mergDataset) {

	                if (pathBuild.prvSet.isEmpty()) {

	                    for (String path: pathBuild.pathRetrieved) {
	                        if (path.endsWith(this.targetNoe))
	                            System.out.println(path);
	                    }
	                }

	              //  if(!pathBuild.pathRetrieved.isEmpty()){
	                if (!pathBuild.prvSet.isEmpty()) {

	                    while (!pathBuild.prvSet.isEmpty()) {
	                        String dataset = pathBuild.prvSet.removeLast();
	                        int i = 0;

	                        Iterator < String > iter = pathBuild.pathRetrieved.iterator();
	                        while (iter.hasNext()) {
	                            String currentPath = iter.next();

	                            if(currentPath.startsWith(this.sourceNode) && currentPath.endsWith(this.targetNoe))
	                            	System.out.println("single hope: "+ currentPath);
	                            
	                            for (String previousPath: map.get(dataset).get(i).pathRetrieved) {
	                                if (!currentPath.endsWith(this.targetNoe) && !previousPath.endsWith(this.targetNoe) && currentPath.startsWith(pathLastNode(previousPath))) {
	                                    //iter.remove();
	                                    pathBuild.pathRetrieved.set(i, previousPath.concat("<<<>>>").concat(currentPath));
	                                  //  iter.remove();

	                                } else {
	                                	
	                                    if (!previousPath.endsWith(this.targetNoe) && currentPath.startsWith(pathLastNode(previousPath)) && currentPath.endsWith(this.targetNoe))
	                                    		
	                                        System.out.println(previousPath.concat("<<<>>>").concat(currentPath));
	                                }

	                            }

	                        }
	                        i++;

	                    }
	                }
	           // }

	            }
	        }

	    }

	    private String pathLastNode(String path){
	    	
	    	return path.substring(path.lastIndexOf(">-") + 2);
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
				// TODO Auto-generated method stub
				return getPaths(this.curDataset, this.src, this.target,this.topK);
			}

	    }
}
