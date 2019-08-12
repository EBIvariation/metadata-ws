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

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Publication extends Auditable<Long>{

    @ApiModelProperty(position = 1, value = "Publication auto generated id", readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ApiModelProperty(position = 2, example = "PubmedId")
    @NotNull
    @JsonProperty
    @Column(unique = true)
    private String publicationId;

    public Publication() {
    }

    public Publication(String publicationId) {
        this.publicationId = publicationId;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Release date control: get the *earliest* release date from all studies which link to this publication.
     */
    @Formula("(SELECT min(study.release_date) FROM study_publications " +
             "INNER JOIN study on study_publications.study_id = study.id " +
             "WHERE study_publications.publications_id=id)")
    @JsonIgnore
    private LocalDate releaseDate;

    /**
     * Get the studyIds.
     */
    @Formula("(SELECT string_agg(concat(study.accession,'.',study.version),',') FROM study_publications " +
            "INNER JOIN study on study_publications.study_id = study.id " +
            "WHERE study_publications.publications_id=id)")
    @JsonIgnore
    private String studyIds;

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String getStudyIds() {
        return studyIds;
    }
}
