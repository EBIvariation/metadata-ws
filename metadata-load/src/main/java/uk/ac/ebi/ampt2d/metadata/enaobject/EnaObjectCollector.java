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

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.parser.AnalysisFileTypeFromXml;
import uk.ac.ebi.ampt2d.metadata.service.EnaDbService;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnaObjectCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnaObjectCollector.class);

    @Autowired
    private EnaDbService enaDbService;

    public List<AnalysisFileType> getEnaAnalysisFileTypeFromDb(int pageNumber, int pageSize) {
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid page number and/or page size");
        }

        List<SQLXML> sqlxmlList;
        List<AnalysisFileType> analysisFileTypeList;
        long rowFrom = (pageNumber * pageSize) + 1;
        long rowTo = rowFrom + pageSize - 1;

        sqlxmlList = enaDbService.getEnaAnalysisXml(rowFrom, rowTo);
        analysisFileTypeList = sqlxmlList.stream()
                .map(this::getAnalysisFileTypeList)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return analysisFileTypeList;
    }

    private List<AnalysisFileType> getAnalysisFileTypeList(SQLXML sqlxml) {
        List<AnalysisFileType> analysisFileTypeList = new ArrayList<>();
        AnalysisFileTypeFromXml analysisFileTypeFromXml = new AnalysisFileTypeFromXml();
        try {
            analysisFileTypeList = analysisFileTypeFromXml.extractFromXml(sqlxml.getString());
        } catch (SQLException e) {
            LOGGER.error("Unable to convert SQLXML Object to String: {}", sqlxml.toString(), e);
        } catch (XmlException e) {
            LOGGER.error("Unable to convert XML String to AnalysisSet: {}", sqlxml.toString(), e);
        }
        return analysisFileTypeList;
    }

}
