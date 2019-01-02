


/**
 * Created by vadim on 13.10.16.
 */
public class TrivialDictionary<K> implements VertexDictionary<K,K>, EdgeDictionary<K,K> {

    static TrivialDictionary instance = null;

     public static <K> TrivialDictionary<K> instance(){
        if( instance==null ) {
            instance = new TrivialDictionary();
        }
        return instance;
    }
    protected TrivialDictionary(){
    }
    public K vertexEntry(K key, Edge.Component component) {
        return key;
    }
    public K vertexKey(K entry, Edge.Component component) {
        return entry;
    }
    public K edgeEntry(K key){ return key; }
    public K edgeKey(K entry){ return entry; }
}
