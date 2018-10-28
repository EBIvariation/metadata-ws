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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"db", "accession", "version"})})
public class CrossReference extends Auditable<Long> implements Comparable<CrossReference> {

    @ApiModelProperty(position = 1, value = "Cross reference auto generated id", required = true, readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ApiModelProperty(position = 2, required = true)
    @Embedded
    private DbAccessionVersionId dbAccessionVersionId;

    @ApiModelProperty(position = 3)
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column
    private String label;

    @ApiModelProperty(position = 4)
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column
    private String url;

    public Long getId() {
        return id;
    }

    public DbAccessionVersionId getDbAccessionVersionId() {
        return dbAccessionVersionId;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossReference that = (CrossReference) o;
        return dbAccessionVersionId != null && Objects.equals(dbAccessionVersionId, that.dbAccessionVersionId);
    }

    @Override
    public int hashCode() {
        return dbAccessionVersionId.hashCode();
    }

    @Override
    public int compareTo(CrossReference o) {
        return dbAccessionVersionId.compareTo(o.getDbAccessionVersionId());
    }

}
