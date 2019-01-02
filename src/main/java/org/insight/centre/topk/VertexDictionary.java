


/**
 * Created by vadim on 13.10.16.
 */
public interface VertexDictionary<V,K> {
    K vertexEntry(V key, Edge.Component component );
    V vertexKey(K entry, Edge.Component component );
}
