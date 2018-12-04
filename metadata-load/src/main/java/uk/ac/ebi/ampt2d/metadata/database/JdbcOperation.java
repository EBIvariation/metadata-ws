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
package uk.ac.ebi.ampt2d.metadata.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ampt2d.metadata.parser.AnalysisFileTypeFromSet;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;

public class JdbcOperation {

    private static final Logger logger = LoggerFactory.getLogger(JdbcOperation.class);

    private JdbcConnection jdbcConnection;

    public JdbcOperation(JdbcConnection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    private String createCompleteUrl(JdbcConnection jdbcConnection) {
        String url;
        String host;
        String protocol;
        String serviceName;
        String port;
        if ((jdbcConnection.getUrl() == null || jdbcConnection.getUrl().trim().isEmpty()) ||
                (jdbcConnection.getHost() == null || jdbcConnection.getHost().trim().isEmpty()) ||
                (jdbcConnection.getPort() == null || jdbcConnection.getPort().trim().isEmpty()) ||
                (jdbcConnection.getProtocol() == null || jdbcConnection.getProtocol().trim().isEmpty()) ||
                (jdbcConnection.getServiceName() == null || jdbcConnection.getServiceName().trim().isEmpty())) {
            throw new IllegalArgumentException("Some fields are not present in properties file.");
        } else {
            url = jdbcConnection.getUrl();
            host = jdbcConnection.getHost();
            protocol = jdbcConnection.getProtocol();
            serviceName = jdbcConnection.getServiceName();
            port = jdbcConnection.getPort();
        }

        url = url + ":@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = " + protocol + ")(HOST = " + host + ")(PORT = " +
                port + ")))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = " + serviceName + ")))";

        return url;
    }


    public List<AnalysisFileType> getAnalysisFileType() throws SQLException {
        Connection dbConnection = null;
        PreparedStatement prepStatement = null;
        ResultSet resultSet = null;
        SQLXML sqlXml;
        List<AnalysisFileType> analysisFileList = new ArrayList<>();

        String completeUrl = createCompleteUrl(jdbcConnection);
        String userName;
        String password;
        if ((jdbcConnection.getUserName() == null || jdbcConnection.getUserName().trim().isEmpty()) ||
                (jdbcConnection.getPassword() == null || jdbcConnection.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Some fields are not present in properties file.");
        } else {
            userName = jdbcConnection.getUserName();
            password = jdbcConnection.getPassword();
        }

        try {
            String prepSql = "select ANALYSIS_XML from ERA.ANALYSIS";
            dbConnection = DriverManager.getConnection(completeUrl, userName, password);
            prepStatement = dbConnection.prepareStatement(prepSql);
            resultSet = prepStatement.executeQuery();
            AnalysisFileTypeFromSet analysisFileTypeFromSet = new AnalysisFileTypeFromSet();
            List<AnalysisFileType> subAnalysisFileList;
            while (resultSet.next()) {
                sqlXml =  resultSet.getSQLXML("ANALYSIS_XML");
                subAnalysisFileList = analysisFileTypeFromSet.extractFromSqlXml(sqlXml);
                analysisFileList.addAll(subAnalysisFileList);
            }
        } catch (SQLException e) {
            logger.error("Unable to establish JDBC connection with Oracle server", e);
            throw e;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (prepStatement != null) {
                prepStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
        return analysisFileList;
    }

}
