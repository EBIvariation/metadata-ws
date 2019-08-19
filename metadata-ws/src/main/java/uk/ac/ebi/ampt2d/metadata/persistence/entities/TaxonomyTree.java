/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(allocationSize = 1, name = "TAXONOMY_TREE_SEQ", sequenceName = "taxonomy_tree_sequence")
public class TaxonomyTree {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TAXONOMY_TREE_SEQ")
    private long id;

    @ApiModelProperty(position = 2, dataType = "java.lang.String", notes = "Url to a taxonomySpecies")
    @JoinColumn(name = "species_id", nullable = false, referencedColumnName = "taxonomyId", unique = true)
    @OneToOne
    private Taxonomy taxonomySpecies;

    @ApiModelProperty(position = 4, dataType = "java.lang.String", notes = "Url to a taxonomyGenus")
    @JoinColumn(name = "genus_id", referencedColumnName = "taxonomyId")
    @OneToOne
    private Taxonomy taxonomyGenus;

    @ApiModelProperty(position = 5, dataType = "java.lang.String", notes = "Url to a taxonomyOrder")
    @JoinColumn(name = "order_id", referencedColumnName = "taxonomyId")
    @OneToOne
    private Taxonomy taxonomyOrder;

    @ApiModelProperty(position = 6, dataType = "java.lang.String", notes = "Url to a taxonomyClass")
    @JoinColumn(name = "class_id", referencedColumnName = "taxonomyId")
    @OneToOne
    private Taxonomy taxonomyClass;

    public Taxonomy getTaxonomySpecies() {
        return taxonomySpecies;
    }

    public void setTaxonomySpecies(Taxonomy taxonomySpecies) {
        this.taxonomySpecies = taxonomySpecies;
    }

    @ApiModelProperty(position = 1, readOnly = true)
    public Long getTaxSpeciesId() {
        return taxonomySpecies.getTaxonomyId();
    }

    public Taxonomy getTaxonomyGenus() {
        return taxonomyGenus;
    }

    public void setTaxonomyGenus(Taxonomy taxonomyGenus) {
        this.taxonomyGenus = taxonomyGenus;
    }

    @ApiModelProperty(position = 3, readOnly = true)
    public Long getTaxGenusId() {
        return (taxonomyGenus == null) ? null : taxonomyGenus.getTaxonomyId();
    }

    public Taxonomy getTaxonomyOrder() {
        return taxonomyOrder;
    }

    public void setTaxonomyOrder(Taxonomy taxonomyOrder) {
        this.taxonomyOrder = taxonomyOrder;
    }

    @ApiModelProperty(position = 5, readOnly = true)
    public Long getTaxOrderId() {
        return (taxonomyOrder == null) ? null : taxonomyOrder.getTaxonomyId();
    }

    public Taxonomy getTaxonomyClass() {
        return taxonomyClass;
    }

    public void setTaxonomyClass(Taxonomy taxonomyClass) {
        this.taxonomyClass = taxonomyClass;
    }

    @ApiModelProperty(position = 7, readOnly = true)
    public Long getTaxClassId() {
        return (taxonomyClass == null) ? null : taxonomyClass.getTaxonomyId();
    }
}
