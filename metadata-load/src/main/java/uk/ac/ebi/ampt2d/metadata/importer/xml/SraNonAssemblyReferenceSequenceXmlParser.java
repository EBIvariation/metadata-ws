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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SraNonAssemblyReferenceSequenceXmlParser extends SraXmlParser<ReferenceSequence> {

    private static final Logger LOGGER = Logger.getLogger(SraNonAssemblyReferenceSequenceXmlParser.class.getName());

    private static final String TSA = "Transcriptome Shotgun Assembly";

    private static final String ENTRY_PATH = "/ROOT/entry";

    private static final String SLASH = "/";

    @Override
    public ReferenceSequence parseXml(String xmlString, String accession) throws XmlException {
        try {
            DomQueryUsingXPath domQueryUsingXPath = new DomQueryUsingXPath(xmlString);
            String referenceSequenceAccession = domQueryUsingXPath.findInDom(ENTRY_PATH + SLASH + "@accession");
            String referenceSequenceName = domQueryUsingXPath.findInDom(ENTRY_PATH + SLASH + "description");
            ReferenceSequence.Type referenceSequenceType = ReferenceSequence.Type.SEQUENCE;
            if (domQueryUsingXPath.isExpressionExists(ENTRY_PATH + "[keyword='" + TSA + "']")) {
                referenceSequenceType = ReferenceSequence.Type.TRANSCRIPTOME_SHOTGUN_ASSEMBLY;
            }
            ReferenceSequence referenceSequence = new ReferenceSequence(referenceSequenceName, null, Arrays.asList
                    (referenceSequenceAccession), referenceSequenceType);
            String taxonomyName = domQueryUsingXPath.findInDom(ENTRY_PATH + SLASH + "feature/taxon/@scientificName");
            long taxonomyId = Long.parseLong(domQueryUsingXPath.findInDom(ENTRY_PATH + SLASH +
                    "feature/taxon/@taxId"));
            Taxonomy taxonomy = new Taxonomy(taxonomyId, taxonomyName, "no rank");
            referenceSequence.setTaxonomy(taxonomy);
            return referenceSequence;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while parsing XML for accession " + accession);
            throw new XmlException(e.getMessage(), e);
        }
    }
}
