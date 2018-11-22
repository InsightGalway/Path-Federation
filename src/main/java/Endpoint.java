

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.RDFNode;


public class Endpoint {

	String endpName;
	String graph;
	RDFNode subject;
	RDFNode object;
	
		
	static Map <String, List<RDFNode>> connectedThroughMap;
	static Map <String, List<String>> connectedToMap;
	
	public Endpoint() {
		
		connectedThroughMap= new java.util.HashMap<String, List<RDFNode>>();
		connectedToMap=new java.util.HashMap<String, List<String>>();
	}
	public Endpoint(String endpName, String graph) {
	
		this.endpName=endpName;
		this.graph=graph;
		connectedThroughMap= new java.util.HashMap<String, List<RDFNode>>();
		connectedToMap=new java.util.HashMap<String, List<String>>();
	}
	
	public Endpoint(RDFNode subject, RDFNode object, String endpName) {
		
		this.subject=subject;
		this.object=object;
		this.endpName=endpName;
	}

	
	public static void connected(String sourceEndp, String conenctedEndp, RDFNode rdfNode) {

		if (connectedToMap.containsKey(sourceEndp)) {
			connectedToMap.get(sourceEndp).add(conenctedEndp);
		} else {
			List<String> lstConnetedTo = new ArrayList<>();
			lstConnetedTo.add(conenctedEndp);
			connectedToMap.put(sourceEndp, lstConnetedTo);
		}

		if (connectedThroughMap.containsKey(conenctedEndp)) {
			connectedThroughMap.get(conenctedEndp).add(rdfNode);
		} else {
			List<RDFNode> lstConnetedThrough = new ArrayList<>();
			lstConnetedThrough.add(rdfNode);

			connectedThroughMap.put(conenctedEndp, lstConnetedThrough);
		}

	}
	
	public String getEndpName() {
		return endpName;
	}

	public void setEndpName(String endpName) {
		this.endpName = endpName;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}
	public static Map<String, List<RDFNode>> getConnectedThroughMap() {
		return connectedThroughMap;
	}

	public static Map<String, List<String>> getConnectedToMap() {
		return connectedToMap;
	}


	
	
	
}
