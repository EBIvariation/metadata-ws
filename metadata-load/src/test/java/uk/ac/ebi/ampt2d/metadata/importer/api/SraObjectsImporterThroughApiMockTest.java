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

package uk.ac.ebi.ampt2d.metadata.importer.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=API"})
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SraObjectsImporterThroughApiMockTest {

    private static final String ANALYSIS_DOCUMENT_API_XML = "analysis/AnalysisWithTranscriptome.xml";

    @InjectMocks
    @Autowired
    private SraObjectsImporterThroughApi sraObjectImporter;

    @Mock
    private SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    @Test
    public void importAnalysisObject() throws Exception {
        String analysisAccession = "ERZ496533";
        when(sraXmlRetrieverByAccession.getXml(analysisAccession)).thenReturn(new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(ANALYSIS_DOCUMENT_API_XML).toURI()))));
        Analysis analysis = sraObjectImporter.importAnalysis(analysisAccession);
        assertEquals("ERZ496533", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.EXOME_SEQUENCING, analysis.getTechnology());
        assertEquals(2, analysis.getFiles().size());
        List<ReferenceSequence> referenceSequences = analysis.getReferenceSequences();
        assertEquals(2, referenceSequences.size());
        ReferenceSequence referenceSequence = referenceSequences.get(0);
        assertEquals("GCA_000002305.1", referenceSequence.getAccessions().get(0));
        assertEquals("EquCab2.0", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.GENOME_ASSEMBLY, referenceSequence.getType());
        referenceSequence = referenceSequences.get(1);
        assertEquals("GBRU01000000", referenceSequence.getAccessions().get(0));
        assertEquals("Petunia axillaris, TSA project GBRU01000000 data", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.TRANSCRIPTOME_SHOTGUN_ASSEMBLY, referenceSequence.getType());
    }

}
