/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.ampt2d.metadata.importer.objectImporters;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SraObjectsImporter implements ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(SraObjectsImporter.class.getName());

    private SraRetrieverByAccession sraRetrieverByAccession;

    private SraXmlParser<StudyType> sraStudyXmlParser;

    private Converter<StudyType, Study> studyConverter;

    public SraObjectsImporter(SraRetrieverByAccession sraRetrieverByAccession,
                              SraXmlParser<StudyType> sraStudyXmlParser,
                              Converter<StudyType, Study> studyConverter) {
        this.sraRetrieverByAccession = sraRetrieverByAccession;
        this.sraStudyXmlParser = sraStudyXmlParser;
        this.studyConverter = studyConverter;
    }

    @Override
    public List<Study> importStudy(Set<String> accessions) {
        List<Study> studies = new ArrayList<>();
        for (String accession : accessions) {
            try {
                String xml = sraRetrieverByAccession.getXml(accession);
                StudyType studyType = sraStudyXmlParser.parseXml(xml, accession);
                Study study = studyConverter.convert(studyType);
                List<Analysis> analyses = importAnalysis(getAnalysisAccessions(studyType));
                study.setAnalyses(analyses);
                studies.add(study);
            } catch (Exception exception) {
                IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for accession" + accession);
                IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
            }
        }
        return studies;
    }

    @Override
    public List<Analysis> importAnalysis(Set<String> accessions) {
        return new ArrayList<>();
    }

    @Override
    public List<ReferenceSequence> importReferenceSequence(Set<String> accessions) {
        return new ArrayList<>();
    }

    @Override
    public List<Sample> importSample(Set<String> accessions) {
        return new ArrayList<>();
    }

    private Set<String> getAnalysisAccessions(StudyType studyType) {

        Set<String> analysisAccessions = new HashSet<>();

        StudyType.STUDYLINKS studylinks = studyType.getSTUDYLINKS();
        if (studylinks == null) {
            return analysisAccessions;
        }
        LinkType[] studyLinkArrays = studylinks.getSTUDYLINKArray();
        String analyses = null;
        for (int i = 0; i < studyLinkArrays.length; i++) {
            XRefType xRefType = studyLinkArrays[i].getXREFLINK();
            if (xRefType != null && xRefType.getDB().equals("ENA-ANALYSIS")) {
                analyses = xRefType.getID();
                break;
            }
        }
        if (analyses == null) {
            return analysisAccessions;
        }
        Stream.of(analyses.split(","))
                .forEach(analysis -> {
                    String[] range = analysis.split("-");
                    if (range.length > 1) {
                        String startRange = range[0];
                        String endRange = range[1];
                        String analysisAccessionPrefix = startRange.split("\\d")[0];
                        int startRangeNumericPart = Integer.valueOf(startRange.split("[A-Z]*[^\\d]")[1]);
                        int endRangeNumericPart = Integer.valueOf(endRange.split("[A-Z]*[^\\d]")[1]);
                        int lengthOfNumericPartOfAccessionWithZeros = startRange.length() - analysisAccessionPrefix.length();
                        for (int i = startRangeNumericPart; i <= endRangeNumericPart; i++) {
                            analysisAccessions.add(analysisAccessionPrefix + String.format
                                    ("%0" + lengthOfNumericPartOfAccessionWithZeros + "d", i));
                        }
                    } else {
                        analysisAccessions.add(analysis);
                    }
                });

        return analysisAccessions;
    }
}
