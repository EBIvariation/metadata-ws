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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLXML;
import java.util.List;
import java.util.Map;

@Service
public class EnaDbService {

    private static final String SQL_ANALYSIS = "SELECT ANALYSIS_XML FROM ERA.ANALYSIS WHERE ROWNUM <= :rowNum";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${queryRows}")
    private Integer queryRows;

    public List<SQLXML> getEnaAnalysisXml() {
        List<SQLXML> sqlxmlList;

        Long totalRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ERA.ANALYSIS", (Map)null, Long.class);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (queryRows > 0) {
            parameters.addValue("rowNum", queryRows);
        } else {
            parameters.addValue("rowNum", totalRows);
        }
        sqlxmlList = jdbcTemplate.queryForList(SQL_ANALYSIS, parameters, SQLXML.class);

        return sqlxmlList;
    }
}
