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
package uk.ac.ebi.ampt2d.metadata.importer.database;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.SraDatabaseConfiguration;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=DB"})
@ContextConfiguration(classes = {SraDatabaseConfiguration.class})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class SraObjectRetrieverThroughDatabaseTest {

    @Autowired
    private SraObjectRetrieverThroughDatabase sraRetriever;

    @Test
    @Category(OracleDbCategory.class)
    public void getXml() throws Exception {
        String studyAccession = "ERP000332";
        String studyDocumentPath = "study/studyDocumentDB";

        String xmlString = sraRetriever.getXml(studyAccession);
        String expectedXmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(studyDocumentPath).toURI())));

        assertEquals(expectedXmlString, xmlString);
    }

}