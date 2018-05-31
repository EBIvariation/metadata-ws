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
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class File implements BaseEntity<String> {

    public enum Type {

        TSV,

        VCF,

        BINARY
    }

    @ApiModelProperty(position = 1, value = "File auto generated id", required = true, readOnly = true)
    @GenericGenerator(name = "idGenerator", strategy = "uk.ac.ebi.ampt2d.metadata.persistence.idgenerator.IdGenerator")
    @GeneratedValue(generator = "idGenerator")
    @Id
    private String id;

    @ApiModelProperty(position = 2, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    private String accession;

    @ApiModelProperty(position = 3, required = true)
    @JsonProperty
    @Min(1)
    private int version;

    @ApiModelProperty(position = 4, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String hash;

    @ApiModelProperty(position = 5, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String fileName;

    @ApiModelProperty(position = 6, required = true)
    @JsonProperty
    private long fileSize;

    @ApiModelProperty(position = 7, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    File() {}

    public File(String accession, String hash, int version, long fileSize, Type type, String fileName) {
        this.accession = accession;
        this.hash = hash;
        this.version = version;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    @Override
    public int getVersion() {
        return version;
    }
}
