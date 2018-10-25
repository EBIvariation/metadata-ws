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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
public class CrossReference extends Auditable<DbIdVersionEntityId> implements Comparable<CrossReference> {

    @ApiModelProperty(position = 1, required = true)
    @EmbeddedId
    private DbIdVersionEntityId id;

    @ApiModelProperty(position = 2)
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column
    private String label;

    @ApiModelProperty(position = 3)
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column
    private String url;

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
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(CrossReference o) {
        return id.compareTo(o.getId());
    }

    public DbIdVersionEntityId getId() {
        return id;
    }

}
