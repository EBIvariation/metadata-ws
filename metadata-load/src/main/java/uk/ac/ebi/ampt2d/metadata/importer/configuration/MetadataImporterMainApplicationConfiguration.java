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

package uk.ac.ebi.ampt2d.metadata.importer.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraObjectsImporterThroughAPI;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraXmlRetrieverThroughApi;
import uk.ac.ebi.ampt2d.metadata.importer.converter.AnalysisConverter;
import uk.ac.ebi.ampt2d.metadata.importer.converter.ReferenceSequenceConverter;
import uk.ac.ebi.ampt2d.metadata.importer.converter.StudyConverter;
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataAnalysisPersister;
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataStudyFinderOrPersister;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraObjectsImporterThroughDatabase;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraXmlRetrieverThroughDatabase;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraStudyXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PublicationRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

@Configuration
public class MetadataImporterMainApplicationConfiguration {

    @Bean
    @ConditionalOnProperty(name = "import.source", havingValue = "API")
    public ObjectsImporter objectImporterThroughEnaApi(SraXmlRetrieverThroughApi sraXmlRetrieverThroughApi,
                                                   PublicationRepository publicationRepository,
                                                   WebResourceRepository webResourceRepository,
                                                   FileRepository fileRepository,
                                                   TaxonomyRepository taxonomyRepository,
                                                   ReferenceSequenceRepository referenceSequenceRepository,
                                                   AnalysisRepository analysisRepository,
                                                   StudyRepository studyRepository) {
        return new SraObjectsImporterThroughAPI(
                sraXmlRetrieverThroughApi,
                sraStudyXmlParser(),
                studyConverter(),
                publicationExtractorFromStudy(publicationRepository),
                webResourceExtractorFromStudy(webResourceRepository),
                taxonomyExtractor(taxonomyRepository),
                sraAnalysisXmlParser(),
                analysisConverter(),
                fileExtractorFromAnalysis(fileRepository),
                sraAssemblyXmlParser(),
                referenceSequenceConverter(taxonomyRepository, referenceSequenceRepository),
                metadataAnalysisPersister(analysisRepository),
                metadataStudyFinderOrPersister(studyRepository)
        );
    }

    @Bean
    @ConditionalOnProperty(name = "import.source", havingValue = "DB")
    public ObjectsImporter objectImporterThroughEnaDatabase(SraXmlRetrieverThroughDatabase sraXmlRetrieverThroughDatabase,
                                                PublicationRepository publicationRepository,
                                                WebResourceRepository webResourceRepository,
                                                FileRepository fileRepository,
                                                TaxonomyRepository taxonomyRepository,
                                                ReferenceSequenceRepository referenceSequenceRepository,
                                                AnalysisRepository analysisRepository,
                                                StudyRepository studyRepository) {
        return new SraObjectsImporterThroughDatabase(
                sraXmlRetrieverThroughDatabase,
                sraStudyXmlParser(),
                studyConverter(),
                publicationExtractorFromStudy(publicationRepository),
                webResourceExtractorFromStudy(webResourceRepository),
                taxonomyExtractor(taxonomyRepository),
                sraAnalysisXmlParser(),
                analysisConverter(),
                fileExtractorFromAnalysis(fileRepository),
                sraAssemblyXmlParser(),
                referenceSequenceConverter(taxonomyRepository, referenceSequenceRepository),
                metadataAnalysisPersister(analysisRepository),
                metadataStudyFinderOrPersister(studyRepository)
        );
    }

    private StudyConverter studyConverter() {
        return new StudyConverter();
    }

    private AnalysisConverter analysisConverter() {
        return new AnalysisConverter();
    }

    private SraStudyXmlParser sraStudyXmlParser() {
        return new SraStudyXmlParser();
    }

    private SraAnalysisXmlParser sraAnalysisXmlParser() {
        return new SraAnalysisXmlParser();
    }

    private MetadataStudyFinderOrPersister metadataStudyFinderOrPersister(StudyRepository studyRepository) {
        return new MetadataStudyFinderOrPersister(studyRepository);
    }

    private MetadataAnalysisPersister metadataAnalysisPersister(AnalysisRepository analysisRepository) {
        return new MetadataAnalysisPersister(analysisRepository);
    }

    private TaxonomyExtractor taxonomyExtractor(TaxonomyRepository taxonomyRepository) {
        return new TaxonomyExtractor(taxonomyRepository);
    }

    private FileExtractorFromAnalysis fileExtractorFromAnalysis(FileRepository fileRepository) {
        return new FileExtractorFromAnalysis(fileRepository);
    }

    private SraAssemblyXmlParser sraAssemblyXmlParser() {
        return new SraAssemblyXmlParser();
    }

    private PublicationExtractorFromStudy publicationExtractorFromStudy(PublicationRepository publicationRepository) {
        return new PublicationExtractorFromStudy(publicationRepository);
    }

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy(WebResourceRepository webResourceRepository) {
        return new WebResourceExtractorFromStudy(webResourceRepository);
    }

    private ReferenceSequenceConverter referenceSequenceConverter(
            TaxonomyRepository taxonomyRepository,ReferenceSequenceRepository referenceSequenceRepository) {
        return new ReferenceSequenceConverter(taxonomyRepository, referenceSequenceRepository);
    }
}
