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

package uk.ac.ebi.ampt2d.metadata.importer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = "import.source=API")
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
public class MetadataImporterMainApplicationAPITest {

    @Autowired
    private MetadataImporterMainApplication metadataImporterMainApplication;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Before
    public void setUp() {
        analysisRepository.deleteAll();
        studyRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
    }

    @Test
    public void run() throws Exception {
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=study/StudyAccessions.txt"}));
        assertEquals(2, studyRepository.count());
        // Actual count should be 3 but ERZ000001 Analysis has a CUSTOM assembly so not imported
        assertEquals(2, analysisRepository.count());
        assertEquals(24, referenceSequenceRepository.count());
        assertEquals(2, sampleRepository.count());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidFilePath() throws Exception {
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=InvalidFilePath/StudyAccessions.txt"}));
    }

    @Test
    public void testDuplicateStudy() throws Exception {
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=study/DuplicateStudyAccessions.txt"}));
        assertEquals(1, studyRepository.count());
    }

}
