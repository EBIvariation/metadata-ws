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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name","patch"}))
@SequenceGenerator(initialValue=1, allocationSize=1 , name="REFERENCE_SEQUENCE_SEQ",
        sequenceName="reference_sequence_sequence")
public class ReferenceSequence extends Auditable<Long> {

    public enum Type {

        GENOME_ASSEMBLY,

        SEQUENCE,

        TRANSCRIPTOME_SHOTGUN_ASSEMBLY

    }

    @ApiModelProperty(position = 1, value = "Reference Sequence auto generated id", readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="REFERENCE_SEQUENCE_SEQ")
    private long id;

    @ApiModelProperty(position = 2, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 3, required = true)
    @JsonProperty
    @Column(nullable = true)
    private String patch;

    @ApiModelProperty(position = 4, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String accession;

    @ApiModelProperty(position = 5, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ApiModelProperty(position = 6, dataType = "java.lang.String", notes = "URL to a taxonomy")
    @JsonProperty
    @NotNull(message = "A reference sequence must have one valid URL to taxonomy")
    @ManyToOne(optional = false)
    private Taxonomy taxonomy;

    public ReferenceSequence() {}

    public ReferenceSequence(String name, String patch, String accession, Type type) {
        this.name = name;
        this.patch = patch;
        this.accession = accession;
        this.type = type;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPatch() {
        return patch;
    }

    public String getAccession() {
        return accession;
    }

    public Type getType() {
        return type;
    }

    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    /**
     * Release date control: reference sequences are always public.
     */
    @Override
    public LocalDate getReleaseDate() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /*
    *  ReferenceSequence entities are public they are not restricted by access control. So returning null for studyIds.
    */
    @Override
    public String getStudyIds() {
        return null;
    }
}
