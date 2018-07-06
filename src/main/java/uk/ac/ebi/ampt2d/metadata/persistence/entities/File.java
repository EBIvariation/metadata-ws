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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class File extends Auditable {

    public enum Type {

        TSV,

        VCF,

        BINARY
    }

    @ApiModelProperty(position = 1, required = true)
    @Valid
    @EmbeddedId
    private AccessionVersionEntityId id;

    @ApiModelProperty(position = 2, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String hash;

    @ApiModelProperty(position = 3, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 4, required = true)
    @JsonProperty
    private long size;

    @ApiModelProperty(position = 5, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    File() {}

    public File(AccessionVersionEntityId id, String hash, String name, long size, Type type) {
        this.id = id;
        this.hash = hash;
        this.name = name;
        this.size = size;
        this.type = type;
    }

    public AccessionVersionEntityId getId() {
        return id;
    }
}
