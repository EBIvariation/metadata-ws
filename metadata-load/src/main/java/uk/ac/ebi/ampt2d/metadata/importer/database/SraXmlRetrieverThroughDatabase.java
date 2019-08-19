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

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SraXmlRetrieverThroughDatabase implements SraXmlRetrieverByAccession {

    private NamedParameterJdbcTemplate jdbcTemplate;

    private String enaObjectQuery;

    public SraXmlRetrieverThroughDatabase(NamedParameterJdbcTemplate jdbcTemplate, String enaObjectQuery) {
        this.jdbcTemplate = jdbcTemplate;
        this.enaObjectQuery = enaObjectQuery;
    }

    @Override
    public String getXml(String accession) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("accession", accession);
        SQLXML sqlxml = jdbcTemplate.queryForObject(enaObjectQuery, parameters, SQLXML.class);
        try {
            return sqlxml.getString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getXmlList(List<String> accessionList) {
        Map<String, List> paramMap = Collections.singletonMap("accession", accessionList);
        List<SQLXML> sqlxmlList = jdbcTemplate.queryForList(enaObjectQuery, paramMap, SQLXML.class);
        try {
            List<String> sqlxmlStringList = new ArrayList<>();
            for (SQLXML xml : sqlxmlList) {
                sqlxmlStringList.add(xml.getString());
            }
            return sqlxmlStringList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEnaObjectQuery(String enaObjectQuery) {
        this.enaObjectQuery = enaObjectQuery;
    }
}
