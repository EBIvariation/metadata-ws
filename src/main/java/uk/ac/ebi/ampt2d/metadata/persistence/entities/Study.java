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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class Study implements BaseEntity<String> {

    @ApiModelProperty(position = 1, value = "Study auto generated id", required = true, readOnly = true)
    @GenericGenerator(name = "idGenerator", strategy = "uk.ac.ebi.ampt2d.metadata.persistence.idgenerator.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    @Id
    private String id;

    @ApiModelProperty(position = 2, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    private String accession;

    @ApiModelProperty(position = 3, required = true)
    @Min(1)
    @JsonProperty
    private int version;

    @ApiModelProperty(position = 4, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 5, required = true)
    @NotNull
    @NotBlank
    @JsonProperty
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ApiModelProperty(position = 6, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String center;

    @ApiModelProperty(position = 7, dataType = "java.lang.String", notes = "Url to a Taxonomy")
    @JsonProperty
    @ManyToOne(optional = false)
    private Taxonomy taxonomy;

    @OneToMany(mappedBy = "study")
    private List<Analysis> analyses;

    @OneToMany
    private List<WebResource> resources;

    @ManyToMany
    private List<Publication> publications;

    @Override
    public String getAccession() {
        return accession;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public String getId() {
        return id;
    }
}
