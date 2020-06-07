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
package uk.ac.ebi.ampt2d.metadata;

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
import uk.ac.ebi.ampt2d.metadata.aop.ReleaseDateAspect;
import uk.ac.ebi.ampt2d.metadata.aop.StudyDeprecationAspect;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Project;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Publication;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.idconverter.CustomBackendIdConverter;
import uk.ac.ebi.ampt2d.metadata.persistence.services.ProjectService;
import uk.ac.ebi.ampt2d.metadata.persistence.services.ProjectServiceImpl;
import uk.ac.ebi.ampt2d.metadata.persistence.services.ReferenceSequenceService;
import uk.ac.ebi.ampt2d.metadata.persistence.services.ReferenceSequenceServiceImpl;
import uk.ac.ebi.ampt2d.metadata.persistence.services.StudyService;
import uk.ac.ebi.ampt2d.metadata.persistence.services.StudyServiceImpl;
import uk.ac.ebi.ampt2d.metadata.persistence.services.TaxonomyService;
import uk.ac.ebi.ampt2d.metadata.persistence.services.TaxonomyServiceImpl;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.AnalysisRestController;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.ProjectRestController;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.ReferenceSequenceRestController;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.SampleRestController;
import uk.ac.ebi.ampt2d.metadata.rest.controllers.StudyRestController;
import uk.ac.ebi.ampt2d.metadata.rest.resources.AnalysisResource;
import uk.ac.ebi.ampt2d.metadata.rest.resources.ProjectResource;
import uk.ac.ebi.ampt2d.metadata.rest.resources.ReferenceSequenceResource;
import uk.ac.ebi.ampt2d.metadata.rest.resources.SampleResource;
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
                config.getCorsRegistry().addMapping("/**").allowedMethods("*").allowedOrigins("*");
                config.exposeIdsFor(
                        Analysis.class,
                        ReferenceSequence.class,
                        File.class,
                        Sample.class,
                        Study.class,
                        Taxonomy.class,
                        Publication.class,
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
    public ProjectService projectService() {
        return new ProjectServiceImpl();
    }

    @Bean
    public ReferenceSequenceService referenceSequenceService() {
        return new ReferenceSequenceServiceImpl();
    }

    @Bean
    public TaxonomyService taxonomyTreeService() {
        return new TaxonomyServiceImpl();
    }

    @Bean
    public GenericResourceAssembler<Analysis, AnalysisResource> analysisResourceAssembler() {
        return new GenericResourceAssembler<Analysis, AnalysisResource>(AnalysisRestController.class, AnalysisResource.class);
    }

    @Bean
    public GenericResourceAssembler<ReferenceSequence, ReferenceSequenceResource> referenceSequenceResourceAssembler() {
        return new GenericResourceAssembler<ReferenceSequence, ReferenceSequenceResource>(ReferenceSequenceRestController.class, ReferenceSequenceResource.class);
    }

    @Bean
    public GenericResourceAssembler<Project, ProjectResource> projectResourceAssembler() {
        return new GenericResourceAssembler<>(ProjectRestController.class, ProjectResource.class);
    }

    @Bean
    public GenericResourceAssembler<Study, StudyResource> studyResourceAssembler() {
        return new GenericResourceAssembler<Study, StudyResource>(StudyRestController.class, StudyResource.class);
    }

    @Bean
    public GenericResourceAssembler<Sample, SampleResource> sampleResourceAssembler() {
        return new GenericResourceAssembler<Sample, SampleResource>(SampleRestController.class, SampleResource.class);
    }

    /**
     * Inject StudyDeprecationAspect bean
     * <p>
     * The StudyDeprecationAspect ensures every GET request returns only not yet deprecated studies
     *
     * @return StudyDeprecationAspect
     */
    @Bean
    public StudyDeprecationAspect studyDeprecationAspect() {
        return new StudyDeprecationAspect();
    }

    /**
     * Inject ReleaseDateAspect bean conditionally
     * <p>
     * The ReleaseDateAspect ensures every GET request returns only published entities
     * Set "endpoints.studies.date.restricted" to false if you don't want this restriction
     *
     * @return ReleaseDateAspect
     */
    @Bean
    @ConditionalOnProperty(name = "endpoints.entities.release-date.restricted", matchIfMissing = true)
    public ReleaseDateAspect releaseDateAspect() {
        return new ReleaseDateAspect();
    }

}
