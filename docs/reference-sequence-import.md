# Importing Reference Sequence

 This document shows the flow of how different types of ReferenceSequences are extracted and imported.

## Types of Reference Sequence to import
   
   * ASEMBLY
   * GENE
   * TRANSCRIPTOME
 
## Retrieving Reference Sequence Accessions

 SRA Analysis records can contain a reference sequence in either of the three analysis categories:
   * REFERENCE_ALIGNMENT
   * SEQUENCE_VARIATION 
   * PROCESSED_READS. 
   
 It is guaranteed that each analysis contains at most one of these three types.
 Example XML below which is parsed into a Java object:
 
 ```
 <SEQUENCE_VARIATION>
     <ASSEMBLY>
         <STANDARD accession="GCA_000001405.1"/>
     </ASSEMBLY>
     <SEQUENCE accession="GBRU01" label="GBRU01"/>
     <SEQUENCE accession="JQ739518.1" label="JQ739518.1"/>
     <SEQUENCE accession="CM000665.1" label="chr3"/>
     [...]
 </SEQUENCE_VARIATION>
```

 ##### Retrieving ASSEMBLY accession
  The Assembly type reference sequence accession is represented in below part of the xml.
  ``` 
  <ASSEMBLY>
          <STANDARD accession="GCA_000001405.1"/>
  </ASSEMBLY>
  ```
  
 ##### Retrieving GENE OR TRANSCRIPTOME accession
  The GENE or TRANSCRIPTOME type reference sequence accession is represented in below part of the xml.
   ```
        <SEQUENCE accession="GBRU01" label="GBRU01"/>
        <SEQUENCE accession="JQ739518.1" label="JQ739518.1"/>
   ```
   
 ## Retrieving Reference Sequence Xmls from accessions
 
 The reference sequence xml is retrieved via ENA API like other objects
 
 ##### ASSEMBLY XML
  The assembly accession returns an assembly xml which can be parsed via ASSEMBLYDocument factory parser.
   The patches are calculated only for GRch| GRcm from the alias name. A reference sequence is created by parsing the
    accession , name , patch and taxon details.
   Example of assembly xml below,
   ```
   <?xml version="1.0" encoding="UTF-8"?>
   <ROOT request="GCA_000001405.1&amp;display=xml">
    <ASSEMBLY accession="GCA_000001405.2" alias="GRCh37.p1" center_name="Genome Reference Consortium">
            <IDENTIFIERS>
                <PRIMARY_ID>GCA_000001405.2</PRIMARY_ID>
                <SUBMITTER_ID namespace="Genome Reference Consortium">GRCh37.p1</SUBMITTER_ID>
            </IDENTIFIERS>
            <TITLE>Genome Reference Consortium Human Build 37 patch release 1 (GRCh37.p1)</TITLE>
            <DESCRIPTION>The human reference assembly defines a standard upon which other whole genome studies are based. Providing the best representation of the human genome is essential for facilitating continued progress in understanding and improving human health. &lt;p&gt;The Human Genome Project (HGP) was an international research collaboration coordinated by the U.S. Department of Energy (DOE) and National Institutes of Health (NIH) whose goals were to determine the sequence of the human chromosomes and identify and map all human genes. The project began in 1988 when Congress funded both the NIH and the DOE and was completed in April of 2003. The HGP used a clone-oriented approach to produce map data, clone reagents, and the assembled human genome sequence. The International Human Genome Sequencing Consortium published the first draft of the human genome, with the sequence 90% complete, in the journal Nature in February 2001. The full sequence was completed in April 2003. The HGP resulted in significant technology, tool, and resource development that continues to have a significant impact on medicine and other life sciences. &lt;/p&gt; &lt;p&gt;The Genome Reference Consortium (GRC) was formed in 2008 to maintain the reference assembly for the human genome. The goals of this group are to correct regions that are misrepresented, to close remaining gaps, and to produce alternative assemblies of structurally variant loci. The consortium does experimental work to address gaps or sub-optimal sequence regions and has developed the infrastructure to review and curate assembly joins. The GRC consists of: The Wellcome Trust Sanger Institute, The Genome Center at Washington University, The European Bioinformatics Institute and The National Center for Biotechnology Information. The public can see regions under review and report genome problems at the GRC website, &lt;a href="http://genomereference.org" alt="link to GRC home page"&gt;http://genomereference.org&lt;/a&gt;. &lt;/p&gt; &lt;p&gt;The human genome assembly submitted by the GRC is available in GenBank under accession ranges GL000001-GL000258 (the scaffolds), and CM000663-CM000686 (the chromosomes).&lt;br/&gt;&lt;p&gt;&lt;b&gt;Comment&lt;/b&gt;&lt;br/&gt;The DNA sequence is composed of genomic sequence, primarily finished clones that were sequenced as part of the Human Genome Project. PCR products and WGS shotgun sequence have been added where necessary to fill gaps or correct errors. All such additions are manually curated by GRC staff. For more information see: http://genomereference.org</DESCRIPTION>
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
   
 ##### GENE or TRANSCRIPTOME XML
  The Gene or Transcriptome accession returns an entry Xml which is then parsed manually.
   The difference between transcriptome and gene sequence xml is that all transcriptome xml will contain a keyword 
   element with value "Transcriptome shotgun assembly" where the other doesn't contain it.
   
   Example of Gene sequence xml,
   ```
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

   Example of Transcriptome xml,
   ```
   <?xml version="1.0" encoding="UTF-8"?>
   <ROOT request="GBRU01&amp;display=xml">
   <entry accession="GBRU01000000" version="1" entryVersion="4" dataClass="SET" taxonomicDivision="PLN" moleculeType="transcribed RNA" sequenceLength="74572" topology="linear" firstPublic="2015-10-21" firstPublicRelease="126" lastUpdated="2016-09-16" lastUpdatedRelease="130">
   	 <projectAccession>PRJNA261953</projectAccession>
   	 <description>Petunia axillaris, TSA project GBRU01000000 data</description>
   	 <keyword>Transcriptome Shotgun Assembly</keyword>
   	 <keyword>TSA</keyword>
   	 <xref db="ENA-TSA" id="GBRU01000001-GBRU01074572"/>
   	 <feature name="source" location="1..74572">
   		<taxon scientificName="Petunia axillaris" taxId="33119">
   		</taxon>
      		...
   </entry> 
   </ROOT>     		
```

   
   
    
  
  
  
    
