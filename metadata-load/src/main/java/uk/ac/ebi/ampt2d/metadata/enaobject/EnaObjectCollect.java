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
package uk.ac.ebi.ampt2d.metadata.enaobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ampt2d.metadata.database.SqlxmlJdbcTemplate;
import uk.ac.ebi.ampt2d.metadata.parser.AnalysisFileTypeFromXml;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnaObjectCollect {
    AnalysisFileTypeFromXml analysisFileTypeFromXml = new AnalysisFileTypeFromXml();
    private static final Logger logger = LoggerFactory.getLogger(EnaObjectCollect.class);

    private List<AnalysisFileType> getAnalysisFile(SQLXML sqlxml) {
        List<AnalysisFileType> analysisFileTypeList = new ArrayList<>();
        try {
            analysisFileTypeList = analysisFileTypeFromXml.extractFromSqlXml(sqlxml.getString());
        } catch (SQLException e) {
            logger.error("Unable to convert SQLXML Object to String: {}", sqlxml.toString(), e);
        }
        return analysisFileTypeList;
    }

    public List<AnalysisFileType> getEnaAnalysisFileFromDb(DataSource dataSource) {
        List<SQLXML> sqlxmlList;
        String sqlAnalysis = "SELECT ANALYSIS_XML FROM ERA.ANALYSIS";
        String columnAnalysisXml = "ANALYSIS_XML";

        SqlxmlJdbcTemplate sqlxmlJdbcTemplate = new SqlxmlJdbcTemplate(dataSource,
                sqlAnalysis, columnAnalysisXml);
        sqlxmlList = sqlxmlJdbcTemplate.listSqlxml();

        return sqlxmlList.stream()
                .map(this::getAnalysisFile)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
