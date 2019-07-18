# Importing Reference Sequence

This document shows the flow of how different types of ReferenceSequences are extracted and imported.

## Types of Reference Sequence to import
   
* GENOME_ASSEMBLY
* SEQUENCE
* TRANSCRIPTOME_SHOTGUN_ASSEMBLY
 
## Retrieving Reference Sequence Accessions

SRA Analysis records can contain a reference sequence in either of the three analysis categories:
* REFERENCE_ALIGNMENT
* SEQUENCE_VARIATION 
* PROCESSED_READS

It is guaranteed that each analysis belong to at most one of these three types.
Example XML below which is parsed into a Java object:
 
```xml
<SEQUENCE_VARIATION>
     <ASSEMBLY>
         <STANDARD accession="GCA_000001405.1"/>
     </ASSEMBLY>
     <SEQUENCE accession="CM000665.1" label="chr3"/>
     [...]
</SEQUENCE_VARIATION>
```

### Retrieving GENOME_ASSEMBLY accession
The genome_assembly type reference sequence accession is represented in the XML as shown below:
```xml
<ASSEMBLY>
   <STANDARD accession="GCA_000001405.1"/>
</ASSEMBLY>
```
  
### Retrieving SEQUENCE OR TRANSCRIPTOME_SHOTGUN_ASSEMBLY accession
The sequence or transcriptome type reference sequence accession is represented in the XML as shown below:
```xml
<SEQUENCE_VARIATION>
    <SEQUENCE accession="GBRU01" label="GBRU01"/>
    <SEQUENCE accession="JQ739518.1" label="JQ739518.1"/>
</SEQUENCE_VARIATION>
```
   
## Retrieving Reference Sequence XMLs from accessions
 
The reference sequence XML is retrieved via ENA API like other objects
 
### GENOME_ASSEMBLY XML
**For accessions starting with "GCF_"** (issued by NCBI RefSeq) the genome_assembly XML is retrieved with the following 2 endpoints from the 
NBCI's Entrez API, We can search by the GCF accession, then retrieve its associated metadata.                        
                                    
To obtain the internal ID based on the GCF:https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=assembly&term=GCF_000442705.1                                                                 

To query based on said internal ID:https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=assembly&id=49321

**For all other accessions** the genome_assembly XML is retrieved via ENA API which can be parsed via the _ASSEMBLYDocument_ factory parser.
A reference sequence is created by parsing the accession, name, patch and taxonomy. 
Patch numbers are calculated only for GRC assemblies for human (GRCh) and mouse (GRCm).

Example of assembly XML below,
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ROOT request="GCA_000001405.1&amp;display=xml">
 <ASSEMBLY accession="GCA_000001405.2" alias="GRCh37.p1" center_name="Genome Reference Consortium">
   <IDENTIFIERS>
      <PRIMARY_ID>GCA_000001405.2</PRIMARY_ID>
      <SUBMITTER_ID namespace="Genome Reference Consortium">GRCh37.p1</SUBMITTER_ID>
   </IDENTIFIERS>
    <TITLE>Genome Reference Consortium Human Build 37 patch release 1 (GRCh37.p1)</TITLE>
    <DESCRIPTION>The human reference assembly defines a standard upon which other whole genome studies are based. Providing the best representation of the human genome is essential for facilitating continued progress in understanding and improving human health. [...]</DESCRIPTION>
    <NAME>GRCh37.p1</NAME>
     <ASSEMBLY_LEVEL>chromosome</ASSEMBLY_LEVEL>
     <GENOME_REPRESENTATION>full</GENOME_REPRESENTATION>
     <TAXON>
       <TAXON_ID>9606</TAXON_ID>
       <SCIENTIFIC_NAME>Homo sapiens</SCIENTIFIC_NAME>
       <COMMON_NAME>human</COMMON_NAME>
     </TAXON>
               .....
  </ASSEMBLY>
</ROOT>
```
   
### SEQUENCE or TRANSCRIPTOME_SHOTGUN_ASSEMBLY XML
The sequence or transcriptome accession returns an entry XML which is then parsed manually. The difference between 
sequence and transcriptome XML is that all transcriptome XML will contain a keyword element with 
value "Transcriptome shotgun assembly" where the other doesn't contain it.
 
Example of sequence XML,
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ROOT request="JQ739518.1&amp;display=xml">
  <entry accession="JQ739518" version="1" entryVersion="1" dataClass="STD" taxonomicDivision="PLN" moleculeType="mRNA" sequenceLength="1558" topology="linear" firstPublic="2012-05-29" firstPublicRelease="112" lastUpdated="2012-05-29" lastUpdatedRelease="112">
    <description>Camellia oleifera fatty acid desaturase (FAD) mRNA, complete cds.</description>
    <feature name="source" location="1..1558">
      <taxon scientificName="Camellia oleifera" taxId="385388">
      </taxon>
    </feature>
   ...
  </entry> 
</ROOT>     		
```

Example of transcriptome XML,
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ROOT request="GBRU01&amp;display=xml">
 <entry accession="GBRU01000000" version="1" entryVersion="4" dataClass="SET" taxonomicDivision="PLN" 
 moleculeType="transcribed RNA" sequenceLength="74572" topology="linear" firstPublic="2015-10-21" firstPublicRelease="126" lastUpdated="2016-09-16" lastUpdatedRelease="130">
  <projectAccession>PRJNA261953</projectAccession>
   	 <description>Petunia axillaris, TSA project GBRU01000000 data</description>
   	 <keyword>Transcriptome Shotgun Assembly</keyword>
   	 <keyword>TSA</keyword>
   	 <xref db="ENA-TSA" id="GBRU01000001-GBRU01074572"/>
   	 <feature name="source" location="1..74572">
   	    <taxon scientificName="Petunia axillaris" taxId="33119">
   		</taxon>
   	 </feature>
 ...
 </entry> 
</ROOT>     		
```
