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

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;

public class AnalysisFileTypeFromXmlFile implements TypeFromXmlFile<AnalysisFileType, SQLXML> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisFileTypeFromXmlFile.class);

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
        AnalysisSetType analysisSetType;

        try {
            analysisSetType = ANALYSISSETDocument.Factory.parse(xmlStr).getANALYSISSET();
        } catch (XmlException e) {
            logger.error("Unable to parse the XML file: {}",  xmlStr, e);
            throw e;
        }
        return analysisSetType;
    }

    @Override
    public List<AnalysisFileType> extractFromSqlXml(SQLXML sqlXml) {
        String xmlStr;
        List<AnalysisFileType> analysisFileList = new ArrayList<>();

        try {
            xmlStr = sqlXml.getString();
            AnalysisSetType analysisSet;
            analysisSet = getAnalysisSet(xmlStr);
            analysisFileList = extract(analysisSet);
        } catch (XmlException e) {
            logger.error("Unable to convert XML String to AnalysisSet:",  e);
        } catch (SQLException e) {
            logger.error("Unable to convert SQLXML Object to String: {}", sqlXml.toString(), e);
        }
        return analysisFileList;
    }

}
