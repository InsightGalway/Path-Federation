
//import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class Util
{

    static public void printPaths(Collection<List<Edge<Integer,Integer>>>paths, GraphIndex gi, String key){
        final Logger _log = LoggerFactory.getLogger(Path.class);

        PrintWriter writer;
        try{
            File file = new File("./test_output/" + key);
            file.getParentFile().mkdirs();
            writer = new PrintWriter(new FileOutputStream(file, false));
        }
        catch(FileNotFoundException ex){ return; }

        for (List<Edge<Integer,Integer>> path : paths) {
            _log.debug(format(path,gi));
            writer.println(format(path,gi));
        }
        writer.close();
    }

    public static <V,E> String format(Collection<Edge<V,E>> path, GraphIndex gi) {
        return format(path.iterator(), gi);
    }
    public static <V,E> String format(Iterator<Edge<V,E>> path, GraphIndex gi){
        VertexDictionary vertexDict = gi instanceof VertexDictionary ? (VertexDictionary) gi : TrivialDictionary.instance();
        EdgeDictionary edgeDict = gi instanceof EdgeDictionary ? (EdgeDictionary) gi : TrivialDictionary.instance();
        return format(path, vertexDict, edgeDict);
    }
    
    public static <V,E> String format(Iterator<Edge<V,E>> path, VertexDictionary vDict, EdgeDictionary eDict )
    {
        if( vDict == null ){
            vDict = TrivialDictionary.instance();
        }
        if( eDict == null ){
            eDict = TrivialDictionary.instance();
        }

        StringBuilder sb = new StringBuilder();

        while( path.hasNext() ){
            Edge<V,E> e = path.next();
            if(sb.length()==0){
                sb.append("[");
                sb.append(trimNS(vDict.vertexEntry(e.vertex(),Edge.Component.SOURCE)));
                continue;
            }
            sb.append("-<");
            sb.append(trimNS(eDict.edgeEntry(e.label()), true));
            sb.append(">-");
            sb.append(trimNS(vDict.vertexEntry(e.vertex(),Edge.Component.TARGET)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String trimNS( Object URI ){
        return trimNS(URI,false);
    }

    public static String trimNS( String URI, boolean soft, boolean ignore ){
        if( ignore ){ return URI; }
        int trimPos = URI.lastIndexOf('#');
        if( trimPos <= 0 ) {
            trimPos = URI.lastIndexOf('/');
        }
        if( soft && trimPos > 0 ){
            int penultimateSlash = URI.substring(0,trimPos-1).lastIndexOf('/');
            trimPos = penultimateSlash > 0 ? penultimateSlash : 0;
        }
        return trimPos>0? URI.substring(trimPos+1) : URI;
    }

    public static String trimNS( Object URI, boolean soft ){
        if( URI == null ){
            return "";
        }
        return trimNS(URI.toString(),soft, false);
    }
    
    static void writePathToFile(List<String> path){
    	
    	   PrintWriter writer;
           try{
               File file = new File("./test_output/" + "federated");
               file.getParentFile().mkdirs();
               writer = new PrintWriter(new FileOutputStream(file, false));
           }
           catch(FileNotFoundException ex){ return; }

           for (String p : path) {
               writer.println(p);
           }
           writer.close();
    	
    }


    /**
     * Format a path using the integer-based representation.
     *
     * @param path A path to convert into string.
     * @return
     */
    public static String format(Path path){
        if( !path.isEdgeLabeled() ){
            return path.toString();
        }
        else{
            StringBuilder sb = new StringBuilder();
            Iterator vs = path.vertexList().iterator();
            for( Object e : path.edgeSequence() ){
                if(sb.length()==0){
                    sb.append("[");
                    sb.append(vs.next());
                }
                sb.append("-<"); sb.append(e); sb.append(">");
                sb.append(vs.next());
            }
            if( sb.length()>0 ) {
                sb.append("]");
            }
            return sb.toString();
        }
    }





    public static String trimNS( String URI, boolean soft ){
        return trimNS(URI,soft, false);
    }

    public static String trimNS( String URI ){
        return trimNS(URI,false);
    }



}
