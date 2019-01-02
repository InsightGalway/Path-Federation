package org.insight.centre.topk;


import com.hp.hpl.jena.graph.Graph;

import org.apache.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.jena.graph.Node;

/**
 * Created by jumbrich on 01/06/16.
 */
public abstract class TopKAlgorithm implements  ModelInit{

    protected List<Path> solutions = new LinkedList<>();
    protected List<String> sourceToAllTemp = new LinkedList<>();
    
    abstract public void init(Properties config) throws IOException;

    /**
     * Is called before each topk method call
     */
    abstract void reset();

    public List<Node[]> topk(Node start, Node end, int k, boolean backword) throws Exception{
        System.out.println("TopK("+start+" , "+end+" , "+k+" , "+backword+")");

        reset();

        solutions = new LinkedList<>();

        return _topk(start, end, k, backword);
    }

    abstract public List<Node[]> _topk(Node start, Node end, int k, boolean backword) throws Exception;
    abstract public List<Path> run(Node start, Node end, Node filter, int k, boolean backword) throws Exception;
}

interface ModelInit{
    public void init(Model hdtIdx, Model mainModel);
}