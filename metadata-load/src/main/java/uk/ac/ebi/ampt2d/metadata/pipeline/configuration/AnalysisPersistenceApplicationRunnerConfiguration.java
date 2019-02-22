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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.converter.FileConverter;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.pipeline.persistence.AnalysisPersistenceApplicationRunner;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

@Configuration
@ConditionalOnProperty(name = "import.object", havingValue = "analysis")
public class AnalysisPersistenceApplicationRunnerConfiguration {

    @Bean
    public AnalysisPersistenceApplicationRunner pipelineApplicationRunner(SraRetrieverByAccession sraRetrieverByAccession,
                                                                          SraXmlParser sraXmlParser) {
        return new AnalysisPersistenceApplicationRunner(getFileExtractorFromAnalysis(sraRetrieverByAccession, sraXmlParser));
    }

    @Bean
    public FileExtractorFromAnalysis getFileExtractorFromAnalysis(SraRetrieverByAccession sraRetrieverByAccession,
                                                                  SraXmlParser sraXmlParser) {
        return new FileExtractorFromAnalysis(sraRetrieverByAccession, sraXmlParser, getConverter());
    }

    @Bean
    public Converter<AnalysisFileType, File> getConverter() {
        return new FileConverter();
    }

    @Bean
    public SraXmlParser<AnalysisType> sraXmlParser() {
        return new SraAnalysisXmlParser();
    }

}
