package org.insight.centre.topk;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;

import com.hp.hpl.jena.util.FileManager;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;



import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class BaselineTopKPath extends TopKAlgorithm implements  ModelInit{

	LinkedList<TNode> q;
		
	boolean reversed = false;
	protected Node startNode, targetNode;
	protected Node filterEdge;
	protected Model gIdx;

	protected Model mainModel;
	
	@Override
	public void init(Properties config) throws IOException {
       // if(config.getProperty("type").equals("hdt"))
            //gIdx = new HDTGraphIndex(config.getProperty("dataset"), true);
	}

    public void init(Model hdtIdx, Model mainModel ) {
        gIdx=hdtIdx;
        this.mainModel=mainModel;
    }

    public void reset(){
		solutions = new LinkedList<>();
		
    }


	public List<Node[]> _topk(Node start, Node end, int k, boolean backword) throws Exception {
		List<Path> results = run(start, end, null, k, backword);
		
		//System.out.println(Util.formatToString(results,gIdx));
		return null;//Util.formatToString(results,gIdx);
	}

	public List<Path> run(Node start, Node end, Node filter,  int k, boolean backword) throws Exception {

	      startNode=start;
	      targetNode = end;
	        
	      filterEdge =  filter==null? null : null;// gIdx.getProperty(filter.toString()).asNode();
	      
		TNode n = TNode.createNode(startNode, null, null);
		q = new LinkedList<>();
		q.add(n);

		

		int qsize = 0;
		while (!q.isEmpty()) {
			if (qsize < q.size()) {
				qsize = q.size();
			}
			TNode current = q.poll();
			visit(current, backword);
		
			if (solutions.size() >= k) {
				System.out.println("Max queue size: " + qsize);
				break;
			}
		}
		// write federated paths to file 
		System.out.println("Done!!");
		Util.writePathToFile(sourceToAllTemp);
		
		return solutions;


	}

	static int count=0;
	private void visit(TNode current, boolean backword) throws Exception {
		count=0;
		TNode next = null;
		
		for(StmtIterator stmtI= gIdx.listStatements(gIdx.getResource(current.vertex.toString()), null, (RDFNode)null);stmtI.hasNext(); ){//gIdx.getResource(vertex.toString()).listProperties();stmtI.hasNext();){
   		Triple t = stmtI.next().asTriple();
			if (t.getObject().isLiteral())
				continue;
			boolean doNotQueue = false;
			
			if (current.visitedEdge(t)) {
			
			
					continue;
			
				
			}
			
			
			
			if (current.visiteddVertex(t)) {
				
				if (t.getObject() != targetNode) {
					doNotQueue = false;
					//continue;
				}else{doNotQueue = true;}
				
				
				
				
				//continue;
			}

			next = TNode.createNode(t.getObject(), t.getPredicate(), current);
			
			if (!doNotQueue) {
				q.add(next);
			}
			
			
			
			/*if (t.getObject().equals(startNode)) {// break;
				doNotQueue = true;
				continue;
			}*/

			

			if (t.getObject().equals(targetNode) && (current.filterEdge == null || t.getPredicate() == filterEdge)) {
				doNotQueue = true;
				Path p = trace(next, new Path(Collections.singleton(targetNode)));
				if (!reversed && !backword) {
					// dont reverse p
					//p.reverse();
				
				}else{
					p.reverse();
					solutions.add(p);
				}
				
				
				
				//q.add(next);
				
			/*	boolean flag = false;

				ListIterator<?> it = p.vertexList().listIterator();
				Object[] arry = p.vertexList().toArray();

				while (it.hasNext()) {
					if (it.previousIndex() != -1) {
						flag = connectedThrough(mainModel, arry[it.previousIndex()].toString(),
								arry[it.nextIndex()].toString());
						if (flag)
							solutions.add(p);
					
					}
					it.next();
				}*/

				
					
			}
/*
			if (!doNotQueue) {
				q.add(next);
			}*/
		}

	}

	protected boolean  connectedThrough (Model modelTemp,String s, String t){
		
		//Model modelTemp2= ModelFactory.createDefaultModel();
       // InputStream in= FileManager.get().open("data/index-2.nt");
   
       // modelTemp2.read(in,null,"N-TRIPLE");
        
		String query= "prefix feds: <http://vocab.org.centre.insight/feds#>"
				+ " ASK WHERE {<" + s + ">  feds:connectedThrough ?con."
				+ "<" + t + ">  feds:connectedThrough ?con."
						+ "FILTER (?con!= <" + targetNode + ">)"
								+ "}";

        //String askQry="ASK WHERE{{<" +s + "> ?p ?o} UNION {<" +s + "> ?p ?o}}";
		
		 QueryExecution qryExec = QueryExecutionFactory.create(query, modelTemp);
		
		  return qryExec.execAsk();
	}
	  
	
	private List<String> prunePath(String sourcePath, String federatedPath ){
		List<String> pathList= new ArrayList<>();
		
		 String temp1 = sourcePath.replaceAll("\\[", "").replaceAll("\\]", "").substring(0, sourcePath.lastIndexOf("-("));
		
		String [] temp2 = federatedPath.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
		
		for(String breakArray: Arrays.asList(temp2))
		pathList.add(temp1.concat(breakArray));
		
		
		
		return pathList;
	}
	static Path trace( TNode n, Path p ){
		if( n.prev == null ){
			return p;
		}
		p.appendEdge( n.incomingEdge, n.prev.vertex );
		return trace(n.prev, p);
	}



    static class TNode {
    	Node vertex;
    	Node incomingEdge;
    	Node filterEdge = null;
		TNode prev = null;

		TNode(Node vertex, Node incomingEdge, TNode prev) {
	
			this.vertex = vertex;
			this.incomingEdge = incomingEdge;
			this.prev = prev;
			if (prev != null) {
				filterEdge = prev.filterEdge;
				if (prev.prev == null && incomingEdge == filterEdge) {
					//filtering condition already satisfied
					filterEdge = null;
				}
			}
		}

		public boolean visiteddVertex(Triple t) {
			if(prev!=null){
				return prev.vertex.equals(t.getObject());
			}
			return false;
		}

		static TNode createNode(Node vertex, Node incomingEdge, TNode prev){
			return new TNode(vertex,incomingEdge, prev);
		}

		//int cnt=0;
		boolean visitedEdge(Triple t) {
			if (prev != null) {
				 boolean visited=vertex.equals(t.getObject()) && incomingEdge.equals(t.getPredicate())
						&& prev.vertex.equals(t.getSubject()) || prev.visitedEdge(t);
				 //System.err.println(visited);
				 if(visited)
					 count++;
				 return visited;
			}
			return false;
		}
		public String toString(){
			return prev==null? "("+vertex+")"
							  : String.format("(%s)-%s-(%s)", prev.vertex, incomingEdge,vertex);
		}
	}



}
