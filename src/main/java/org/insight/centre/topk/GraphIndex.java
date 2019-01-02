


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.shared.NotFoundException;



interface GraphIndex<V,E> {

	 Iterator<Edge<V,E>> lookupEdges(V source, V target);
}
