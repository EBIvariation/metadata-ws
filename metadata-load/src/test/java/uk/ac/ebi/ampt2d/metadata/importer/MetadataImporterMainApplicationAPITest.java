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
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = "import.source=API")
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
@EnableRetry
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

    @Autowired
    private FileRepository fileRepository;

    @Before
    public void setUp() {
        analysisRepository.deleteAll();
        fileRepository.deleteAll();
        studyRepository.deleteAll();
        sampleRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        sampleRepository.deleteAll();
    }

    @Test
    public void run() throws Exception {
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=study/StudyAccessions.txt"}));
        assertEquals(2, studyRepository.count());
        assertEquals(2, analysisRepository.count());
        assertEquals(21, referenceSequenceRepository.count());
        assertEquals(25, sampleRepository.count());
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

    @Test
    public void testInvalidAnalysisOfAStudyStopsWholeTreeFromImporting() throws Exception {
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=study/StudyAccessionsWithInvalidAnalysis.txt"}));
        assertEquals(0, studyRepository.count());
        assertEquals(0, analysisRepository.count());
        assertEquals(0, referenceSequenceRepository.count());
        assertEquals(0, sampleRepository.count());
        assertEquals(0, fileRepository.count());
    }

    @Test
    public void testValidAndInvalidStudy() throws Exception {
        /* The below file contains two studies ERP000054,ERP009613 but only one study(ERP009613) and its dependent tree
        is imported as the other study contains a invalid Analysis */
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=study/ValidAndInvalidStudyAccessions.txt"}));
        assertEquals(1, studyRepository.count());
        assertEquals(2, analysisRepository.count());
        assertEquals(24, referenceSequenceRepository.count());
        assertEquals(2, sampleRepository.count());
        assertEquals(10, fileRepository.count());
    }
}
