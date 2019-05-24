package org.insight.centre.topk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.*;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.multimap.api.embedded.MultimapCache;
import org.insight.centre.cache.CacheClass;
import org.insight.centre.cache.PathCache1;
import org.insight.centre.federation.SendQueryInChunks;
import org.insight.centre.federation.SourceSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.util.FileManager;

import insight.dev.flushablemap.*;


public class StartTopK implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1132178429405140038L;
	static Logger _log= LoggerFactory.getLogger(StartTopK.class);
	static int SUBJECT=0;
	static int OBJECT=1;
	static boolean backword=false, forward=true;
	
	static Model mainModel= ModelFactory.createDefaultModel();
    static String cacheLocation = "data/cacheTestTest/";
	
	boolean  flagConnVia;
    @SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, NotFoundException, InterruptedException, ExecutionException {
	            
    	//MultimapCache<Integer, SourceSelection.PathCache> cacheDB=CacheClass.infinispan();
    	SyncableMap<Integer, PathCache> cacheDB = new SyncableMap<Integer, PathCache>();
    	                                cacheDB.setSerialiseFactory(PathCacheSerializationFactory.instance);  
       	                                cacheDB.load(cacheLocation);
    //	System.out.println(cacheDB.getValue(698547532).size());
    	
    	InputStream in= FileManager.get().open("data/updatedindex.nt");
    	mainModel.read(in,null,"N-TRIPLE");
    	
    	
    	
    	Set<String> sourceDatasets= new HashSet<>();
    	Set<String> targetDatasets= new HashSet<>(); 
    	
       
        List<Endpoint> involvedEndp= new ArrayList<>();
       
    /*    involvedEndp.add(new Endpoint("http://localhost:3040/d1/query", "http://d1.graph.sample"));
        involvedEndp.add( new Endpoint("http://localhost:3041/d2/query","http://d2.graph.sample"));
        involvedEndp.add(new Endpoint("http://localhost:3042/d3/query","http://d3.graph.sample"));
       // involvedEndp.add(new Endpoint("http://localhost:3043/d4/query", "http://d4.graph.sample"));
*/      
     
      /*  involvedEndp.add(new Endpoint("http://10.196.2.224:3037/lsr/query", "http://d4.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3041/hgnc/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3035/kegg/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3034/pharmgkb/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3040/genage/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3030/goa/query","http://d3.graph.sample"));
        //involvedEndp.add(new Endpoint("http://10.196.2.224:3032/omim/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3046/affymetrix/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3042/bioportal/query", "http://d1.graph.sample"));

       */
        
       involvedEndp.add(new Endpoint("http://localhost:3022/disease/query","http://d1.graph.sample"));
        
        involvedEndp.add(new Endpoint("http://localhost:3021/phenotype/query", "http://d1.graph.sample"));
      
        involvedEndp.add(new Endpoint("http://localhost:3020/do/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://localhost:3023/hpo/query", "http://d1.graph.sample"));
        
        involvedEndp.add(new Endpoint("http://localhost:3024/gene/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://localhost:3025/protein/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://localhost:3026/variant/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://localhost:3027/panther/query", "http://d1.graph.sample"));
        
       
    
      /* involvedEndp.add(new Endpoint("http://10.196.2.224:3022/disease/query","http://d1.graph.sample"));
        
        involvedEndp.add(new Endpoint("http://10.196.2.224:3021/phenotype/query", "http://d1.graph.sample"));
      
        involvedEndp.add(new Endpoint("http://10.196.2.224:3020/do/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3023/hpo/query", "http://d1.graph.sample"));
        
        involvedEndp.add(new Endpoint("http://10.196.2.224:3024/gene/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3025/protein/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3026/variant/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3027/panther/query", "http://d1.graph.sample"));*/
      
        
  /*    involvedEndp.add(new Endpoint("http://10.196.2.224:3001/disease/query", "http://d4.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3002/do/query","http://d3.graph.sample"));
        //involvedEndp.add(new Endpoint("http://10.196.2.224:3003/gene/query","http://d3.graph.sample"));
       // involvedEndp.add(new Endpoint("http://10.196.2.224:3004/genesymbol/query","http://d3.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3006/hpo/query","http://d3.graph.sample"));
       // involvedEndp.add(new Endpoint("http://10.196.2.224:3007/mesh/query","http://d3.graph.sample"));
        //involvedEndp.add(new Endpoint("http://10.196.2.224:3008/panther/query", "http://d1.graph.sample"));
        involvedEndp.add(new Endpoint("http://10.196.2.224:3009/phenotype/query", "http://d1.graph.sample"));
        //involvedEndp.add(new Endpoint("http://10.196.2.224:3010/protein/query", "http://d1.graph.sample"));
        //involvedEndp.add(new Endpoint("http://10.196.2.224:3011/pubmed/query", "http://d1.graph.sample"));
       // involvedEndp.add(new Endpoint("http://10.196.2.224:3012/uml/query", "http://d1.graph.sample"));
        //involvedEndp.add(new Endpoint("http://10.196.2.224:3013/variant/query", "http://d1.graph.sample"));
*/
        
  //  String source="http://node-F";
  //  String target= "http://node-E";
        
        
    
   /* working 
    *   String source="http://bio2rdf.org/kegg:hsa_4763";
        String target= "http://bio2rdf.org/omim:613113";*/
        
        
       
       /* worked but took long time (omim dataset)as compare to HDT,
        * String source="http://bio2rdf.org/omim:193520";
        String target= "http://bio2rdf.org/snomedct:7425008";*/
        
      /* working
       * String source="http://bio2rdf.org/kegg:hsa_4763"; 
        String target= "http://bio2rdf.org/taxonomy:9606";*/
        		
    /* working 
     *  String source="http://bio2rdf.org/uniprot:P21359";
       String target= "http://bio2rdf.org/go:0045762";*/
       
      /* working
       *  String source="http://bio2rdf.org/hgnc.symbol:KRT17P3"; 
        String target= "http://bio2rdf.org/refseq:NT_010799";
    */    
        
      /* working 
       * String source="http://bio2rdf.org/hgnc.symbol:PEX10";
        String target= "http://identifiers.org/hgnc.symbol/PEX10";
    */    
        
     
       // String source="http://bio2rdf.org/hgnc:25979"; //root of http://bio2rdf.org/hgnc:25979
       // String target= "http://bio2rdf.org/uniprot:Q9NQG6";
       
        
     //   String source="http://bio2rdf.org/hgnc.symbol:MIEF1"; //root of 
     //   String target= "http://bio2rdf.org/uniprot:Q9NQG6";
       
       /* TEST: works for 3 datasets involved in this path
        *  String source="http://bio2rdf.org/goa_resource:human_182602"; 
        String target= "http://qaiser.org";*/
        	
        
      /* 
       * working
        String source="http://bio2rdf.org/uniprot:P21359"; 
        String target= "http://bio2rdf.org/go:0007265";
      */  
        
      
   /*   working
    *  String source="http://linkedlifedata.com/resource/umls/id/C0003564"; 
       String target= "http://purl.obolibrary.org/obo/HP_0001608";
        */
       //------------- 
       
      /* (1)
       * work for goa (2 hops)
       * String source="http://bio2rdf.org/uniprot:P21359";
       String target= "http://bio2rdf.org/go:0045762";*/
       
        /*(2)
         * works for goa and affymetrix (2 and 3 hops)
         * String source="http://bio2rdf.org/affymetrix:212676_at";
        String target= "http://bio2rdf.org/go:0045762";
        */
        
   /* (3) 
    * work for goa and affymetrix (max 4 hops) 
    *    String source="http://bio2rdf.org/affymetrix:212676_at";
        String target= "http://identifiers.org/go/0045762";*/
        
        
     //   String source="http://bio2rdf.org/uniprot:P21359";
      //  String target= "http://bio2rdf.org/go:0043407";
        
        
        //-------------
     
        //(1)
    
     //  String source="http://purl.obolibrary.org/obo/HP_0000818"; 
      // String target= "http://semanticscience.org/resource/SIO_000275";
       
        //(2)
    //	String source="http://purl.obolibrary.org/obo/HP_0004942"; 
      //String target="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001626";
            
     // (3)
     //    String source="http://purl.obolibrary.org/obo/HP_0004942"; 
      //String target="http://bioportal.bioontology.org/ontologies/DOID/DOID:7";
        
        // (4)

       //  String source="http://identifiers.org/doid/DOID:0014667"; 
        //String target="http://semanticscience.org/resource/SIO_000275";
        
        //(5)
       //String source="http://identifiers.org/dbsnp/rs769022521"; 
        //String target="http://identifiers.org/ncbigene/10128";
        
        
        //(6)
        
    //     String source="http://purl.obolibrary.org/obo/HP_0004942"; 
     //  String target="http://rdf.disgenet.org/v5.0.0/void/doClass";
        
        // (7)
        // String source="http://linkedlifedata.com/resource/umls/id/C0033581"; 
       //  String target="http://purl.obolibrary.org/obo/HP_0000024";
        
        //(8)
       // String source="http://identifiers.org/dbsnp/rs769022521"; 
      //String target="http://monarchinitiative.org/gene/NCBIGene:10128";
        
        // (9)
        
      //    String source="http://purl.obolibrary.org/obo/HP_0000818"; 
       //  String target="http://linkedlifedata.com/resource/phenotype/id/HP:0000818";
        
        
        // (10)

         // String source="http://purl.obolibrary.org/obo/HP_0000818"; 
         // String target="http://bio2rdf.org/umls:C4025823";
        
        //(11)
       //String source= "http://identifiers.org/dbsnp/rs769022521";
       //String target= "http://semanticscience.org/resource/SIO_000275";
        
        
        //(12)
        
        String source="http://identifiers.org/dbsnp/rs769022521"; 
        String target="http://rdf.disgenet.org/v5.0.0/void/pantherClass";
      
       // (13)
        
      //  String source="http://identifiers.org/dbsnp/rs769022521"; 
      //  String target="http://rdf.disgenet.org/resource/panther.classification/PC00137";
        

        
        
        

        
        /*	Doesn't work for single dataset phenotype (if source and target only exists in single dataset, need to check the code)
         * String source="http://purl.obolibrary.org/obo/HP_0001405"; 
        String target= "http://identifiers.org/hp/HP:0001405";*/
       
        //(2)
    	//String source="http://purl.obolibrary.org/obo/HP_0004942"; 
      //String target="http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001626";
      
        
      /* (3)
       * work for three dataset phenotype, disease, hpoclass (3 hops) */
       //String source="http://purl.obolibrary.org/obo/HP_0004942"; 
       // String target="http://linkedlifedata.com/resource/umls/id/C0007222";
       
      /* (4)
       * works for three datasets phenotype, disease, doclass (3 hops)*/
      //  String source="http://purl.obolibrary.org/obo/HP_0004942"; 
     //  String target="http://bioportal.bioontology.org/ontologies/DOID/DOID:7";
   
        
     
        
        
       /* (6)
        *  path from singel dataset disease (single hop)*/
       // String source="http://linkedlifedata.com/resource/umls/id/C0033581"; 
       // String target="http://purl.obolibrary.org/obo/HP_0000119";
      
        
        
       
        
        //(8)
      //  String source="http://linkedlifedata.com/resource/umls/id/C0033581"; // through http://purl.obolibrary.org/obo/HP_0000024(prevoius)
      //  String target="http://linkedlifedata.com/resource/phenotype/id/HP:0000024";
        
        /* (9)
         * working for single dataset doClass (single hop) (not used in paper)*/
        //  String source="http://identifiers.org/doid/DOID:0014667"; 
       // String target="http://semanticscience.org/resource/SIO_000275";
        
   
        
  
        
 
        
        // used in paper
     // String source="http://identifiers.org/dbsnp/rs769022521"; 
      //  String target="http://identifiers.org/uniprot/P42704";
        
       
       
  
        
        

        
        
   

        
        // not used in paper
     //String source="http://purl.obolibrary.org/obo/HP_0000193"; 
    // String target="http://linkedlifedata.com/resource/umls/id/C0266122";
        
        
        	
    
       
		for (Endpoint endp : involvedEndp) {

			if (checkEndpoints(source, SUBJECT, endp)) {
				//long start=System.nanoTime();
				sourceDatasets.add(endp.endpName);
				// long end=System.nanoTime();
			     
				//  System.err.println("source dataset "+(end-start));
				

			}
		/*	if (checkEndpoints(source, OBJECT, endp)) {

				sourceDatasets.add(endp.endpName);
			}*/

			if (checkEndpoints(target, SUBJECT, endp)) {
				//long start=System.nanoTime();
				
				targetDatasets.add(endp.endpName);
				// long end=System.nanoTime();
			     
				//  System.out.println("target dataset "+(end-start));
			}
			if (checkEndpoints(target, OBJECT, endp)) {
				//long start=System.nanoTime();
				targetDatasets.add(endp.endpName);
				 //long end=System.nanoTime();
			     
				 // System.out.println("target dataset "+(end-start));
			}

		}

		
		Map<String, Set<String>> pathWithDatasets= new HashMap<>();
		

	  long start=System.currentTimeMillis();
	
	  for (String s : sourceDatasets) {

		for (String t : targetDatasets) {
			//System.err.println(t);
				//if(s.equals(t))
				Model md = mdlEndpConnectedTo( s,t);
				//System.err.println(new StartTopK().isFlagConnVia());
				//if(!new StartTopK().isFlagConnVia()){
				if(s.equals(t))
					continue;
				List<Path> results = bfsAlgo(s,t, md, forward);
				if(results.isEmpty()){
					results=bfsAlgo(t, s, md,backword);
					
				}
			
				for (Path p : results) {
					//System.out.println(p);
					//_log.info("dataset path to traverse is {} ", p);
					//new SourceSelection(mainModel,source, target,pathWithDatasets, cacheDB).startSourceSelection(p);
					new SendQueryInChunks(mainModel,source, target,pathWithDatasets, cacheDB).startSourceSelection(p);
				}
				
			}
		}
		 long end=System.currentTimeMillis();
	     
		  System.out.println("dataset connectivity "+(end-start));
	
	/*	 long end=System.currentTimeMillis();
		     
		  System.out.println(end-start);*/
		  
		  cacheDB.sync(cacheLocation);
 
    }


    protected static Model mdlEndpConnectedTo(String s, String t){
    	
    	  Model modelTemp= ModelFactory.createDefaultModel();
    	  
    	  String query1= "prefix feds: <http://vocab.org.centre.insight/feds#>"
    	    	 		+ "CONSTRUCT {?d1 feds:hasConnection ?con. ?con feds:hasTargetDataset ?d2}"
    	    	 		+ "  WHERE {"
    	    	 		+ "  {  SELECT DISTINCT ?d1 ?con ?d2"
    	    	 		+ " where {?d1  feds:hasConnection ?con."
    	    	 		+ " ?con  feds:hasTargetDataset ?d2."
    	    	 		+ "?con feds:hasCommonNode ?node."
    	    	 		//+ "    Filter (?d1=<http://localhost:3040/d1/query> && ?d2=<http://localhost:3041/d2/query>)"
    	    	 		+ "}"
    	    	 		+ "}"
    	    	 		+ "}";

          
          String query= "prefix feds: <http://vocab.org.centre.insight/feds#> CONSTRUCT {?s feds:connectedTo ?o} WHERE {?s feds:connectedTo ?o}";
          QueryExecution qryExec = QueryExecutionFactory.create(query, mainModel);
          modelTemp=qryExec.execConstruct();          
         // connectedThrough(modelTemp,s, t);
          
          return modelTemp;
          
    }
    
