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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.database.DatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.enaobject.EnaObjectCollector;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
@ContextConfiguration(classes = {DatabaseConfiguration.class})
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class EnaObjectCollectorDbTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testGetEnaAnalysisFileTypeFromDb() {
        /*
         * This test retrieves all the Analysis Files from the DB pointed with credentials in application.properties.
         * As the DB content prediction is not possible only size is verified having at least one element.
         * Also before running the test SQL query limited only to few rows can be utilised to minimize time.
         * Ex: Change SQL_ANALYSIS in EnaDbService.java to
         * private static final String SQL_ANALYSIS = "SELECT ANALYSIS_XML FROM ERA.ANALYSIS WHERE ROWNUM <= 5";
         */
        EnaObjectCollector enaObjectCollect = new EnaObjectCollector();
        List<AnalysisFileType> analysisFileTypeList = enaObjectCollect.getEnaAnalysisFileTypeFromDb(jdbcTemplate);
        assertTrue(analysisFileTypeList.size() > 0);
    }

}