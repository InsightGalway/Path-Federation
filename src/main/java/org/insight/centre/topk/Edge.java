package org.insight.centre.topk;

import java.util.Objects;

import com.hp.hpl.jena.graph.Node;

public class Edge<V,E> {

    public enum Component{ SOURCE, TARGET, EDGE }

    protected E label;
    protected V vertex;

    protected Edge(){ vertex = null; label = null; }

    public Edge(V vertex, E label){
        this.vertex = vertex;
        this.label = label;
    }

    public Edge(Edge<V,E> e){
        this.vertex = e.vertex();
        this.label = e.label();
    }

    public V vertex(){ return vertex; }

    public E label(){ return label; }


    @Override
    public int hashCode(){ return (vertex!=null? vertex.hashCode() : 0)
                                + (label!=null? 1000*label.hashCode() : 0); }

    @Override
    public boolean equals( Object obj ){
        Edge pe = (Edge)obj;
        if( pe != null ){
            return Objects.equals(vertex,pe.vertex) &&
                    Objects.equals(label,pe.label);
        }
        return false;
    }

    @Override
    public String toString(){ return "-" + label + "-("+vertex + ")"; }


}

