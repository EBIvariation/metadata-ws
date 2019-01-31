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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ena.analysis.query.pageFrom}")
    private long pageFrom;

    @Value("${ena.analysis.query.pageSize}")
    private long pageSize;

    @Value("${ena.analysis.query.totalPages}")
    private long totalPages;

    @Autowired
    EnaDbService enaDbService;

    public void initialize() {
        pageFrom = pageSize = totalPages = 1;
    }

    public List<AnalysisFileType> getEnaAnalysisFileTypeFromDb() {
        List<SQLXML> sqlxmlList;
        List<AnalysisFileType> subList;
        List<AnalysisFileType> analysisFileTypeList = new ArrayList<>();
        long rowFrom = ((pageFrom - 1) * pageSize) + 1;
        long rowTo = rowFrom + pageSize - 1;
        long count = 0;

        // To get all records
        if (totalPages < 0) {
            totalPages = Long.MAX_VALUE;
        }

        while (count < totalPages) {
            sqlxmlList = enaDbService.getEnaAnalysisXml(rowFrom, rowTo);
            subList = sqlxmlList.stream()
                    .map(this::getAnalysisFileTypeList)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            analysisFileTypeList.addAll(subList);

            // No more records in DB
            if (sqlxmlList.size() < pageSize) {
                break;
            }

            count++;
            rowFrom = rowTo + 1;
            rowTo = rowFrom + pageSize - 1;
        }
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
