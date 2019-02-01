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

package uk.ac.ebi.ampt2d.metadata.pipeline.persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.pipeline.PipelineMainApplication;
import uk.ac.ebi.ampt2d.metadata.pipeline.configuration.PipelineConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PipelineConfiguration.class)
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class})
public class FilePersistenceServiceTest {

    @Autowired
    private FileRepository fileRepository;

    private SpringApplication springApplication = new SpringApplication(PipelineMainApplication.class);

    private String[] applicationArguments = new String[4];

    @Before
    public void beforeEachMethod() {
        applicationArguments[0] = "--analysisAccession.file.path=analysisAccessions.txt";
        applicationArguments[1] = "--import.object=files";
        applicationArguments[2] = "--import.source=API";
        applicationArguments[3] = "--spring.jpa.hibernate.ddl-auto=create";
    }

    @Test
    public void testRun() throws Exception {
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        Assert.assertEquals(4, fileRepository.count());
        configurableApplicationContext.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidFilePath() throws Exception {
        applicationArguments[0] = "--analysisAccession.file.path=InvalidFilePath/analysisAccessions.txt";
        springApplication.run(applicationArguments);
    }

    @Test
    public void testWithNoFileProvided() throws Exception {
        applicationArguments[0] = "";
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        Assert.assertEquals(0, fileRepository.count());
        configurableApplicationContext.close();
    }

    @Test(expected = UnsatisfiedDependencyException.class)
    public void testWithNoImportSourceProvided() throws Exception {
        applicationArguments[2] = "";
        springApplication.run(applicationArguments);
    }

    @Test
    public void testWithInvalidAndValidAnalysisAccession() throws Exception {
        applicationArguments[0] = "--analysisAccession.file.path=InvalidAndValidAnalysisAccession.txt";
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        Assert.assertEquals(2, fileRepository.count());
        configurableApplicationContext.close();
    }

    @Test
    public void testWithDuplicateFiles() throws Exception {
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        Assert.assertEquals(4, fileRepository.count());
        configurableApplicationContext.close();
        applicationArguments[3] = "--spring.jpa.hibernate.ddl-auto=none";
        configurableApplicationContext = springApplication.run(applicationArguments);
        Assert.assertEquals(4, fileRepository.count());
        configurableApplicationContext.close();

    }
}
