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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"accession", "version"}))
public class Analysis extends Auditable<Long> {

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

    @ApiModelProperty(position = 1, value = "Analysis auto generated id", required = true, readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ApiModelProperty(position = 2)
    @Embedded
    @Valid
    private AccessionVersionId accessionVersionId;

    @ApiModelProperty(position = 3, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 4, required = true)
    @NotNull
    @NotBlank
    @JsonProperty
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ApiModelProperty(position = 5, dataType = "java.lang.String", notes = "Url to a Study")
    @JsonProperty
    @ManyToOne(optional = false)
    private Study study;

<<<<<<< HEAD:metadata-ws/src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @ApiModelProperty(position = 6, dataType = "java.lang.String", notes = "Url to an Reference Sequence")
=======
    @ApiModelProperty(position = 5, dataType = "java.lang.String", notes = "Url to an Assembly")
>>>>>>> Only multiple genes with single analysis:src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @JsonProperty
    @ManyToOne(optional = false)
    private Assembly assembly;

<<<<<<< HEAD:metadata-ws/src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @ApiModelProperty(position = 7, required = true)
=======
    @ApiModelProperty(position = 6, dataType = "java.lang.String", notes = "Url to a Transcriptome")
    @JsonProperty
    @ManyToOne(optional = false)
    private Transcriptome transcriptome;

    @ApiModelProperty(position = 7, dataType = "java.lang.String", example = "[url1, url2]", notes = "Url to Gene List")
    @JsonProperty
    @ManyToMany
    private List<Gene> genes;

    @ApiModelProperty(position = 8, required = true)
>>>>>>> Only multiple genes with single analysis:src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Technology technology;

<<<<<<< HEAD:metadata-ws/src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @ApiModelProperty(position = 8, required = true)
=======
    @ApiModelProperty(position = 9, required = true)
>>>>>>> Only multiple genes with single analysis:src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

<<<<<<< HEAD:metadata-ws/src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @ApiModelProperty(position = 9, required = true)
=======
    @ApiModelProperty(position = 10, required = true)
>>>>>>> Only multiple genes with single analysis:src/main/java/uk/ac/ebi/ampt2d/metadata/persistence/entities/Analysis.java
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String platform;

    @ManyToMany
    private List<Sample> samples;

    @ManyToMany
    private List<File> files;

    @Override
    public Long getId() {
        return id;
    }

    public AccessionVersionId getAccessionVersionId() {
        return accessionVersionId;
    }


}