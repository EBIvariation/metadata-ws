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

import java.util.ArrayList;
import java.util.List;

public class AnalysisFileTypeFromXml implements TypeFromXml<AnalysisFileType, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisFileTypeFromXml.class);

    @Override
    public List<AnalysisFileType> extractFromXml(String xmlStr) throws XmlException {
        List<AnalysisFileType> analysisFileList;
        analysisFileList = extractAnalysisFilesFromAnalysis(getAnalysisSet(xmlStr));
        return analysisFileList;
    }

    private List<AnalysisFileType> extractAnalysisFilesFromAnalysis(AnalysisSetType analysisSet) {
        List<AnalysisFileType> subAnalysisFileList = new ArrayList<>();

        AnalysisType[] analysesType = analysisSet.getANALYSISArray();
        for (AnalysisType analysis : analysesType) {
            AnalysisType.FILES analysisFILES = analysis.getFILES();
            AnalysisFileType[] analysesFile = analysisFILES.getFILEArray();
            for (AnalysisFileType element : analysesFile) {
                subAnalysisFileList.add(element);
            }
        }
        return subAnalysisFileList;
    }

    private AnalysisSetType getAnalysisSet(String xmlStr) throws XmlException {
        AnalysisSetType analysisSetType;

        try {
            analysisSetType = ANALYSISSETDocument.Factory.parse(xmlStr).getANALYSISSET();
        } catch (XmlException e) {
            LOGGER.error("Unable to parse the XML file: {}",  xmlStr, e);
            throw e;
        }
        return analysisSetType;
    }

}
