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

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.ampt2d.metadata.properties.SwaggerApiInfoProperties;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Configuration
@EnableSwagger2
@EnableConfigurationProperties(SwaggerApiInfoProperties.class)
@Import({SpringDataRestConfiguration.class, BeanValidatorPluginsConfiguration.class})
public class SwaggerConfig {

    @Autowired
    private TypeResolver typeResolver;

    @Autowired
    private SwaggerApiInfoProperties swaggerApiInfoProperties;

    private SecurityReference securityReference = SecurityReference.builder()
            .reference("Authorization").scopes(new AuthorizationScope[0]).build();

    private SecurityContext securityContext = SecurityContext.builder()
            .securityReferences(Arrays.asList(securityReference)).build();

    @Bean
    public Docket metadataApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(getScanRestServicesPathPredicate())
                .build()
                .apiInfo(getApiInfo())
                .pathMapping("/")
                .tags(
                        new Tag("Analysis Entity", "Analysis metadata"),
                        new Tag("Assembly Entity", "Assembly definition"),
                        new Tag("File Entity", "File metadata"),
                        new Tag("Publication Entity", "Publication Pubmed reference"),
                        new Tag("Sample Entity", "Sample metadata"),
                        new Tag("Study Entity", "Study metadata"),
                        new Tag("Taxonomy Entity", "Taxonomy definitions"),
                        new Tag("WebResource Entity", "Study resources that can be accessed through web protocols")
                )
                .globalResponseMessage(RequestMethod.POST, getResponseMessagesForPostAndPatch())
                .globalResponseMessage(RequestMethod.PATCH, getResponseMessagesForPostAndPatch())
                .directModelSubstitute(LocalDate.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .securitySchemes(Arrays.asList(new ApiKey("Authorization", "Authorization", "header")))
                .securityContexts(Arrays.asList(securityContext))
                .alternateTypeRules(getSubstitutionRules());
    }

    private List<ResponseMessage> getResponseMessagesForPostAndPatch() {
        return Arrays.asList(new ResponseMessageBuilder()
                        .code(201)
                        .message("Created")
                        .build(),
                new ResponseMessageBuilder()
                        .code(400)
                        .message("Validation error")
                        .build(),
                new ResponseMessageBuilder()
                        .code(401)
                        .message("Unauthorized")
                        .build(),
                new ResponseMessageBuilder()
                        .code(403)
                        .message("Forbidden")
                        .build(),
                new ResponseMessageBuilder()
                        .code(404)
                        .message("Not Found")
                        .build());
    }

    private Predicate<String> getScanRestServicesPathPredicate() {
        return Predicates.and(
                Predicates.not(PathSelectors.regex("/actuator.*")), // Hide spring-actuator
                Predicates.not(PathSelectors.regex("/error.*")), // Hide spring-data error
                Predicates.not(PathSelectors.regex("/profile.*")) // Hide spring-data profile
        );
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .contact(new Contact(swaggerApiInfoProperties.getContact().getName(),
                        swaggerApiInfoProperties.getContact().getUrl(),
                        swaggerApiInfoProperties.getContact().getEmail()))
                .license(swaggerApiInfoProperties.getLicense())
                .licenseUrl(swaggerApiInfoProperties.getLicenseUrl())
                .termsOfServiceUrl(swaggerApiInfoProperties.getTermsOfServiceUrl())
                .title(swaggerApiInfoProperties.getTitle())
                .description(swaggerApiInfoProperties.getDescription())
                .version(swaggerApiInfoProperties.getVersion())
                .build();
    }

    private AlternateTypeRule[] getSubstitutionRules() {
        AlternateTypeRule[] alternateTypeRules = new AlternateTypeRule[2];
        alternateTypeRules[0] = newRule(typeResolver.resolve(DeferredResult.class,
                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                typeResolver.resolve(WildcardType.class));
        alternateTypeRules[1] = newRule(typeResolver.resolve(Iterable.class, WildcardType.class),
                typeResolver.resolve(List.class, WildcardType.class));
        return alternateTypeRules;
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayRequestDuration(true)
                .filter(false)
                .validatorUrl("")
                .build();
    }

}
