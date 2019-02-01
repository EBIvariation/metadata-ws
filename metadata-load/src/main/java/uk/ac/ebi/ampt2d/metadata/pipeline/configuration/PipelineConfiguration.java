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

package uk.ac.ebi.ampt2d.metadata.pipeline.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ampt2d.metadata.pipeline.converter.FileTypeConverter;
import uk.ac.ebi.ampt2d.metadata.pipeline.converter.SraToAmpt2dConverter;
import uk.ac.ebi.ampt2d.metadata.pipeline.importer.api.SraAnalysisDocumentImportFromAPI;
import uk.ac.ebi.ampt2d.metadata.pipeline.importer.api.SraAnalysisFileExtractFromAnalysisDocument;
import uk.ac.ebi.ampt2d.metadata.pipeline.importer.SraObjectImporter;
import uk.ac.ebi.ampt2d.metadata.pipeline.importer.SraObjectExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ena.sra.xml.ANALYSISDocument;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

@Configuration
@EntityScan(basePackages = "uk.ac.ebi.ampt2d.metadata.persistence.entities")
@EnableJpaRepositories(basePackages = "uk.ac.ebi.ampt2d.metadata.persistence.repositories")
public class PipelineConfiguration {

    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnProperty(name = "import.source",havingValue = "API")
    public SraObjectExtractorFromAnalysis<AnalysisFileType,ANALYSISDocument> sraObjectLoaderFromAnalysisDocument() {
        return new SraAnalysisFileExtractFromAnalysisDocument(sraObjectLoaderByAccession());
    }

    @Bean
    public SraObjectImporter<ANALYSISDocument> sraObjectLoaderByAccession() {
        return new SraAnalysisDocumentImportFromAPI(restTemplate());
    }

    @Bean
    public SraToAmpt2dConverter<AnalysisFileType, File> fileSraToAmpt2dConverter() {
        return new FileTypeConverter();
    }
}
