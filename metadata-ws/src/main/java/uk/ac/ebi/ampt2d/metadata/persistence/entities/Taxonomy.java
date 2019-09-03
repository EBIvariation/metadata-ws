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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@SequenceGenerator(allocationSize = 1, name = "TAXONOMY_SEQ", sequenceName = "taxonomy_sequence")
public class Taxonomy extends Auditable<Long> implements Serializable {

    @ApiModelProperty(position = 1, value = "Taxonomy auto generated id", readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TAXONOMY_SEQ")
    private long id;

    @ApiModelProperty(position = 2, example = "1")
    @NotNull
    @JsonProperty
    @Min(1)
    @Column(unique = true)
    private long taxonomyId;

    @ApiModelProperty(position = 3)
    @NotNull
    @JsonProperty
    @Size(max = 255, min = 1)
    @Column(unique = true)
    private String name;

    @ApiModelProperty(position = 4)
    @JsonProperty
    @Column
    private String rank;

    @ApiModelProperty(position = 5, dataType = "java.lang.String", notes = "Url to a taxonomySpecies")
    @JoinColumn(name = "speciesId", referencedColumnName = "taxonomyId")
    @ManyToOne
    private Taxonomy taxonomySpecies;

    @ApiModelProperty(position = 6, dataType = "java.lang.String", notes = "Url to a taxonomyGenus")
    @JoinColumn(name = "genusId", referencedColumnName = "taxonomyId")
    @ManyToOne
    private Taxonomy taxonomyGenus;

    @ApiModelProperty(position = 7, dataType = "java.lang.String", notes = "Url to a taxonomyOrder")
    @JoinColumn(name = "orderId", referencedColumnName = "taxonomyId")
    @ManyToOne
    private Taxonomy taxonomyOrder;

    @ApiModelProperty(position = 8, dataType = "java.lang.String", notes = "Url to a taxonomyClass")
    @JoinColumn(name = "classId", referencedColumnName = "taxonomyId")
    @ManyToOne
    private Taxonomy taxonomyClass;

    public Taxonomy() {
    }

    public Taxonomy(long taxonomyId, String name) {
        this.taxonomyId = taxonomyId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public long getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(long taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Release date control: taxonomies are always public.
     */
    @Override
    @JsonIgnore
    public LocalDate getReleaseDate() {
        return null;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Taxonomy getTaxonomySpecies() {
        return taxonomySpecies;
    }

    public void setTaxonomySpecies(Taxonomy taxonomySpecies) {
        this.taxonomySpecies = taxonomySpecies;
    }

    public Taxonomy getTaxonomyGenus() {
        return taxonomyGenus;
    }

    public void setTaxonomyGenus(Taxonomy taxonomyGenus) {
        this.taxonomyGenus = taxonomyGenus;
    }

    public Taxonomy getTaxonomyOrder() {
        return taxonomyOrder;
    }

    public void setTaxonomyOrder(Taxonomy taxonomyOrder) {
        this.taxonomyOrder = taxonomyOrder;
    }

    public Taxonomy getTaxonomyClass() {
        return taxonomyClass;
    }

    public void setTaxonomyClass(Taxonomy taxonomyClass) {
        this.taxonomyClass = taxonomyClass;
    }
}
