package org.insight.centre.topk;

/**
 * Created by vadim on 18.05.16.
 */
public interface Edges<T> {
    Iterable<T> vertexSequence();
    Iterable<Edge> edgeSequence();
}
