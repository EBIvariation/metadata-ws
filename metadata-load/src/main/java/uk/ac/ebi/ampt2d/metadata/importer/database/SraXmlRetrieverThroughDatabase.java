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
import java.util.HashMap;
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

    /*
    In DB requests ObjectImporter.java->importAnalysis()->importSamples()->importSample() will invoke this method
    to retrieve Sample XMLs by providing an "Analysis accession".
    Prepared statement will be made with SQL query EnaObjectQuery.SAMPLE_QUERY.
    A list of Sample XMLs with associated Sample accession and BioSample ID will be returned.
    */
    List<String[]> getSampleXmls(String analysisAccession) {
        Map<String, String> paramMap = new HashMap<>();
        String samplesQuery = enaObjectQuery;
        paramMap.put("accession", analysisAccession);
        List<String[]> sampleData = new ArrayList<>();
        try {
            List<Map<String, Object>> idSqlxmls = jdbcTemplate.queryForList(samplesQuery, paramMap);
            for (Map<String, Object> xmlId : idSqlxmls) {
                String sampleId = (String) xmlId.get("SAMPLE_ID");
                String bioSampleAccession = (String) xmlId.get("BIOSAMPLE_ID");
                String sqlXmlString = ((SQLXML) xmlId.get("SAMPLE_XML")).getString();
                String[] currentSampleData = new String[] {sampleId, bioSampleAccession, sqlXmlString};
                sampleData.add(currentSampleData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return sampleData;
    }

    void setEnaObjectQuery(String enaObjectQuery) {
        this.enaObjectQuery = enaObjectQuery;
    }

}
