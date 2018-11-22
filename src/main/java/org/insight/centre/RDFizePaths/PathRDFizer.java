package org.insight.centre.RDFizePaths;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;


public class PathRDFizer {


	public static void RDFizeAndSave(String CompltPath, String endp, String startNode,String endNode, String pathFrom){
		
		Model mdl= ModelFactory.createDefaultModel();
		
		Resource pathResource = mdl.createResource(rmvBrkt(CompltPath));
		
		pathResource=mdl.createResource(rmvBrkt(CompltPath),Vocab.FullPath);
		pathResource.addProperty(Vocab.retrievedFrom, mdl.createResource(endp));
		pathResource.addProperty(Vocab.startNode, mdl.createResource(startNode));
		pathResource.addProperty(Vocab.endNode, mdl.createResource(endNode));
		
		//mdl.write(System.out, "N-Triples");
		writeToFile(mdl);
		
	}
	

		static protected void writeToFile(Model m){
			
			
			try {
				FileOutputStream fop = new FileOutputStream("path-components.nt", true);
				m.write(fop,"N-Triples");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

public static void RDFizeAndSave(String previousPath, String prevsDataset, String pPrevLastNode, String pPrevFirstNode,
		String currentPath, String curDataset, String currFirstNode, String currLastNode) {

	
	Model mdl= ModelFactory.createDefaultModel();
	
	Resource pPrevPath = mdl.createResource(rmvBrkt(previousPath));
	Resource pCurrPath = mdl.createResource(rmvBrkt(currentPath));
	
	
	pPrevPath=mdl.createResource(rmvBrkt(previousPath),Vocab.PartialPath);
	pPrevPath.addProperty(Vocab.retrievedFrom, mdl.createResource(rmvBrkt(prevsDataset)));
	pPrevPath.addProperty(Vocab.startNode, mdl.createResource(rmvBrkt(pPrevFirstNode)));
	pPrevPath.addProperty(Vocab.endNode, mdl.createResource(rmvBrkt(pPrevLastNode)));
	
	
	pCurrPath=mdl.createResource(rmvBrkt(currentPath),Vocab.PartialPath);
	pCurrPath.addProperty(Vocab.retrievedFrom, mdl.createResource(rmvBrkt(curDataset)));
	pCurrPath.addProperty(Vocab.startNode, mdl.createResource(rmvBrkt(currFirstNode)));
	pCurrPath.addProperty(Vocab.endNode, mdl.createResource(rmvBrkt(currLastNode)));
	
	mdl.write(System.out,"N-Triples");
	writeToFile(mdl);
		
}

protected static String rmvBrkt(String path){
	String afterRmv="";
	afterRmv=path.replaceAll("-<", "(").replaceAll(">-", ")");
	return afterRmv;
}


public static void RDFizeAndSave(String CompltPath, List<String> dtsContributed, String startNode, String endNode,
		String pathFrom) {

	Model mdl= ModelFactory.createDefaultModel();
	
	Resource pathResource = mdl.createResource(rmvBrkt(CompltPath));
	
	pathResource=mdl.createResource(rmvBrkt(CompltPath),Vocab.FullPath);
	
	pathResource.addProperty(Vocab.startNode, mdl.createResource(startNode));
	pathResource.addProperty(Vocab.endNode, mdl.createResource(endNode));
	
	for(String dataset: dtsContributed){
		pathResource.addProperty(Vocab.retrievedFrom, mdl.createResource(dataset));
	}
	//mdl.write(System.out, "N-Triples");
	writeToFile(mdl);
	
	
}

}
