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
package uk.ac.ebi.ampt2d.metadata.persistence.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ampt2d.metadata.persistence.events.AnalysisEventHandler;
import uk.ac.ebi.ampt2d.metadata.persistence.events.SampleEventHandler;
import uk.ac.ebi.ampt2d.metadata.persistence.events.TaxonomyEventHandler;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

@Configuration
public class RepositoryConfiguration {

    @Bean
    SampleEventHandler sampleEventHandler() {
        return new SampleEventHandler();
    }

    @Bean
    AnalysisEventHandler analysisEventHandler() {
        return new AnalysisEventHandler();
    }

    @Bean
    TaxonomyEventHandler taxonomyEventHandler(TaxonomyRepository taxonomyRepository) {
        return new TaxonomyEventHandler(taxonomyRepository);
    }

}