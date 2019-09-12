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

package uk.ac.ebi.ampt2d.metadata.importer.api;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.EntrezAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SraObjectsImporterThroughApi extends ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(SraObjectsImporterThroughApi.class.getName());

    public SraObjectsImporterThroughApi(
            SraXmlRetrieverThroughApi sraXmlRetrieverThroughApi,
            AssemblyXmlRetrieverThroughEntrezApi assemblyXmlRetrieverThroughEntrezApi,

            SraXmlParser<StudyType> sraStudyXmlParser,
            SraXmlParser<AnalysisType> sraAnalysisXmlParser,
            EntrezAssemblyXmlParser entrezAssemblyXmlParser,
            SraXmlParser<SampleType> sraSampleXmlParser,

            Converter<StudyType, Study> studyConverter,
            Converter<AnalysisType, Analysis> analysisConverter,
            Converter<SampleType, Sample> sampleConverter,

            PublicationExtractorFromStudy publicationExtractorFromStudy,
            WebResourceExtractorFromStudy webResourceExtractorFromStudy,
            FileExtractorFromAnalysis fileExtractorFromAnalysis,

            StudyRepository studyRepository,
            AnalysisRepository analysisRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            SampleRepository sampleRepository,
            TaxonomyRepository taxonomyRepository) {
        super(
                sraXmlRetrieverThroughApi,
                assemblyXmlRetrieverThroughEntrezApi,

                sraStudyXmlParser,
                sraAnalysisXmlParser,
                entrezAssemblyXmlParser,
                sraSampleXmlParser,

                studyConverter,
                analysisConverter,
                sampleConverter,

                publicationExtractorFromStudy,
                webResourceExtractorFromStudy,
                fileExtractorFromAnalysis,

                studyRepository,
                analysisRepository,
                referenceSequenceRepository,
                sampleRepository,
                taxonomyRepository
        );
    }

    @Override
    protected Study extractAnalysisFromStudy(StudyType studyType, Study study) throws Exception {
        studyRepository.save(study);
        for (String analysisAccession : getAnalysisAccessions(studyType)) {
            Analysis analysis = importAnalysis(analysisAccession);
            try {
                analysis.setStudy(study);
                analysisRepository.save(analysis);
            } catch (Exception exception) {
                IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for accession " + analysisAccession);
                IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
                throw exception;
            }
        }
        return study;
    }

    @Override
    protected Analysis extractStudyFromAnalysis(AnalysisType analysisType, Analysis analysis) throws Exception {
        return analysis;
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
