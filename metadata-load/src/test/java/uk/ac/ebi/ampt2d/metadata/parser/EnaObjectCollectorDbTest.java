/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata.parser;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.category.OracleDb;
import uk.ac.ebi.ampt2d.metadata.database.DatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.enaobject.EnaObjectCollector;
import uk.ac.ebi.ampt2d.metadata.service.EnaDbService;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
@ContextConfiguration(classes = {DatabaseConfiguration.class, EnaDbService.class, EnaObjectCollector.class})
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class EnaObjectCollectorDbTest {

    @Autowired
    EnaObjectCollector enaObjectCollector;

    @Test
    @Category(OracleDb.class)
    public void testGetEnaAnalysisFileTypeFromDb() {
        /*
         * This test retrieves Analysis Files from DB.
         * As the DB content prediction is not possible only size is verified.
         */
        List<AnalysisFileType> analysisFileTypeList = enaObjectCollector.getEnaAnalysisFileTypeFromDb();
        assertTrue(analysisFileTypeList.size() > 0);
    }

}