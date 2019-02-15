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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.pipeline.MetadataPipelineMainApplication;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.database.OracleDbCategory;

@RunWith(SpringRunner.class)
public class FilePersistenceServiceSourceDBTest {

    private final static int NUMBER_OF_APPLICATION_ARGUMENTS = 3;

    private SpringApplication springApplication = new SpringApplication(MetadataPipelineMainApplication.class);

    private String[] applicationArguments = new String[NUMBER_OF_APPLICATION_ARGUMENTS];

    @Before
    public void beforeEachMethod() {
        applicationArguments[0] = "--analysisAccession.file.path=analysisAccessions.txt";
        applicationArguments[1] = "--import.object=files";
        applicationArguments[2] = "--import.source=DB";
    }

    @Test
    @Category(OracleDbCategory.class)
    public void testRunDB() throws Exception {
        ConfigurableApplicationContext configurableApplicationContext = springApplication.run(applicationArguments);
        FileRepository fileRepository = getFileRepository(configurableApplicationContext);
        Assert.assertEquals(4, fileRepository.count());
        configurableApplicationContext.close();
    }

    private FileRepository getFileRepository(ConfigurableApplicationContext configurableApplicationContext) {
        return (FileRepository) configurableApplicationContext.getBeanFactory().getBean
                ("fileRepository");
    }
}
