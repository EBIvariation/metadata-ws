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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.api.AssemblyXmlRetrieverThroughEntrezApi;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraObjectsImporterThroughApi;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraXmlRetrieverThroughApi;
import uk.ac.ebi.ampt2d.metadata.importer.converter.AnalysisConverter;
import uk.ac.ebi.ampt2d.metadata.importer.converter.ReferenceSequenceConverter;
import uk.ac.ebi.ampt2d.metadata.importer.converter.SampleConverter;
import uk.ac.ebi.ampt2d.metadata.importer.converter.StudyConverter;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraObjectsImporterThroughDatabase;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraXmlRetrieverThroughDatabase;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.DomQueryUsingXPath;
import uk.ac.ebi.ampt2d.metadata.importer.xml.EntrezAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraEntryXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraSampleXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraStudyXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PublicationRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

@Configuration
public class MetadataImporterMainApplicationConfiguration {

    @Bean
    public SraXmlRetrieverThroughApi sraXmlRetrieverThroughApi() {
        return new SraXmlRetrieverThroughApi();
    }

    @Bean
    @ConditionalOnProperty(name = "import.source", havingValue = "API")
    public ObjectsImporter objectImporterThroughEnaApi(SraXmlRetrieverThroughApi sraXmlRetrieverThroughApi,
                                                       PublicationRepository publicationRepository,
                                                       WebResourceRepository webResourceRepository,
                                                       FileRepository fileRepository,
                                                       TaxonomyRepository taxonomyRepository,
                                                       ReferenceSequenceRepository referenceSequenceRepository,
                                                       AnalysisRepository analysisRepository,
                                                       StudyRepository studyRepository,
                                                       SampleRepository sampleRepository,
                                                       @Value("${entrez.api.key:}") String entrezApiKey) {
        return new SraObjectsImporterThroughApi(
                sraXmlRetrieverThroughApi,
                assemblyXmlRetrieverThroughEntrezApi(entrezApiKey),

                sraStudyXmlParser(),
                sraAnalysisXmlParser(),
                sraAssemblyXmlParser(),
                sraEntryXmlParser(),
                entrezAssemblyXmlParser(),
                sraSampleXmlParser(),

                studyConverter(),
                analysisConverter(),
                referenceSequenceConverter(),
                sampleConverter(),

                publicationExtractorFromStudy(publicationRepository),
                webResourceExtractorFromStudy(webResourceRepository),
                fileExtractorFromAnalysis(fileRepository),

                studyRepository,
                analysisRepository,
                referenceSequenceRepository,
                sampleRepository,
                taxonomyRepository
        );
    }

    @Bean
    @ConditionalOnProperty(name = "import.source", havingValue = "DB")
    public ObjectsImporter objectImporterThroughEnaDatabase(
            SraXmlRetrieverThroughDatabase sraXmlRetrieverThroughDatabase,
            SraXmlRetrieverThroughApi sraXmlRetrieverThroughApi,
            PublicationRepository publicationRepository,
            WebResourceRepository webResourceRepository,
            FileRepository fileRepository,
            TaxonomyRepository taxonomyRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            AnalysisRepository analysisRepository,
            StudyRepository studyRepository,
            SampleRepository sampleRepository,
            @Value("${entrez.api.key:}") String entrezApiKey) {
        return new SraObjectsImporterThroughDatabase(
                // For database import we need two importers
                // Most entries are imported from the database, but reference sequences can only be imported via API
                sraXmlRetrieverThroughDatabase,
                sraXmlRetrieverThroughApi,
                assemblyXmlRetrieverThroughEntrezApi(entrezApiKey),

                sraStudyXmlParser(),
                sraAnalysisXmlParser(),
                sraAssemblyXmlParser(),
                sraEntryXmlParser(),
                entrezAssemblyXmlParser(),
                sraSampleXmlParser(),

                studyConverter(),
                analysisConverter(),
                referenceSequenceConverter(),
                sampleConverter(),

                publicationExtractorFromStudy(publicationRepository),
                webResourceExtractorFromStudy(webResourceRepository),
                fileExtractorFromAnalysis(fileRepository),

                studyRepository,
                analysisRepository,
                referenceSequenceRepository,
                sampleRepository,
                taxonomyRepository
        );
    }

    // Parser factories

    private SraStudyXmlParser sraStudyXmlParser() {
        return new SraStudyXmlParser();
    }

    private SraAnalysisXmlParser sraAnalysisXmlParser() {
        return new SraAnalysisXmlParser();
    }

    private SraSampleXmlParser sraSampleXmlParser() {
        return new SraSampleXmlParser();
    }

    private SraAssemblyXmlParser sraAssemblyXmlParser() {
        return new SraAssemblyXmlParser();
    }

    private SraEntryXmlParser sraEntryXmlParser() {
        return new SraEntryXmlParser();
    }

    private AssemblyXmlRetrieverThroughEntrezApi assemblyXmlRetrieverThroughEntrezApi(String entrezApiKey) {
        return new AssemblyXmlRetrieverThroughEntrezApi(entrezApiKey);
    }

    private EntrezAssemblyXmlParser entrezAssemblyXmlParser() {
        return new EntrezAssemblyXmlParser();
    }

    // Converter factories

    private StudyConverter studyConverter() {
        return new StudyConverter();
    }

    private AnalysisConverter analysisConverter() {
        return new AnalysisConverter();
    }

    private SampleConverter sampleConverter() {
        return new SampleConverter();
    }

    private ReferenceSequenceConverter referenceSequenceConverter() {
        return new ReferenceSequenceConverter();
    }

    // Extractor factories

    private PublicationExtractorFromStudy publicationExtractorFromStudy(PublicationRepository publicationRepository) {
        return new PublicationExtractorFromStudy(publicationRepository);
    }

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy(WebResourceRepository webResourceRepository) {
        return new WebResourceExtractorFromStudy(webResourceRepository);
    }

    private FileExtractorFromAnalysis fileExtractorFromAnalysis(FileRepository fileRepository) {
        return new FileExtractorFromAnalysis(fileRepository);
    }

}
