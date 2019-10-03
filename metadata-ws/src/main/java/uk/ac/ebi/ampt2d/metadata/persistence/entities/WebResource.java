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
package uk.ac.ebi.ampt2d.metadata.persistence.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@SequenceGenerator(initialValue = 1, allocationSize = 1, name = "WEB_RESOURCE_SEQ", sequenceName = "web_resource_sequence")
public class WebResource extends Auditable<Long> {

    private static final String WEBRESOURCE_QUERY_EXPRESSION = "FROM study_resources " +
            "INNER JOIN study on study_resources.study_id = study.id " +
            "WHERE study_resources.resources_id=id";

    @ApiModelProperty(position = 1, value = "Web resource auto generated id", readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "WEB_RESOURCE_SEQ")
    private long id;

    @ApiModelProperty(position = 2, required = true)
    @NotNull
    @JsonProperty
    @Column(nullable = false, unique = true)
    @NotEmpty
    @Pattern(message = "Must be a valid URL.", regexp = "(^(https?|ftp):(//|\\\\))[-a-zA-Z0-9+&@#/%?=~_|!:,.;$'`*\\[\\]()]+")
    private String resourceUrl;

    /**
     * Release date control: get the *earliest* release date from all studies which link to this web resource.
     */
    @Formula("(SELECT min(study.release_date) " + WEBRESOURCE_QUERY_EXPRESSION + ")")
    @JsonIgnore
    private LocalDate releaseDate;

    /**
     * Get the ids of the studies which link to this object (used for access control).
     */
    @Formula("(SELECT string_agg(study.accession,',') " + WEBRESOURCE_QUERY_EXPRESSION + ")")
    @JsonIgnore
    private String studyIds;

    WebResource() {
    }

    public WebResource(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public Long getId() {
        return id;
    }

    @Override
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String getStudyIds() {
        return studyIds;
    }
}
