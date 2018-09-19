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
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Assembly  extends Auditable<Long> {

    @ApiModelProperty(position = 1, value = "Assembly auto generated id", required = true, readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ApiModelProperty(position = 2, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 3, required = true)
    @Size(min = 1, max = 255)
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String patch;

    @ApiModelProperty(position = 4, required = true)
    @NotNull
    @JsonProperty
    @ElementCollection
    private List<String> accessions = new ArrayList<String>();

    Assembly() {}

    public Assembly(String name, String patch, List<String> accessions) {
        this.name = name;
        this.patch = patch;
        this.accessions = accessions;
    }

    public Long getId() {
        return id;
    }

}
