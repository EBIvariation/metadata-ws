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
import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.converter.AnalysisConverter;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.ReferenceSequenceExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.SampleExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.StudyExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.persistence.AnalysisPersistenceApplicationRunner;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

@Configuration
@ConditionalOnProperty(name = "import.object", havingValue = "analysis")
public class AnalysisPersistenceApplicationRunnerConfiguration {

    @Bean
    public AnalysisPersistenceApplicationRunner pipelineApplicationRunner(
            SraRetrieverByAccession sraRetrieverByAccession,
            SraXmlParser sraXmlParser,
            AnalysisRepository analysisRepository,
            StudyRepository studyRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            FileRepository fileRepository,
            SampleRepository sampleRepository,
            TaxonomyRepository taxonomyRepository) {
        return new AnalysisPersistenceApplicationRunner(sraRetrieverByAccession, sraXmlParser, analysisRepository,
                analysisConverter(studyRepository, referenceSequenceRepository, fileRepository, sampleRepository,
                        taxonomyRepository));
    }

    @Bean
    public Converter<AnalysisType, Analysis> analysisConverter(StudyRepository studyRepository,
                                                               ReferenceSequenceRepository referenceSequenceRepository,
                                                               FileRepository fileRepository,
                                                               SampleRepository sampleRepository,
                                                               TaxonomyRepository taxonomyRepository) {
        return new AnalysisConverter(studyExtractor(studyRepository, taxonomyRepository),
                referenceSequenceExtractorFromAnalysis(referenceSequenceRepository),
                fileExtractorFromAnalysis(fileRepository),
                sampleExtractor(sampleRepository, taxonomyRepository));
    }

    @Bean
    public SampleExtractor sampleExtractor(SampleRepository sampleRepository, TaxonomyRepository taxonomyRepository) {
        return new SampleExtractor(sampleRepository, taxonomyExtractor(taxonomyRepository));
    }

    @Bean
    public TaxonomyExtractor taxonomyExtractor(TaxonomyRepository taxonomyRepository) {
        return new TaxonomyExtractor(taxonomyRepository);
    }

    @Bean
    public SraXmlParser<AnalysisType> sraXmlParser() {
        return new SraAnalysisXmlParser();
    }

    @Bean
    public StudyExtractor studyExtractor(StudyRepository studyRepository, TaxonomyRepository taxonomyRepository) {
        return new StudyExtractor(studyRepository, taxonomyExtractor(taxonomyRepository));
    }

    @Bean
    public ReferenceSequenceExtractorFromAnalysis referenceSequenceExtractorFromAnalysis(
            ReferenceSequenceRepository referenceSequenceRepository) {
        return new ReferenceSequenceExtractorFromAnalysis(referenceSequenceRepository);
    }

    @Bean
    public FileExtractorFromAnalysis fileExtractorFromAnalysis(FileRepository fileRepository) {
        return new FileExtractorFromAnalysis(fileRepository);
    }
}
