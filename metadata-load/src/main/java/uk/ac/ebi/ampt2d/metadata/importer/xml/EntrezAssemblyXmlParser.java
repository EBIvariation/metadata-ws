/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.ampt2d.metadata.importer.xml;

import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.util.DomQueryUsingXPath;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EntrezAssemblyXmlParser {

    private static final Logger LOGGER = Logger.getLogger(EntrezAssemblyXmlParser.class.getName());

    public ReferenceSequence parseXml(String xmlString, String accession, String entrezDatabase) throws Exception {
        boolean isAssembly = entrezDatabase.equals("assembly");
        String xmlPath = isAssembly ? "/eSummaryResult/DocumentSummarySet/DocumentSummary/" : "/eSummaryResult/DocSum/";
        String nameXmlPath = isAssembly ? "AssemblyName" : "Item[@Name=\"Title\"]";
        String taxIdXmlPath = isAssembly ? "SpeciesTaxid" : "Item[@Name=\"TaxId\"]";

        try {
            DomQueryUsingXPath domQueryUsingXPath = new DomQueryUsingXPath(xmlString);
            StringBuilder referenceSequenceName = new StringBuilder(
                    domQueryUsingXPath.findInDom(xmlPath + nameXmlPath));
            String patch = getPatch(referenceSequenceName);

            // Detect reference sequence type
            ReferenceSequence.Type referenceSequenceType;
            if (entrezDatabase.equals("assembly")) {
                referenceSequenceType = ReferenceSequence.Type.GENOME_ASSEMBLY;
            } else if (referenceSequenceName.toString().startsWith("TSA: ")) {
                referenceSequenceType = ReferenceSequence.Type.TRANSCRIPTOME_SHOTGUN_ASSEMBLY;
            } else {
                referenceSequenceType = ReferenceSequence.Type.SEQUENCE;
            }

            // Create new reference sequence
            ReferenceSequence referenceSequence = new ReferenceSequence(
                    referenceSequenceName.toString(), patch, accession, referenceSequenceType);

            // Create new taxonomy
            long taxonomyId = Long.parseLong(domQueryUsingXPath.findInDom(xmlPath + taxIdXmlPath));
            // TODO: NCBI provides species names for assemblies, but not for sequences from the `nuccore` database.
            String taxonomyName = isAssembly ? domQueryUsingXPath.findInDom(xmlPath + "SpeciesName") : "no name";
            Taxonomy taxonomy = new Taxonomy(taxonomyId);
            referenceSequence.setTaxonomy(taxonomy);
            return referenceSequence;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while parsing XML for accession " + accession);
            throw e;
        }
    }

    private static String getPatch(StringBuilder refName) {
        String refNameStr = refName.toString();
        String patch = null;
        // Attempt to extract patch from refName (only for GRC human or mouse assembly names)
        if (refNameStr.matches("^GRC[hm]\\d+\\.p\\d+$")) {
            String[] refNameSplit = refNameStr.split("\\.", 2);
            refName.replace(refName.indexOf("."), refName.length(), "");
            patch = refNameSplit[1];
        }
        return patch;
    }

}
