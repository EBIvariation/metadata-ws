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

package uk.ac.ebi.ampt2d.metadata.pipeline.loader.extractor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.pipeline.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraXmlParser;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence.Type.ASSEMBLY;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MetadataDatabaseConfiguration.class)
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties")
public class ReferenceSequenceExtractorFromAnalysisTest {

    private static final String ANALYSIS_ACCESSION = "ERZ496533";

    private static final String ANALYSIS_DOCUMENT_API_XML = "AnalysisDocumentApi.xml";

    private static final String ANALYSIS_DOCUMENT_DATABASE_XML = "AnalysisDocumentDatabase1.xml";

    private SraXmlParser<AnalysisType> xmlParser;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    private ReferenceSequenceExtractorFromAnalysis referenceSequenceExtractorFromAnalysis;

    @Before
    public void setUp() {
        xmlParser = new SraAnalysisXmlParser();
        referenceSequenceExtractorFromAnalysis = new ReferenceSequenceExtractorFromAnalysis(referenceSequenceRepository);
    }

    @Test
    public void testReferenceSequenceExtractApi() throws Exception {
        List<ReferenceSequence> referenceSequences = referenceSequenceExtractorFromAnalysis
                .getReferenceType(getAnalysisType(ANALYSIS_DOCUMENT_API_XML));

        Assert.assertNotNull(referenceSequences);
        Assert.assertEquals(1, referenceSequences.size());
        Assert.assertEquals(1, referenceSequences.get(0).getAccessions().size());
        Assert.assertEquals("GCA_000002305.1", referenceSequences.get(0).getAccessions().get(0));
        Assert.assertEquals("GCA_000002305.1", referenceSequences.get(0).getName());
        Assert.assertEquals(ASSEMBLY, referenceSequences.get(0).getType());
    }

    @Test
    public void testReferenceSequenceExtractDB() throws Exception {
        List<ReferenceSequence> referenceSequences = referenceSequenceExtractorFromAnalysis
                .getReferenceType(getAnalysisType(ANALYSIS_DOCUMENT_DATABASE_XML));

        Assert.assertNotNull(referenceSequences);
        Assert.assertEquals(1, referenceSequences.size());
        Assert.assertEquals("GRCh37", referenceSequences.get(0).getName());
        Assert.assertEquals(ASSEMBLY, referenceSequences.get(0).getType());
    }

    private AnalysisType getAnalysisType(String xml) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, ANALYSIS_ACCESSION);
    }
}

