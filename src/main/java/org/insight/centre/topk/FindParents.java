package org.insight.centre.topk;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;




public class FindParents extends TopKAlgorithm{

	Model mdl;
	LinkedList<TNode> q;
	
	boolean reversed = false;
	
	protected Node startNode;
	
	public FindParents() {
		mdl= ModelFactory.createDefaultModel();
		InputStream in= FileManager.get().open("/Users/user/Desktop/My-Stuff/Data_Dumps/pharmgkb-port-3034/pharmgkb.nt");
        //TDB.sync(dataset);
		mdl.read(in,null,"N-TRIPLE");
		
	}
	public static void main(String[] args) throws Exception {
		
		String rs= "http://bio2rdf.org/uniprot:P10253";
		
		Node node= NodeFactory.createURI(rs);
		
		new FindParents().run(node,null,null,10,true);

	}

	
	private void findParents(TNode current) {
	
		
	ResIterator iter = mdl.listSubjectsWithProperty(null, mdl.getRDFNode(current.vertex));
	System.err.println(iter);
	
	TNode next=null;
		
	Triple t = null;
		
		for(StmtIterator stmtI= mdl.listStatements( (Resource)null,null,(RDFNode)mdl.getRDFNode(current.vertex));stmtI.hasNext(); ){//gIdx.getResource(vertex.toString()).listProperties();stmtI.hasNext();){
	   		 t = stmtI.next().asTriple();
	   		System.out.println(t);
	   		
	   		System.err.println(t.getSubject());
	   		next = TNode.createNode(t.getSubject(),t.getPredicate(),current);

			
				q.add(next);
				
				Path p = trace(next, new Path(Collections.singleton(t.getSubject())));
				if (!reversed) {
					p.reverse();
					solutions.add(p);
				}
			
			

		}
		
		if(t==null){
			System.out.println("no more parents");
		}
		 //solutions.add(e)
		
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

		boolean visitedEdge(Triple t) {
			if (prev != null) {
				return vertex.equals(t.getObject()) && incomingEdge.equals(t.getPredicate())
						&& prev.vertex.equals(t.getSubject()) || prev.visitedEdge(t);
			}
			return false;
		}
		public String toString(){
			return prev==null? "("+vertex+")"
							  : String.format("(%s)-%s-(%s)", prev.vertex, incomingEdge,vertex);
		}
	}



	@Override
	public void init(Model hdtIdx, Model mainModel) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void init(Properties config) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	void reset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<Node[]> _topk(Node start, Node end, int k, boolean backword) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<Path> run(Node start, Node end, Node filter, int k, boolean backword) throws Exception {
		
		startNode=start;
		TNode n = TNode.createNode(startNode, null, null);
		q = new LinkedList<>();
		q.add(n);

		

		int qsize = 0;
		while (!q.isEmpty()) {
			if (qsize < q.size()) {
				qsize = q.size();
			}
			TNode current = q.poll();
			findParents(current);
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



}
