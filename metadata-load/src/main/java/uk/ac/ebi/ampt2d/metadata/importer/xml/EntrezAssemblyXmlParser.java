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

import org.w3c.dom.Document;
import uk.ac.ebi.ampt2d.metadata.importer.converter.ReferenceSequenceConverter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntrezAssemblyXmlParser {

    private static final Logger LOGGER = Logger.getLogger(EntrezAssemblyXmlParser.class.getName());

    private DomQueryUsingXPath domQueryUsingXPath;

    public EntrezAssemblyXmlParser(DomQueryUsingXPath domQueryUsingXPath) {
        this.domQueryUsingXPath = domQueryUsingXPath;
    }

    public ReferenceSequence parseXml(String xmlString, String accession) throws Exception {
        try {
            Document document = domQueryUsingXPath.buildDom(xmlString);
            XPath xPath = domQueryUsingXPath.getXpath();
            String documentSummary = "/eSummaryResult/DocumentSummarySet/DocumentSummary/";
            String referenceSequenceAccession = (String) xPath.evaluate(documentSummary + "AssemblyAccession",
                    document, XPathConstants.STRING);
            StringBuilder referenceSequenceName = new StringBuilder();
            referenceSequenceName.append((String) xPath.evaluate(documentSummary +
                    "AssemblyName", document, XPathConstants.STRING));
            String patch = ReferenceSequenceConverter.getPatch(referenceSequenceName);
            ReferenceSequence referenceSequence = new ReferenceSequence(referenceSequenceName.toString(), patch,
                    Arrays.asList(referenceSequenceAccession), ReferenceSequence.Type.GENOME_ASSEMBLY);
            long taxonomyId = Long.parseLong((String) xPath.evaluate(documentSummary + "Taxid", document,
                    XPathConstants.STRING));
            String taxonomyName = (String) xPath.evaluate(documentSummary + "SpeciesName", document,
                    XPathConstants.STRING);
            Taxonomy taxonomy = new Taxonomy(taxonomyId, taxonomyName);
            referenceSequence.setTaxonomy(taxonomy);
            return referenceSequence;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while parsing XML for accession " + accession);
            throw e;
        }
    }

}