/*protected static void connectedThrough (Model modelTemp,String s, String t){
	String query= "prefix feds: <http://vocab.org.centre.insight/feds#>"
			+ " ASK WHERE {<" + s + ">  feds:connectedThrough <http://graph-node/f>."
			+ "<" + t + ">  feds:connectedThrough <http://graph-node/f>.}";
	 QueryExecution qryExec = QueryExecutionFactory.create(query, modelTemp);
	 System.err.println("source: = "+s " target: = "+ t);
	 if(qryExec.execAsk()){
		 System.err.println("true");
		 new StartTopK().setFlagConnVia(true);
	 }else{
		 System.err.println("flase");
		 new StartTopK().setFlagConnVia(false);
		 }
}
    */
	
public boolean isFlagConnVia() {
	return flagConnVia;
}


public void setFlagConnVia(boolean flagConnVia) {
	this.flagConnVia = flagConnVia;
}


	protected static boolean checkEndpoints(String node, int As, Endpoint endpoint){
    
		String askQry = "";

		if (As == SUBJECT) {
			askQry = "ASK WHERE{<%node> ?p ?o. }";
			askQry = askQry.replace("%graph", endpoint.graph).replace("%node", node);
		}
		if (As == OBJECT) {
			askQry = "ASK  WHERE{?s ?p <%node>. }";
			askQry = askQry.replace("%graph", endpoint.graph).replace("%node", node);
		}

		Query qry = QueryFactory.create(askQry);

		QueryExecution exec = QueryExecutionFactory.sparqlService(endpoint.endpName, qry);

		return exec.execAsk();
	
	
    }
    
    protected static List<Path> bfsAlgo(String sourceInp, String targetInp, Model model, boolean backword){
    
        HashMap<String, String> testcases = new LinkedHashMap<>();
        List<Path> results = null;
        boolean doPrint = true;
        boolean doCheck = false;
          
        //Task 1
        testcases.put("sample", "20,"+sourceInp+","+targetInp+",no");
  //    testcases.put("omim_q1", "2,http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00157,http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00396,no");
    //  testcases.put("pharmgkb_q1","2,http://bio2rdf.org/drugbank:BE0003380,http://bio2rdf.org/genatlas:TP53,no");
      
   //     testcases.put("pharmgkb_q1","2,http://bio2rdf.org/drugbank:DB01268,http://bio2rdf.org/genatlas:FLT3,no");
       //... http://identifiers.org/drugbank/DB01268
     //   testcases.put("pharmgkb_q1","2,http://bio2rdf.org/drugbank:DB01268,http://bio2rdf.org/hgnc.symbol:FLT3,no");
        //  testcases.put("cosmic_q1","10,http://bio2rdf.org/genecards:BCL2,http://bio2rdf.org/genbank:L10338,no");
       // Model model = RDFDataMgr.loadModel("data/sample.nt") ;
  
        //String loc= "data/tdb";
       
        // Location loc= new Location("data/tdb");
        //Dataset dataset=TDBFactory.createDataset(loc);
        //Model model= dataset.getDefaultModel();
        
        //Model model= ModelFactory.createDefaultModel();
       // InputStream in= FileManager.get().open("data/index.nt");
        //TDB.sync(dataset);
       // model.read(in,null,"N-TRIPLE");
        
        
        
        
        
     /*   StmtIterator stm = model.listStatements();
         while (stm.hasNext()){
        	 System.err.println(stm.nextStatement());
         }*/

        for (Map.Entry<String, String> entry : testcases.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
           // System.out.println("Testcase: " + key + " Conditions: " + value);
            String[] cond = value.split(",");
            int k = Integer.parseInt(cond[0]);
            String root = cond[1];
            String target = cond[2];
            String edge = cond[3];
            if (edge.equalsIgnoreCase("no")) {
                edge = null;
            }
            
           BaselineTopKPath topK= new BaselineTopKPath();
            
           // BaselineBidirectionalTopK topK = new BaselineBidirectionalTopK();
            
            topK.init(model,mainModel);

           
            try {
                results = topK.run( ResourceFactory.createResource(root).asNode(), ResourceFactory.createResource(target).asNode(), ResourceFactory.createResource(edge).asNode(), k, backword);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }


            if( doPrint ) {
             
            	//Graph g= model.getGraph();
            	//GraphIndex gi = new RDFGraphIndex(g);
            	
            	
            	System.err.println(results);
               //Util.printPaths(results, gi, key);
             
            }
    }
        return results;
    }
  }
