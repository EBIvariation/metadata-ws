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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"type","resourceUrl"}))
@SequenceGenerator(initialValue=1, allocationSize=1 , name="WEB_RESOURCE_SEQ", sequenceName="web_resource_sequence")
public class WebResource extends Auditable<Long> {

    public enum Type {

        STUDY_WEB,

        CENTER_WEB,

        CONSORTIUM_WEB,

    }

    @ApiModelProperty(position = 1, value = "Web resource auto generated id", readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="WEB_RESOURCE_SEQ")
    private long id;

    @ApiModelProperty(position = 2, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ApiModelProperty(position = 3, required = true)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    @NotEmpty
    @Pattern(message = "Must be a valid URL.", regexp="(^(https?|ftp):(//|\\\\))[-a-zA-Z0-9+&@#/%?=~_|!:,.;$'`*\\[\\]()]+")
    private String resourceUrl;

    WebResource() {}

    public WebResource(Type type, String resourceUrl) {
        this.type = type;
        this.resourceUrl = resourceUrl;
    }

    public Long getId() {
        return id;
    }

    /**
     * Release date control: get the *earliest* release date from all studies which link to this web resource.
     */
    @Formula("(SELECT min(study.release_date) FROM study_resources " +
            "INNER JOIN study on study_resources.study_id = study.id " +
            "WHERE study_resources.resources_id=id)")
    @JsonIgnore
    private LocalDate releaseDate;

    @Override
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

}
