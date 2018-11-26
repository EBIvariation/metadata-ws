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
import uk.ac.ebi.ena.sra.xml.ANALYSISSETDocument;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;
import uk.ac.ebi.ena.sra.xml.AnalysisSetType;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AnalysisFileTypeFromSet implements TypeFromSet<AnalysisFileType, AnalysisSetType> {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisFileTypeFromSet.class);
    private static final String propertiesFile = "application.properties";

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
        return ANALYSISSETDocument.Factory.parse(xmlStr).getANALYSISSET();
    }

    public List<AnalysisFileType> extractFromSqlXml(SQLXML sqlXml) {
        String xmlStr;
        List<AnalysisFileType> analysisFileList = new ArrayList<>();

        try {
            xmlStr = sqlXml.getString();
            AnalysisSetType analysisSet;
            try {
                analysisSet = getAnalysisSet(xmlStr);
                analysisFileList = extract(analysisSet);
            } catch (XmlException e) {
                logger.info("Unable to convert XML String to AnalysisSet: ", xmlStr);
            }
        } catch (SQLException e) {
            logger.info("Unable to convert SQLXML Object to String: ", sqlXml.toString());
        }
        return analysisFileList;
    }

    private Properties loadPropertiesFile() throws IOException {
        Properties properties = new Properties();
        InputStream inFile = null;
        try {
            inFile = new FileInputStream(propertiesFile);
            try {
                properties.load(inFile);
            } catch (IOException e) {
                logger.info("Unable to load JDBC connection properties file");
                throw e;
            }
        } catch (FileNotFoundException e) {
            logger.info("JDBC connection properties file not found: " + propertiesFile);
            throw e;
        } finally {
            if (inFile != null) {
                inFile.close();
            }
        }
        return properties;
    }

    private String createUrl(Properties appProperties) {
        String url = appProperties.getProperty("OracleJDBC.url");
        String host = appProperties.getProperty("OracleJDBC.host");
        String protocol = appProperties.getProperty("OracleJDBC.protocol");
        String serviceName = appProperties.getProperty("OracleJDBC.servicename");
        String port = appProperties.getProperty("OracleJDBC.port");
        url = url + ":@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = " + protocol + ")(HOST = " + host + ")(PORT = " +
                port + ")))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = " + serviceName + ")))";

        return url;
    }

    public List<AnalysisFileType> getAnalysisFileObject() throws IOException, SQLException {
        String prepSql = "select ANALYSIS_XML from ERA.ANALYSIS";
        Connection dbConnection = null;
        PreparedStatement prepStatement = null;
        ResultSet resultSet = null;
        SQLXML sqlXml;
        List<AnalysisFileType> analysisFileList = new ArrayList<>();

        Properties appProperties = loadPropertiesFile();
        String url = createUrl(appProperties);
        String username = appProperties.getProperty("OracleJDBC.username");
        String password = appProperties.getProperty("OracleJDBC.password");

        try {
            dbConnection = DriverManager.getConnection(url, username, password);
            prepStatement = dbConnection.prepareStatement(prepSql);
            resultSet = prepStatement.executeQuery();
            while (resultSet.next()) {
                sqlXml =  resultSet.getSQLXML("ANALYSIS_XML");
                List<AnalysisFileType> subAnalysisFileList = extractFromSqlXml(sqlXml);
                analysisFileList.addAll(subAnalysisFileList);

            }
        } catch (SQLException e) {
            logger.info("Unable to establish JDBC connection with Oracle server");
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
