package org.insight.centre.RDFizePaths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Vocab {

	/** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://vocab.org.centre.insight/feds#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final Property endpInvolved = m_model.createProperty( NAMESPACE+"endpInvolved" );
    public static final Property startNode = m_model.createProperty( NAMESPACE+"startNode" );
    public static final Property endNode = m_model.createProperty( NAMESPACE+"endNode" );
    public static final Property partialPathInvolved = m_model.createProperty( NAMESPACE+"partialPathInvolved" );
    
    public static final Property fullPath = m_model.createProperty( NAMESPACE+"fullPath" );
    public static final Property fullPathHops = m_model.createProperty( NAMESPACE+"fullPathHops" );
    public static final Property partialPathHops = m_model.createProperty( NAMESPACE+"partialPathHops" );
    public static final Property partialPath = m_model.createProperty( NAMESPACE+"partialPath" );
    
    
    public static final Resource Path = m_model.createResource( NAMESPACE+"Path" );
    
 
    /** <p>RDFS namespace</p> */ 
    public static final Property RDFS = m_model.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
}
