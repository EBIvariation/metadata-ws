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
import uk.ac.ebi.ampt2d.metadata.database.JdbcConnection;
import uk.ac.ebi.ampt2d.metadata.database.JdbcOperation;
import uk.ac.ebi.ampt2d.metadata.parser.AnalysisFileTypeFromSet;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.List;
import java.util.stream.Collectors;

public class EnaObjectCollect {

    private static final Logger logger = LoggerFactory.getLogger(EnaObjectCollect.class);

    public List<AnalysisFileType> getEnaAnalysisFileFomDb(JdbcConnection jdbcConnection) throws SQLException {
        JdbcOperation jdbcOperation = new JdbcOperation(jdbcConnection);
        List<SQLXML> sqlxmlList;
        List<List<AnalysisFileType>> analysisFileMultiList;

        try {
            sqlxmlList = jdbcOperation.getAnalysislXml();
        } catch (SQLException e) {
            logger.error("Unable to get ENA object AnalysisFileType", e);
            throw e;
        }

        AnalysisFileTypeFromSet analysisFileTypeFromSet = new AnalysisFileTypeFromSet();
        analysisFileMultiList = sqlxmlList.stream()
                .map(xml -> analysisFileTypeFromSet
                        .extractFromSqlXml(xml))
                .collect(Collectors.toList());
        return analysisFileMultiList
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}
