package org.insight.centre.federation;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.spi.http.HttpContext;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.UpdateProcessRemoteBase;
import org.apache.jena.sparql.util.Symbol;

public class RmoteQuery {

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
	
	
	private String prunePath(String sourcePath) {
		

		String temp1 = sourcePath.replaceAll("\\[", "").replaceAll("\\]", "");

		return temp1;
	}
}
