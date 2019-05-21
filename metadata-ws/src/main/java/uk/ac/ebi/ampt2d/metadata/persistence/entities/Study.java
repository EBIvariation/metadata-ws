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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"accession", "version"}))
@SequenceGenerator(initialValue = 1, allocationSize = 1, name = "STUDY_SEQ", sequenceName = "study_sequence")
public class Study extends Auditable<Long> {

    @ApiModelProperty(position = 1, value = "Study auto generated id", required = true, readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_SEQ")
    @Id
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

    @ApiModelProperty(position = 5, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String center;

    @ApiModelProperty(position = 6, required = true, example = "2018-01-01")
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private LocalDate releaseDate;

    @ApiModelProperty(position = 7, dataType = "java.lang.String", notes = "Url to a Taxonomy")
    @JsonProperty
    @ManyToOne(optional = false)
    private Taxonomy taxonomy;

    @ApiModelProperty(position = 8, example = "false")
    @JsonProperty(defaultValue = "false", access = JsonProperty.Access.WRITE_ONLY)
    @Column
    private boolean deprecated;

    @ApiModelProperty(position = 9, example = "false")
    @JsonProperty(defaultValue = "false")
    @Column
    private boolean browsable;

    @ApiModelProperty(position = 10)
    @JsonProperty
    @OneToMany
    private List<Study> childStudies;

    @OneToMany(mappedBy = "study",fetch = FetchType.EAGER)
    private List<Analysis> analyses;

    @ManyToMany
    private List<WebResource> resources;

    @ManyToMany
    private List<Publication> publications;

    public Study() {
    }

    public Study(AccessionVersionId accessionVersionId, String name, String description, String center,
                 LocalDate releaseDate) {
        this.accessionVersionId = accessionVersionId;
        this.name = name;
        this.description = description;
        this.center = center;
        this.releaseDate = releaseDate;
    }

    @Override
    public Long getId() {
        return id;
    }

    public AccessionVersionId getAccessionVersionId() {
        return accessionVersionId;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<Study> getChildStudies() {
        return childStudies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    public boolean isBrowsable() {
        return browsable;
    }

    public List<Analysis> getAnalyses() {
        return analyses;
    }

    public void setAnalysis(Analysis analysis) {
        List<Analysis> analyses = this.getAnalyses();
        if (analyses == null) {
            analyses = new ArrayList<>();
        }
        analyses.add(analysis);
        this.analyses = analyses;
    }

    public List<WebResource> getResources() {
        return resources;
    }

    public void setResources(List<WebResource> resources) {
        this.resources = resources;
    }

    public void setResource(WebResource resource) {
        List<WebResource> resources = this.getResources();
        if (resources == null) {
            resources = new ArrayList<>();
        }
        resources.add(resource);
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    /**
     * Release date control for Study.
     * @return the date at which Study should become available.
     */
    @Override
    public LocalDate getReleaseDate() {
        return releaseDate;
    }

}
