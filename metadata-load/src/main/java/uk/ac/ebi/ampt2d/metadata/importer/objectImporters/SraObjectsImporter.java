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
import uk.ac.ebi.ampt2d.metadata.importer.database.SraObjectRetrieverThroughDatabase;
import uk.ac.ebi.ampt2d.metadata.importer.objects.AnalysisObject;
import uk.ac.ebi.ampt2d.metadata.importer.objects.ReferenceSequenceObject;
import uk.ac.ebi.ampt2d.metadata.importer.objects.SampleObject;
import uk.ac.ebi.ampt2d.metadata.importer.objects.StudyObject;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SraObjectsImporter implements ObjectsImporter {

    private static final String STUDY_QUERY = "SELECT STUDY_XML FROM ERA.ANALYSIS WHERE STUDY_ID = :accession";

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
    public StudyObject importObject(StudyObject studyObject) {

        if (sraRetrieverByAccession instanceof SraObjectRetrieverThroughDatabase) {
            ((SraObjectRetrieverThroughDatabase) sraRetrieverByAccession).setEnaObjectQuery(STUDY_QUERY);
        }

        Set<String> accessions = studyObject.getAccessions();

        for (String accession : accessions) {
            try {
                String xml = sraRetrieverByAccession.getXml(accession);
                StudyType studyType = sraStudyXmlParser.parseXml(xml, accession);
                Study study = studyConverter.convert(studyType);
                AnalysisObject analysisObject = new AnalysisObject(getAnalysisAccessions(studyType), new ArrayList<>());
                analysisObject.doImport(this);
                study.setAnalyses(analysisObject.getAnalyses());
                studyObject.getStudies().add(study);
            } catch (Exception exception) {
                IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for accession" + accession);
                IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
            }
        }

        return studyObject;
    }

    @Override
    public AnalysisObject importObject(AnalysisObject analysisObject) {
        return null;
    }

    @Override
    public ReferenceSequenceObject importObject(ReferenceSequenceObject referenceSequenceObject) {
        return null;
    }

    @Override
    public SampleObject importObject(SampleObject sampleObject) {
        return null;
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
                .forEach(a -> {
                    String[] ab = a.split("-");
                    if (ab.length > 1) {
                        String frstElement = ab[0];
                        String secondElement = ab[1];
                        String frstAnalysisStringPart = frstElement.split("\\d")[0];
                        int frstAnalysisNumericPart = Integer.valueOf(frstElement.split("[A-Z]*[^\\d]")[1]);
                        int lastAnlysisNumericPart = Integer.valueOf(secondElement.split("[A-Z]*[^\\d]")[1]);
                        int lengthOfNumericPartOfAccession = frstElement.length() - frstAnalysisStringPart.length();
                        for (int i = frstAnalysisNumericPart; i <= lastAnlysisNumericPart; i++) {
                            analysisAccessions.add(frstAnalysisStringPart + String.format
                                    ("%0" + lengthOfNumericPartOfAccession + "d", i));
                        }
                    } else {
                        analysisAccessions.add(a);
                    }
                });

        return analysisAccessions;
    }
}
