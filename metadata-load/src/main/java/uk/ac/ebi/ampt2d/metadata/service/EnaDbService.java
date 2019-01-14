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
package uk.ac.ebi.ampt2d.metadata.service;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.ampt2d.metadata.database.SqlxmlJdbcTemplate;

import java.sql.SQLXML;
import java.util.List;

public class EnaDbService {

    private static final String SQL_ANALYSIS = "SELECT ANALYSIS_XML FROM ERA.ANALYSIS";
    private static final String COLUMN_ANALYSIS_XML = "ANALYSIS_XML";
    private JdbcTemplate jdbcTemplate;

    public EnaDbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SQLXML> getEnaAnalysisXml() {
        List<SQLXML> sqlxmlList;
        SqlxmlJdbcTemplate sqlxmlJdbcTemplate = new SqlxmlJdbcTemplate(jdbcTemplate,
                SQL_ANALYSIS, COLUMN_ANALYSIS_XML);
        sqlxmlList = sqlxmlJdbcTemplate.getSqlxmlList();
        return sqlxmlList;
    }
}
