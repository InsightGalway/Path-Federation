package org.insight.centre.federation;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.spi.http.HttpContext;

import org.apache.commons.lang3.text.StrBuilder;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.util.Symbol;

import insight.dev.flushablemap.PathCache;
import insight.dev.flushablemap.SyncableMap;


public class RmoteQuery implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SyncableMap<Integer, PathCache> cacheDB;
	
	
	
	public RmoteQuery(SyncableMap<Integer, PathCache> cacheDB) {
		super();
		this.cacheDB = cacheDB;
	}

	//SyncableMap<Integer, PathCache> cacheDB;
	public List<String> FederateRequest(String endpoint,String sourceNode, String targetNode, int k){
		
		List<String> lst= new ArrayList<String>();
		String qry=  "PREFIX : <http://dbpedia.org/resource/>\n"
		        + "PREFIX ppfj: <java:org.centre.insight.property.path.>\n"
		        + "SELECT *"
		        //+ "WHERE { ?x :knows ?y . ?y :knows ?z . ?path ppfj:topk (?x ?z 2 \"(:knows*)\") }"
		        //+ "WHERE { SERVICE<http://10.196.2.181:3030/hdtservice/query> { ?path ppfj:topk (<http://dbpedia.org/resource/c> <http://dbpedia.org/resource/b> 3) } }";
		        + "WHERE { ?path ppfj:topk (<"+sourceNode+"> <"+targetNode+"> "+k+") . }";
			
		//System.out.println(endpoint+"="+qry);
	   Query query = QueryFactory.create(qry);
	   //query.setPrefix("feds", "http://feds-engine");
	   
	   
	  

       try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query)) {
    	   qexec.setTimeout(90000);
           ResultSet results = qexec.execSelect() ;
          
           for ( ; results.hasNext() ; ) {

               QuerySolution soln = results.nextSolution();
               
               StringBuilder sb = new StringBuilder();
               

               for (Var v : query.getProjectVars() ) {
                      // sb.append(v.toString());
                      // sb.append("=");

                       RDFNode val = soln.get(v.getVarName());
                       sb.append(val==null? "(nil)" : "("+prunePath(val.toString())+")");
                       lst.add(prunePath(val.toString()));
               }
               //System.out.println(sb);
           }
       }
       
       return lst;
	}
	
	public List<String> FederateRequestWithChunks(String endpoint, List<String> sourceNode, List<String> targetNode, int k){
		
		String strQry=constructQry(sourceNode, targetNode, k);
		
		List<String> lst= new ArrayList<String>();
	/*	String qry=  "PREFIX : <http://dbpedia.org/resource/>\n"
		        + "PREFIX ppfj: <java:org.centre.insight.property.path.>\n"
		        + "SELECT *"
		        //+ "WHERE { ?x :knows ?y . ?y :knows ?z . ?path ppfj:topk (?x ?z 2 \"(:knows*)\") }"
		        //+ "WHERE { SERVICE<http://10.196.2.181:3030/hdtservice/query> { ?path ppfj:topk (<http://dbpedia.org/resource/c> <http://dbpedia.org/resource/b> 3) } }";
		        + "WHERE { ?path ppfj:topk (<"+sourceNode+"> <"+targetNode+"> "+k+") . }";
*/			
		//System.out.println(endpoint+"="+qry);
	   Query query = QueryFactory.create(strQry);
	   //query.setPrefix("feds", "http://feds-engine");
	   
	   
	  

       try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query)) {
    	   qexec.setTimeout(90000);
           ResultSet results = qexec.execSelect() ;
          
           for ( ; results.hasNext() ; ) {

               QuerySolution soln = results.nextSolution();
               
               StringBuilder sb = new StringBuilder();
               

               for (Var v : query.getProjectVars() ) {
                      // sb.append(v.toString());
                      // sb.append("=");
            	   
                       RDFNode val = soln.get(v.getVarName());
                       if(val!=null){
                       sb.append(val==null? "(nil)" : "("+prunePath(val.toString())+")");
                       lst.add(prunePath(val.toString()));
                       }else{
                    	   
                    	   for(VarNodes varN: varNodes){
                    		
                    		   if(varN.getVar().equals(v.toString())){
                    			   
                    			 // PathCache cache=null;cache=new PathCache(varN.getSrc().hashCode(),varN.getTrgt().hashCode(),false);
                    			   //if(cacheDB.getValue(key))
                    			  //cacheDB.put(endpoint.hashCode(), cache);
                    			   
                    			   
                    			  break;
                    		   }
                    		   
                    	   }

                       }
               }
               //System.out.println(sb);
           }
       }
       
       return lst;
	}
	

	List<VarNodes> varNodes= new ArrayList<>();
	
	private String constructQry(List<String> sourceNode, List<String> targetNode, int k) {
		StringBuilder strBld= new StringBuilder();
		
		strBld.append("PREFIX ppfj: <java:org.centre.insight.property.path.> \n")
		.append("SELECT * \n").append("WHERE { \n");
		int var=0;
		
		
		
		for(String src:sourceNode){
			
			for(String trgt:targetNode){
			strBld.append("{ ");
			
			String varStr= "?path"+Integer.toString(var++);
			String srcStr= src;
			String trgStr= trgt;	
			
			varNodes.add(new VarNodes(varStr, srcStr, trgStr));
			
			strBld.append(varStr+ " ppfj:topk (<"+src+"> <"+trgt+"> "+k+"). \n");
			strBld.append("} UNION ");	
				
			}
		}
		
		
		strBld.append("}");
		int lastIndxOf=strBld.lastIndexOf("UNION");
		String str = strBld.substring(0, lastIndxOf).concat("}");
		
		
		
		return str;
		
	}

	private String prunePath(String sourcePath) {
		

		String temp1 = sourcePath.replaceAll("\\[", "").replaceAll("\\]", "");

		return temp1;
	}
	
	
	class VarNodes{
		
		String var;
		String src;
		String trgt;
		
		public VarNodes(String var, String src, String trgt) {
			super();
			this.var = var;
			this.src = src;
			this.trgt = trgt;
		}

		public String getVar() {
			return var;
		}

		public void setVar(String var) {
			this.var = var;
		}

		public String getSrc() {
			return src;
		}

		public void setSrc(String src) {
			this.src = src;
		}

		public String getTrgt() {
			return trgt;
		}

		public void setTrgt(String trgt) {
			this.trgt = trgt;
		}
		
		
		
	}
}
