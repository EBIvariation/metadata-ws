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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class File {

    public enum Type {

        TSV,

        VCF,

        BINARY
    }

    @NotNull
    @JsonProperty
    @Id
    private String hash;

    @NotNull
    @JsonProperty
    @Size(max = 255)
    @Column(nullable = false)
    private String fileName;

    @NotNull
    @JsonProperty
    private long fileSize;

    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ManyToMany
    private List<Study> studies;

}
