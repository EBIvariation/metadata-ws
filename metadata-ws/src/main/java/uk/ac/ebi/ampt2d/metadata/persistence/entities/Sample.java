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
import org.hibernate.annotations.Formula;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"accession", "version"}))
@SequenceGenerator(initialValue=1, allocationSize=1 , name="SAMPLE_SEQ", sequenceName="sample_sequence")
public class Sample extends Auditable<Long> {

    @ApiModelProperty(position = 1, value = "Sample auto generated id", required = true, readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="SAMPLE_SEQ")
    private long id;

    @ApiModelProperty(position = 2)
    @Embedded
    @Valid
    private AccessionVersionId accessionVersionId;

    @ApiModelProperty(position = 3, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @Column(nullable = false)
    @JsonProperty
    private String name;

    @ApiModelProperty(position = 4, dataType = "java.lang.String", example = "[url1, url2]", notes = "List of URLs to taxonomies")
    @JsonProperty
    @ManyToMany
    @NotNull
    @Column(nullable = false)
    @Size(min = 1)
    private List<Taxonomy> taxonomies;

    Sample() {
    }

    public Sample(AccessionVersionId accessionVersionId, String name, List<Taxonomy> taxonomies) {
        this.accessionVersionId = accessionVersionId;
        this.name = name;
        this.taxonomies = taxonomies;
    }

    @Override
    public Long getId() {
        return id;
    }

    public AccessionVersionId getAccessionVersionId() {
        return accessionVersionId;
    }

    public List<Taxonomy> getTaxonomies() {
        return taxonomies;
    }

    // Release date control: get the *earliest* release date from all studies which link to this sample
    @Formula("(SELECT min(study.release_date) FROM sample " +
            "INNER JOIN analysis_samples on sample.id = analysis_samples.samples_id " +
            "INNER JOIN analysis on analysis_samples.analysis_id = analysis.id " +
            "INNER JOIN study on analysis.study_id = study.id " +
            "WHERE sample.id=id)")
    private LocalDate releaseDate;

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
}
