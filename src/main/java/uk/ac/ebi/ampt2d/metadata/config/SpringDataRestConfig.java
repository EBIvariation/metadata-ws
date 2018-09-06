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
package uk.ac.ebi.ampt2d.metadata.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.ac.ebi.ampt2d.metadata.aop.StudyDeprecationAspect;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.idconverter.CustomBackendIdConverter;
import uk.ac.ebi.ampt2d.metadata.aop.StudyReleaseDateAspect;
import uk.ac.ebi.ampt2d.metadata.persistence.services.StudyService;
import uk.ac.ebi.ampt2d.metadata.persistence.services.StudyServiceImpl;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.AnalysisRestController;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.AssemblyRestController;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.StudyRestController;
import uk.ac.ebi.ampt2d.metadata.rest.resources.AnalysisResource;
import uk.ac.ebi.ampt2d.metadata.rest.resources.AssemblyResource;
import uk.ac.ebi.ampt2d.metadata.rest.resources.StudyResource;

@Configuration
@EnableJpaAuditing
public class SpringDataRestConfig {

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new CustomBackendIdConverter());
                super.addFormatters(registry);
            }
        };
    }

    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurerAdapter() {

            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
                config.exposeIdsFor(
                        Analysis.class,
                        Assembly.class,
                        File.class,
                        Sample.class,
                        Study.class,
                        Taxonomy.class,
                        WebResource.class
                );

            }

            @Override
            public void configureConversionService(ConfigurableConversionService conversionService) {
                super.configureConversionService(conversionService);
                conversionService.addConverter(new CustomBackendIdConverter());
            }

            @Override
            public void configureValidatingRepositoryEventListener(
                    ValidatingRepositoryEventListener validatingListener) {
                Validator validator = validator();
                validatingListener.addValidator("beforeCreate", validator);
                validatingListener.addValidator("beforeSave", validator);
            }

        };
    }

    @Bean
    public StudyService studyService() {
        return new StudyServiceImpl();
    }

    @Bean
    public GenericResourceAssembler<Analysis, AnalysisResource> analysisResourceAssembler() {
        return new GenericResourceAssembler<Analysis, AnalysisResource>(AnalysisRestController.class, AnalysisResource.class);
    }

    @Bean
    public GenericResourceAssembler<Assembly, AssemblyResource> assemblyResourceAssembler() {
        return new GenericResourceAssembler<Assembly, AssemblyResource>(AssemblyRestController.class, AssemblyResource.class);
    }

    @Bean
    public GenericResourceAssembler<Study, StudyResource> studyResourceAssembler() {
        return new GenericResourceAssembler<Study, StudyResource>(StudyRestController.class, StudyResource.class);
    }

    /**
     * Inject StudyDeprecationAspect bean
     *
     * The StudyDeprecationAspect ensures every GET request returns only not yet deprecated studies
     *
     * @return StudyDeprecationAspect
     */
    @Bean
    public StudyDeprecationAspect studyDeprecationAspect() {
        return new StudyDeprecationAspect();
    }

    /**
     * Inject StudyReleaseDateAspect bean conditionally
     *
     * The StudyReleaseDateAspect ensures every GET request returns only published studies
     * Set "endpoints.studies.date.restricted" to false if you don't want this restriction
     *
     * @return StudyReleaseDateAspect
     */
    @Bean
    @ConditionalOnProperty(name = "endpoints.studies.release-date.restricted", matchIfMissing = true)
    public StudyReleaseDateAspect studyReleaseDateAspect() {
        return new StudyReleaseDateAspect();
    }

}
