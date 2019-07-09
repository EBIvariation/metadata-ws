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

import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SraEntryXmlParser extends SraXmlParser<ReferenceSequence> {

    private static final Logger LOGGER = Logger.getLogger(SraEntryXmlParser.class.getName());

    private static final String TSA = "Transcriptome Shotgun Assembly";

    @Override
    public ReferenceSequence parseXml(String xmlString, String accession) throws XmlException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            String referenceSequenceAccession = (String) xpath.evaluate("/ROOT/entry/@accession", document,
                    XPathConstants.STRING);
            String referenceSequenceName = (String) xpath.evaluate("/ROOT/entry/description", document,
                    XPathConstants.STRING);
            ReferenceSequence.Type referenceSequenceType = ReferenceSequence.Type.GENE;
            if ((boolean) xpath.evaluate("boolean(/ROOT/entry[keyword='" + TSA + "'])",
                    document, XPathConstants.BOOLEAN)) {
                referenceSequenceType = ReferenceSequence.Type.TRANSCRIPTOME;
            }
            ReferenceSequence referenceSequence = new ReferenceSequence(referenceSequenceName, null, Arrays.asList
                    (referenceSequenceAccession), referenceSequenceType);
            String taxonomyName = (String) xpath.evaluate("/ROOT/entry/feature/taxon/@scientificName", document,
                    XPathConstants.STRING);
            long taxonomyId = Long.parseLong((String) xpath.evaluate("/ROOT/entry/feature/taxon/@taxId", document,
                    XPathConstants.STRING));
            Taxonomy taxonomy = new Taxonomy(taxonomyId, taxonomyName);
            referenceSequence.setTaxonomy(taxonomy);
            return referenceSequence;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while parsing XML for accession " + accession);
            throw new XmlException(e.getMessage(), e);
        }
    }
}
