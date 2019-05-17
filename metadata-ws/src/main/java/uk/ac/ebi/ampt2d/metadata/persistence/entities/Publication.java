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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Publication extends Auditable<String> {

    @ApiModelProperty(position = 1, value = "PubmedId", required = true)
    @JsonProperty
    @NotNull
    @Id
    private String id;

    public Publication() {
    }

    public Publication(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
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

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
}
