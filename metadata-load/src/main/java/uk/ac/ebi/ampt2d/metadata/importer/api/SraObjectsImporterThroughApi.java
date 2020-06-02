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
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.xml.EntrezAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Project;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.events.TaxonomyEventHandler;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ProjectRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.ProjectType;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SraObjectsImporterThroughApi extends ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(SraObjectsImporterThroughApi.class.getName());

    public SraObjectsImporterThroughApi(
            SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
            ReferenceSequenceXmlRetrieverThroughEntrezApi referenceSequenceXmlRetrieverThroughEntrezApi,

            SraXmlParser<ProjectType> sraProjectXmlParser,
            SraXmlParser<StudyType> sraStudyXmlParser,
            SraXmlParser<AnalysisType> sraAnalysisXmlParser,
            EntrezAssemblyXmlParser entrezAssemblyXmlParser,
            SraXmlParser<SampleType> sraSampleXmlParser,

            Converter<ProjectType, Project> projectConverter,
            Converter<StudyType, Study> studyConverter,
            Converter<AnalysisType, Analysis> analysisConverter,
            Converter<SampleType, Sample> sampleConverter,

            PublicationExtractor publicationExtractor,
            WebResourceExtractor webResourceExtractor,
            FileExtractorFromAnalysis fileExtractorFromAnalysis,

            ProjectRepository projectRepository,
            StudyRepository studyRepository,
            AnalysisRepository analysisRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            SampleRepository sampleRepository,
            TaxonomyEventHandler taxonomyEventHandler) {
        super(
                sraXmlRetrieverByAccession,
                referenceSequenceXmlRetrieverThroughEntrezApi,

                sraProjectXmlParser,
                sraStudyXmlParser,
                sraAnalysisXmlParser,
                entrezAssemblyXmlParser,
                sraSampleXmlParser,

                projectConverter,
                studyConverter,
                analysisConverter,
                sampleConverter,

                publicationExtractor,
                webResourceExtractor,
                fileExtractorFromAnalysis,

                projectRepository,
                studyRepository,
                analysisRepository,
                referenceSequenceRepository,
                sampleRepository,
                taxonomyEventHandler
        );
    }

    @Override
    protected Project extractAnalysisFromProject(ProjectType projectType, Project project) throws Exception {
        projectRepository.save(project);
        for (String analysisAccession : getAnalysisAccessions(projectType)) {
            Analysis analysis = importAnalysis(analysisAccession);
            try {
                analysis.setProject(project);
                analysisRepository.save(analysis);
            } catch (Exception exception) {
                IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for accession " + analysisAccession);
                IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                throw exception;
            }
        }
        return project;
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
                IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                throw exception;
            }
        }
        return study;
    }

    @Override
    protected Analysis extractStudyFromAnalysis(AnalysisType analysisType, Analysis analysis) throws Exception {
        return analysis;
    }

    private Set<String> getAnalysisAccessions(ProjectType projectType) {
        ProjectType.PROJECTLINKS projectlinks = projectType.getPROJECTLINKS();
        if (projectlinks == null) {
            return new HashSet<>();
        }
        ProjectType.PROJECTLINKS.PROJECTLINK[] projectLinkArrays = projectlinks.getPROJECTLINKArray();

        return getAnalysesFromLinks(
                Arrays.stream(projectLinkArrays).map(ProjectType.PROJECTLINKS.PROJECTLINK::getXREFLINK));
    }

    private Set<String> getAnalysisAccessions(StudyType studyType) {
        StudyType.STUDYLINKS studylinks = studyType.getSTUDYLINKS();
        if (studylinks == null) {
            return new HashSet<>();
        }
        LinkType[] studyLinkArrays = studylinks.getSTUDYLINKArray();

        return getAnalysesFromLinks(Arrays.stream(studyLinkArrays).map(LinkType::getXREFLINK));
    }

    private Set<String> getAnalysesFromLinks(Stream<XRefType> xrefs) {
        Optional<String> analyses = xrefs.filter(Objects::nonNull)
                                         .filter(xref -> xref.getDB().equals("ENA-ANALYSIS"))
                                         .findFirst()
                                         .map(XRefType::getID);

        Set<String> analysisAccessions = new HashSet<>();
        if (!analyses.isPresent()) {
            return analysisAccessions;
        }
        Stream.of(analyses.get().split(","))
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
