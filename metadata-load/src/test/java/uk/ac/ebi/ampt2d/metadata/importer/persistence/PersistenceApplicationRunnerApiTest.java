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

package uk.ac.ebi.ampt2d.metadata.importer.persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

@RunWith(SpringRunner.class)
public class PersistenceApplicationRunnerApiTest {
    private final static int NUMBER_OF_APPLICATION_ARGUMENTS = 3;

    private SpringApplication springApplication = new SpringApplication(MetadataImporterMainApplication.class);

    private String[] applicationArguments = new String[NUMBER_OF_APPLICATION_ARGUMENTS];

    @Before
    public void setUp() {
        applicationArguments[0] = "--accessions.file.path=analysis/analysisAccessions.txt";
        applicationArguments[1] = "--import.object=analysis";
        applicationArguments[2] = "--import.source=API";
    }

    @Test
    public void testRun() throws Exception {
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        AnalysisRepository analysisRepository = (AnalysisRepository) getBean(configurableApplicationContext,
                "analysisRepository");
        Assert.assertEquals(2, analysisRepository.count());
        FileRepository fileRepository = (FileRepository) getBean(configurableApplicationContext,
                "fileRepository");
        Assert.assertEquals(4, fileRepository.count());
        ReferenceSequenceRepository referenceSequenceRepository =
                (ReferenceSequenceRepository) getBean(configurableApplicationContext, "referenceSequenceRepository");
        Assert.assertEquals(1, referenceSequenceRepository.count());
        SampleRepository sampleRepository =
                (SampleRepository) getBean(configurableApplicationContext, "sampleRepository");
        Assert.assertEquals(2, sampleRepository.count());
        StudyRepository studyRepository =
                (StudyRepository) getBean(configurableApplicationContext, "studyRepository");
        Assert.assertEquals(2, studyRepository.count());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidFilePath() throws Exception {
        applicationArguments[0] = "--accessions.file.path=InvalidFilePath/analysisAccessions.txt";
        springApplication.run(applicationArguments);
    }

    @Test
    public void testWithInvalidAndValidAnalysisAccession() throws Exception {
        applicationArguments[0] = "--accessions.file.path=analysis/invalidAndValidAnalysisAccession.txt";

        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        AnalysisRepository analysisRepository = (AnalysisRepository) getBean(configurableApplicationContext,
                "analysisRepository");
        Assert.assertEquals(1, analysisRepository.count());
        FileRepository fileRepository = (FileRepository) getBean(configurableApplicationContext,
                "fileRepository");
        Assert.assertEquals(2, fileRepository.count());
        ReferenceSequenceRepository referenceSequenceRepository =
                (ReferenceSequenceRepository) getBean(configurableApplicationContext, "referenceSequenceRepository");
        Assert.assertEquals(1, referenceSequenceRepository.count());
    }

    @Test
    public void testWithDuplicateAnalysisAccession() throws Exception {
        applicationArguments[0] = "--accessions.file.path=analysis/duplicateAnalysisAccession.txt";
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        AnalysisRepository analysisRepository = (AnalysisRepository) getBean(configurableApplicationContext,
                "analysisRepository");
        Assert.assertEquals(1, analysisRepository.count());
        FileRepository fileRepository = (FileRepository) getBean(configurableApplicationContext,
                "fileRepository");
        Assert.assertEquals(2, fileRepository.count());
        ReferenceSequenceRepository referenceSequenceRepository =
                (ReferenceSequenceRepository) getBean(configurableApplicationContext, "referenceSequenceRepository");
        Assert.assertEquals(0, referenceSequenceRepository.count());
    }

    @Test
    public void testRunStudy() throws Exception {
        applicationArguments[0] = "--accessions.file.path=study/studyAccessions.txt";
        applicationArguments[1] = "--import.object=study";
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        StudyRepository studyRepository = (StudyRepository) getBean(configurableApplicationContext,
                "studyRepository");
        Assert.assertEquals(2, studyRepository.count());
    }

    private Object getBean(ConfigurableApplicationContext configurableApplicationContext, String bean) {
        return configurableApplicationContext.getBeanFactory().getBean(bean);
    }

}