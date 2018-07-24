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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Study extends Auditable<AccessionVersionEntityId> {

    @ApiModelProperty(position = 1, required = true)
    @Valid
    @EmbeddedId
    private AccessionVersionEntityId id;

    @ApiModelProperty(position = 2, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 3, required = true)
    @NotNull
    @NotBlank
    @JsonProperty
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ApiModelProperty(position = 4, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String center;

    @ApiModelProperty(position = 5, required = true, example = "2018-01-01")
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private LocalDate releaseDate;

    @ApiModelProperty(position = 6, dataType = "java.lang.String", notes = "Url to a Taxonomy")
    @JsonProperty
    @ManyToOne(optional = false)
    private Taxonomy taxonomy;

    @ApiModelProperty(position = 7, example = "false")
    @JsonProperty(defaultValue = "false", access = JsonProperty.Access.WRITE_ONLY)
    @Column
    private boolean deprecated;

    @ApiModelProperty(position = 8, example = "false")
    @JsonProperty(defaultValue = "false")
    @Column
    private boolean browsable;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<LinkedStudy> linkedStudies;

    @OneToMany(mappedBy = "linkedStudy", cascade = CascadeType.ALL)
    private List<LinkedStudy> linkedStudiesOf;

    @OneToMany(mappedBy = "study")
    private List<Analysis> analyses;

    @OneToMany
    private List<WebResource> resources;

    @ManyToMany
    private List<Publication> publications;

    public AccessionVersionEntityId getId() {
        return id;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

}
