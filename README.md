# PPDs: Property Paths in Distributed RDF Datasets
PPDs is  a path query federation engine that federates the path queries and (i) retreives path from remote datasets or SPARQL endpoints, (ii) intelligently merges the paths, and (iii) present the **K** paths to users.

## experimental setup:

//

## SPRQL endpoint for experimental results

Fuseki endpoint URL: http://vmurq09.deri.ie:8030/
<br/>Copy and past the URL in your browser and go to control panel and then select the dataset/resuls. Fuseki interface will be displayed to pose a SPARQL query.

* The actual nodes used to find the paths between are as follow:

source node | target node
----------- | -----------
http://purl.obolibrary.org/obo/HP_0000818 | http://semanticscience.org/resource/SIO_000275
http://purl.obolibrary.org/obo/HP_0004942 | http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0001626
http://purl.obolibrary.org/obo/HP_0004942 | http://bioportal.bioontology.org/ontologies/DOID/DOID:7
http://identifiers.org/doid/DOID:0014667 | http://semanticscience.org/resource/SIO_000275
http://identifiers.org/dbsnp/rs769022521 | http://identifiers.org/ncbigene/10128
http://purl.obolibrary.org/obo/HP_0004942 | http://rdf.disgenet.org/v5.0.0/void/doClass
http://linkedlifedata.com/resource/umls/id/C0033581 | http://purl.obolibrary.org/obo/HP_0000024
http://identifiers.org/dbsnp/rs769022521 | http://monarchinitiative.org/gene/NCBIGene:10128
http://purl.obolibrary.org/obo/HP_0000818 | http://linkedlifedata.com/resource/phenotype/id/HP:0000818
http://purl.obolibrary.org/obo/HP_0000818 | http://bio2rdf.org/umls:C4025823
http://identifiers.org/dbsnp/rs769022521 | http://semanticscience.org/resource/SIO_000275
http://identifiers.org/dbsnp/rs769022521 | http://rdf.disgenet.org/v5.0.0/void/pantherClass

<br/> Following is the sample query you can use to check the statistics of the paths retreived:

* Please replace projection variable in the **SELECT** cluase to fetch your required data. youre need to replace object variable **\<source\>** and **\<target\>** nodes to find the paths between two nodes given in above table

Projection Var | Object Var
-------------- | ----------
?fpath | select the distinct paths
?hops | select the number of hops 
?endp | select the endpoints involved in 


 
```javascript
prefix feds: <http://vocab.org.centre.insight/feds#>
select distinct ?fpath    {
    ?path a feds:Path ;
    feds:fullPath ?fpath; 
    feds:fullPathHops ?hops;
    feds:endpInvolved ?endp;
    feds:startNode <source>;
    feds:endNode <target>.
} 
``` 

```javascript
prefix feds: <http://vocab.org.centre.insight/feds#>
select (count(distinct ?fpath) as ?tot){
?path a feds:Path ;
feds:fullPath ?fpath; 
feds:fullPathHops ?hops;
feds:endpInvolved ?endp;
feds:startNode <target>;
feds:endNode <target>.
} 
```

# TEAM
[Qaiser Mehmood](https://www.insight-centre.org/users/qaiser-mehmood) <br/>
[Muhammad Saleem](http://aksw.org/MuhammadSaleem.html) <br/>
[Ratnesh Sahay](https://www.insight-centre.org/users/ratnesh-sahay) <br/>
[Prof. Axel-Cyrille Ngonga Ngomo](https://www.uni-paderborn.de/person/65716/) <br/>
[Prof. Mathieu d'Aquin](https://www.insight-centre.org/users/mathieu-daquin)
