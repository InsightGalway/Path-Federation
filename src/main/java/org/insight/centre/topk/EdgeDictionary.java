package org.insight.centre.topk;



/**
 * Created by "Vadim Savenkov" on 07.12.16.
 */
public interface EdgeDictionary<E,K> {
   K edgeEntry(E key );
   E edgeKey(K entry );
}
