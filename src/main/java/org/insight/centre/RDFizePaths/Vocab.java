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
    
    public static final Property retrievedFrom = m_model.createProperty( NAMESPACE+"retrievedFrom" );
    public static final Property startNode = m_model.createProperty( NAMESPACE+"startNode" );
    public static final Property endNode = m_model.createProperty( NAMESPACE+"endNode" );
    
    public static final Resource FullPath = m_model.createResource( NAMESPACE+"FullPath" );
    public static final Resource PartialPath = m_model.createResource( NAMESPACE+"PartialPath" );
 
    /** <p>RDFS namespace</p> */ 
    public static final Property RDFS = m_model.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
}
