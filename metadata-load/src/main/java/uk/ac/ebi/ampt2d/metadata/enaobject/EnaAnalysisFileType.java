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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnaAnalysisFileType {

    private static final Logger logger = LoggerFactory.getLogger(EnaAnalysisFileType.class);

    public List<uk.ac.ebi.ena.sra.xml.AnalysisFileType> getEnaAnalysisFileObject(JdbcConnection jdbcConnection) {
        JdbcOperation jdbcOperation;
        String sql = "select ANALYSIS_XML from ERA.ANALYSIS";
        String column = "ANALYSIS_XML";
        List<uk.ac.ebi.ena.sra.xml.AnalysisFileType> analysisFileTypeList = new ArrayList<>();

        jdbcOperation = new JdbcOperation(jdbcConnection);
        try {
            analysisFileTypeList = jdbcOperation.getAnalysisFileType();
        } catch (SQLException e) {
            logger.error("Unable to get ENA object AnalysisFileType", e);
        }
        return analysisFileTypeList;
    }

}
