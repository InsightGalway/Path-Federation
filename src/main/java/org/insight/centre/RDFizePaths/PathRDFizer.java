package org.insight.centre.RDFizePaths;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import com.hp.hpl.jena.shared.uuid.JenaUUID;


public class PathRDFizer {


	

		static protected void writeToFile(Model m){
			
			
			try {
				FileOutputStream fop = new FileOutputStream("path-components.nt", true);
				m.write(fop,"N-Triples");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}


protected static String rmvBrkt(String path){
	String afterRmv="";
	afterRmv=path.replaceAll("-<", "(").replaceAll(">-", ")");
	return afterRmv;
}


public static void RDFizeAndSave(String path, Map<String, String> dtsContributed, String startNode, String endNode, boolean fpath) {

	
	JenaUUID uuid = JenaUUID.generate();
	String fullPath= "FullPath:"+uuid.toString();
	
	
	Model mdl= ModelFactory.createDefaultModel();
if(fpath){	
	Resource pathResource = mdl.createResource(fullPath);
	
	pathResource=mdl.createResource(fullPath,Vocab.Path);
	
	pathResource.addProperty(Vocab.fullPath, mdl.createTypedLiteral(new String(rmvBrkt(path))));
	pathResource.addProperty(Vocab.startNode, mdl.createResource(startNode));
	pathResource.addProperty(Vocab.endNode, mdl.createResource(endNode));
	pathResource.addProperty(Vocab.fullPathHops, mdl.createTypedLiteral(new Integer(countHops(rmvBrkt(path)))));
	
	for(Map.Entry<String, String> pPaths: dtsContributed.entrySet()){
		
		//datsets contributed in full path
		pathResource.addProperty(Vocab.retrievedFrom, mdl.createResource(pPaths.getKey()));
	if(dtsContributed.size()>1){	
		// each partial path contributed to involved datasets
		pathResource.addProperty(Vocab.partialPathInvolved, mdl.createResource(rmvBrkt(pPaths.getValue()))
				.addProperty(Vocab.retrievedFrom, mdl.createResource(pPaths.getKey()))
				.addProperty(Vocab.startNode, mdl.createResource(pathFirstNode(pPaths.getValue())))
				.addProperty(Vocab.endNode, mdl.createResource(pathLastNode(pPaths.getValue())))
				.addProperty(Vocab.partialPathHops, mdl.createTypedLiteral(new Integer(countHops(rmvBrkt(pPaths.getValue())))))
				
				);
		
		// start and end nodes of each partial paths and involved datasets (will be usefull to find the paths between different dataset with sparql)
	}
		
		
	}
}
	else{
		
		Resource pPathResource = mdl.createResource(path);
		pPathResource=mdl.createResource(path,Vocab.Path);
		
		pPathResource.addProperty(Vocab.partialPath, mdl.createTypedLiteral(new String(path )));
		pPathResource.addProperty(Vocab.startNode, mdl.createResource(startNode));
		pPathResource.addProperty(Vocab.endNode, mdl.createResource(endNode));
		pPathResource.addProperty(Vocab.partialPathHops, mdl.createTypedLiteral(new Integer(countHops(rmvBrkt(path)))));
		
		
		for(Map.Entry<String, String> pPaths: dtsContributed.entrySet()){
			
			//datsets contributed in full path
			pPathResource.addProperty(Vocab.retrievedFrom, mdl.createResource(pPaths.getKey()));
			
			// each partial path contributed to involved datasets
			pPathResource.addProperty(Vocab.partialPathInvolved, mdl.createResource(rmvBrkt(pPaths.getValue()))
					.addProperty(Vocab.retrievedFrom, mdl.createResource(pPaths.getKey()))
					.addProperty(Vocab.startNode, mdl.createResource(pathFirstNode(pPaths.getValue())))
					.addProperty(Vocab.endNode, mdl.createResource(pathLastNode(pPaths.getValue())))
					.addProperty(Vocab.partialPathHops, mdl.createTypedLiteral(new Integer(countHops(rmvBrkt(pPaths.getValue())))))
					
					);
			
			// start and end nodes of each partial paths and involved datasets (will be usefull to find the paths between different dataset with sparql)
			
			
			
		}
	}
	
	
	//mdl.write(System.out, "N-Triples");
	writeToFile(mdl);
	
	
}



private static int countHops(String path){
	
	//String[] pathArray = path.split("\\(.*?\\)");
	//System.out.println(pathArray);
	
	int i=0;
	Pattern p = Pattern.compile("\\((.*?)\\)");
	Matcher m = p.matcher(path);

	while(m.find()) {
	    //System.out.println(m.group(1));
	    i++;
	}
	
	return i;
}

private static String pathLastNode(String path){
	
	return path.substring(path.lastIndexOf(">-") + 2);
}

private static String pathFirstNode(String path){
	return path.split("-<")[0];
	
}





}
