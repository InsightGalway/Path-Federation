package org.insight.centre.topk;




import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Created by Vadim on 05.06.2017.
 */
public class RDFGraphIndex implements GraphIndex<Node,Node> {
   // final static PathArbiter<Node,Node> allPass = new AllPassArbiter<>();

    Graph graph;

    public RDFGraphIndex(Graph graph){
        this.graph = graph;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }


    @Override
    public Iterator<Edge<Node, Node>> lookupEdges(Node source, Node target) {
        return new WrappingIterator(graph.find(source,null,target), source==null);
    }

  

    static class WrappingIterator implements Iterator<Edge<Node,Node>>{

        ExtendedIterator<Triple> inner;
        boolean fetchSubjects;

        public WrappingIterator( ExtendedIterator<Triple> inner, boolean fetchSubjects  ){
            this.inner = inner;
            this.fetchSubjects = fetchSubjects;
        }

        @Override
        public boolean hasNext() {
            return inner.hasNext();
        }

        @Override
        public Edge<Node, Node> next() {
            Triple  t = inner.next();
            if( fetchSubjects ) {
                return new Edge(t.getSubject(),t.getPredicate());
            }
            return new Edge(t.getObject(),t.getPredicate());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

    }

}
