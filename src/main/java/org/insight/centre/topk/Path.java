package org.insight.centre.topk;



import java.util.*;

public class Path<V,E>  {
    public ArrayList<V> vertSeq;
    protected int hash = 0;

    protected ArrayList<E> edgeSeq = null;

    public Path(){
        vertSeq = new ArrayList<>();
    }

    public Path(Collection<V> vertexSequence)
    {
        this();
        appendPath(vertexSequence);
    }

    public Path(Collection<V> vertexSequence, Collection<E> edgeSequence )
    {
        this();
        appendPath(vertexSequence,edgeSequence);
    }

    public Path( Path p ) {
        this();
        appendPath(p);
    }

    public void appendVertex(V vertex){
        vertSeq.add(vertex);
        if( edgeSeq != null ){
            throw new RuntimeException("Cannot add vertex without an edge to an edge-labeled path.");
        }
        hash = 0;
    }

    public void appendEdge( E edge, V vertex ) {

        if( edgeSeq == null && vertSeq.size()==1 ){
            edgeSeq = new ArrayList<>();
        }

        if( edgeSeq == null || edgeSeq.size()!= vertSeq.size()-1)
        {
            throw new RuntimeException(  "Number of edges " + (edgeSeq==null? 0 : edgeSeq.size())
                    + " and vertices " + vertSeq.size() + " in an edge-labeled path do not match.");
        }
        edgeSeq.add(edge);
        vertSeq.add(vertex);
        hash = 0;
    }

    public void appendPath( Collection<V> vertexSequence ){
        appendPath(vertexSequence, null);
    }

    public void appendPath(Path<V,E> path){
        appendPath(path.vertSeq, path.edgeSeq);
    }

    public void appendPath( Collection<V> vertexSequence, Collection<E> edgeSequence )
    {
        if( edgeSequence!=null )
        {
            if(edgeSeq==null){
                if( vertSeq.size()>1 ){
                    throw new RuntimeException("Appending labeled path to an unlabeled path");
                }
                edgeSeq = new ArrayList<>();
            }
            edgeSeq.addAll(edgeSequence);

            Iterator<V> vit = vertexSequence.iterator();
            if( !vertSeq.isEmpty() ) {
                // ensure that our last vertex and the first vertex in the appended path coincide
                if (  vit.hasNext() ) {
                    if( !vit.next().equals(vertSeq.get(vertSeq.size()-1)) ){
                        throw new RuntimeException("Only paths with matching last/first vertex can be concatenated");
                    }
                }
            }
            vertSeq.ensureCapacity( vertSeq.size() + vertexSequence.size() );
            while( vit.hasNext() ){
                vertSeq.add( vit.next() );
            }
            if( vertSeq.size() != edgeSeq.size()+1 ) {
                throw new RuntimeException(  "Number of edges " + (edgeSeq==null? 0 : edgeSeq.size())
                        + " and vertices " + vertSeq.size() + " in an edge-labeled path do not match.");
            }
        }
        else {
            if( isEdgeLabeled() ){
                if( vertexSequence.size()==1
                        && vertexSequence.iterator().next().equals(vertSeq.get(vertSeq.size()-1)) ){
                    //only this trivial case is allowed when we have edge labels and the appended path does not
                    return;
                }
                throw new RuntimeException("Appending an unlabeled path to a labeled path");
            }
            vertSeq.addAll(vertexSequence);
        }
        hash = 0;
    }

    public boolean isEdgeLabeled(){
        return edgeSeq!=null;
    }

//    public boolean isValid(){
//        if( isEdgeLabeled() ) {
//            Set<Link> edges = new HashSet<>(edgeSeq.size());
//
//            for (Link e : this.edgeSequence()) {
//                Link pe = new Link(e);
//                if (edges.contains(pe)) {
//                    return false;
//                }
//                edges.add(pe);
//            }
//        }
//        return true;
//    }

    public void reverse(){
        Collections.reverse(vertSeq);
        if( edgeSeq != null ){
            Collections.reverse(edgeSeq);
        }
        hash=0; //set for rehash
    }

    @Override
    public int hashCode() {
        if(hash == 0) {
            hash = 1;
            for(V e : vertSeq) {
                if(e != null) {
                    hash = 5 * hash + e.hashCode();
                }
            }
            if(edgeSeq!=null)
            {
                int edgeHash = 1;
                for(E e : this.edgeSeq) {
                    if(e != null) {
                        edgeHash = 5 * edgeHash + e.hashCode();
                    }
                }
                hash += 100000*edgeHash + hash;
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(!(o instanceof Path) || this.hashCode()!=o.hashCode() ) {
            return false;
        }
        Path p = (Path)o;
        if( edgeSeq!= null && p.edgeSeq==null || !p.edgeSeq.equals(edgeSeq) ){
            return false;
        }
        return vertSeq.equals(p.vertSeq);
    }

    public int numVertices() {
        return vertSeq.size();
    }

    public boolean isEmpty(){
        return vertSeq.isEmpty();
    }

    public List<V> vertexList()
    {
        return Collections.unmodifiableList(vertSeq);
    }

    //@Override
    public Iterable<V> vertexSequence() {
        return Collections.unmodifiableCollection(vertSeq);
    }

    //@Override
    public Iterable<E> edgeSequence() {
        return Collections.unmodifiableCollection(edgeSeq);
    }

    @Override
    public String toString(){
        return Util.format(this);
    }

//    public Path join(Path p_otherdir, TNode n) {
//
//        Path result = new Path();
//
//        result.appendPath(new ArrayList(this.vertSeq), new ArrayList(this.edgeSeq)); //should be deep copy
//
//        ArrayList v =null;
//        if( p_otherdir.vertSeq!=null){
//            v=new ArrayList(p_otherdir.vertSeq);
//            v.remove(0);
//        }
//        ArrayList e =null;
//        if( p_otherdir.edgeSeq!=null){
//            e=new ArrayList(p_otherdir.edgeSeq);
//            //if(e.size()>1)
//            //    e.remove(0);
//        }
//
//
//
//        result.appendPath(v, e);
//
//        return result;
//    }

    class EdgeIterator extends Edge<V,E> implements Iterator<Edge<V,E>>  {

        final Iterator<V> v;
        final Iterator<E> e;

        EdgeIterator( Iterator<V> vertexIterator, Iterator<E>  edgeIterator ) {
            super(null,null);
            v = vertexIterator;
            e = edgeIterator;

            if (v.hasNext()) {
                this.vertex = v.next();
            }
        }

        @Override
        public boolean hasNext () {
            return v.hasNext();
        }

        @Override
        public Edge next () {
            this.vertex = v.next();
            if( e!=null ){
                this.label = e.next();
            }
            return this;
        }

        @Override
        public void remove () {
            v.remove();
            if( e!=null ){
                e.remove();
            }
        }

    }

}