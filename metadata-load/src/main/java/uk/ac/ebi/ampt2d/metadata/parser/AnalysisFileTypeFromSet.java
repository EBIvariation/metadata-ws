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

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ampt2d.metadata.database.JdbcConnection;
import uk.ac.ebi.ena.sra.xml.ANALYSISSETDocument;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;
import uk.ac.ebi.ena.sra.xml.AnalysisSetType;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;

public class AnalysisFileTypeFromSet implements TypeFromSet<AnalysisFileType, AnalysisSetType> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisFileTypeFromSet.class);

    @Override
    public List<AnalysisFileType> extract(AnalysisSetType analysisSet) {
        List<AnalysisFileType> subAnalysisFileList = new ArrayList<>();

        AnalysisType[] analysisType = analysisSet.getANALYSISArray();
        for (AnalysisType analysis : analysisType) {
            AnalysisType.FILES analysisFILES = analysis.getFILES();
            AnalysisFileType[] analysisFile = analysisFILES.getFILEArray();
            for (AnalysisFileType anaFile : analysisFile) {
                subAnalysisFileList.add(anaFile);
            }
        }
        return subAnalysisFileList;
    }

    public AnalysisSetType getAnalysisSet(String xmlStr) throws XmlException {
        AnalysisSetType analysisSetType = null;
        try {
            analysisSetType = ANALYSISSETDocument.Factory.parse(xmlStr).getANALYSISSET();
        } catch (XmlException e) {
            logger.warn("Unable to parse the XML file: {}",  xmlStr, e);
            throw e;
        }
        return  analysisSetType;
    }

    public List<AnalysisFileType> extractFromSqlXml(SQLXML sqlXml) {
        String xmlStr;
        List<AnalysisFileType> analysisFileList = new ArrayList<>();

        try {
            xmlStr = sqlXml.getString();
            AnalysisSetType analysisSet;
            analysisSet = getAnalysisSet(xmlStr);
            analysisFileList = extract(analysisSet);
        } catch (XmlException e) {
            logger.warn("Unable to convert XML String to AnalysisSet:",  e);
        } catch (SQLException e) {
            logger.warn("Unable to convert SQLXML Object to String: {}", sqlXml.toString(), e);
        }
        return analysisFileList;
    }

    private String createCompleteUrl(JdbcConnection jdbcConnection) {
        String url = null;
        String host = null;
        String protocol = null;
        String serviceName = null;
        String port = null;
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

    public List<AnalysisFileType> getAnalysisFileObject(JdbcConnection jdbcConnection) throws IOException, SQLException {
        String prepSql = "select ANALYSIS_XML from ERA.ANALYSIS";
        Connection dbConnection = null;
        PreparedStatement prepStatement = null;
        ResultSet resultSet = null;
        SQLXML sqlXml;
        List<AnalysisFileType> analysisFileList = new ArrayList<>();
        String userName = null;
        String password = null;

        String completeUrl = createCompleteUrl(jdbcConnection);
        if ((jdbcConnection.getUserName() == null || jdbcConnection.getUserName().trim().isEmpty()) ||
                (jdbcConnection.getPassword() == null || jdbcConnection.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("Some fields are not present in properties file.");
        } else {
            userName = jdbcConnection.getUserName();
            password = jdbcConnection.getPassword();
        }

        try {
            dbConnection = DriverManager.getConnection(completeUrl, userName, password);
            prepStatement = dbConnection.prepareStatement(prepSql);
            resultSet = prepStatement.executeQuery();
            while (resultSet.next()) {
                sqlXml =  resultSet.getSQLXML("ANALYSIS_XML");
                List<AnalysisFileType> subAnalysisFileList = extractFromSqlXml(sqlXml);
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
