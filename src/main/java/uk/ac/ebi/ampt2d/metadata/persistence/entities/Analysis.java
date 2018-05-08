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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class Analysis {

    public enum Type {

        CASE_CONTROL,

        CONTROL_SET,

        CASE_SET,

        COLLECTION,

        TUMOR,

        MATCHED_NORMAL

    }

    public enum Technology {

        GWAS,

        EXOME_SEQUENCING,

        GENOTYPING,

        ARRAY,

        CURATION

    }

    @ApiModelProperty(position = 1, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Id
    private String accession;

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

    @ApiModelProperty(position = 4, dataType = "java.lang.String", notes = "Url to a Study")
    @JsonProperty
    @ManyToOne(optional = false)
    private Study study;

    @ApiModelProperty(position = 5, dataType = "java.lang.String", notes = "Url to an Assembly")
    @JsonProperty
    @ManyToOne(optional = false)
    private Assembly assembly;

    @ApiModelProperty(position = 6, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Technology technology;

    @ApiModelProperty(position = 7, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ApiModelProperty(position = 8, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String platform;

    @ManyToMany
    private List<Sample> samples;

    @ManyToMany
    private List<File> files;

}