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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ena.sra.xml.ANALYSISSETDocument;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLXML;
import java.util.Properties;

public class AnalysisFileTypeFromAnalysisSetDocument implements TypeFromDocument<AnalysisFileType, ANALYSISSETDocument> {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisFileTypeFromAnalysisSetDocument.class);
    private static final String propertiesFile = "jdbc.properties";

    @Override
    public AnalysisFileType extract(ANALYSISSETDocument superSet) {
        return null;
    }

    public Properties loadPropertiesFile() throws IOException {
        Properties properties = new Properties();
        InputStream inFile = null;
        try {
            inFile = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            logger.info("JDBC connection properties file not found: " + propertiesFile);
            throw e;
        }
        try {
            properties.load(inFile);
        } catch (IOException e) {
            throw e;
        }
        inFile.close();
        return properties;
    }

    private SQLXML getSqlXml() {
        Connection dbConnection = null;
        PreparedStatement prepStatement = null;
        ResultSet resultSet = null;
        SQLXML sqlXml=null;

        return null;
    }
}
