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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"accession", "version"}))
@SequenceGenerator(initialValue=1, allocationSize=1 , name="FILE_SEQ", sequenceName="file_sequence")
public class File extends Auditable<Long> {

    public enum Type {

        AGP, BAI, BAM, BCF, BED, BIONANO_NATIVE, CHROMOSOME_LIST, CRAI, CRAM, FASTA, FASTQ, FLATFILE, GFF,
        INFO, KALLISTO_NATIVE, MANIFEST, OTHER, PHENOTYPE_FILE, README_FILE, SAMPLE_LIST, TAB, TABIX,
        TSV, UNLOCALISED_LIST, VCF, VCF_AGGREGATE, WIG
    }

    @ApiModelProperty(position = 1, value = "File auto generated id", readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="FILE_SEQ")
    private Long id;

    @ApiModelProperty(position = 2)
    @Embedded
    @Valid
    private AccessionVersionId accessionVersionId;

    @ApiModelProperty(position = 3, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String hash;

    @ApiModelProperty(position = 4, required = true)
    @NotNull
    @Size(min = 1, max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(position = 5, required = true)
    @JsonProperty
    private long size;

    @ApiModelProperty(position = 6, required = true)
    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    File() {}

    public File(String hash, String name, String type) {
        this.hash = hash;
        this.name = name;
        this.type = Type.valueOf(type);
    }

    public File(AccessionVersionId accessionVersionId, String hash, String name, long size, Type type) {
        this.accessionVersionId = accessionVersionId;
        this.hash = hash;
        this.name = name;
        this.size = size;
        this.type = type;
    }

    @Override
    public Long getId() {
        return id;
    }

    public AccessionVersionId getAccessionVersionId() {
        return accessionVersionId;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }

    // Release date control: get the *earliest* release date from all studies which link to this file
    @Formula("(SELECT min(study.release_date) FROM file " +
             "INNER JOIN analysis_files on file.id = analysis_files.files_id " +
             "INNER JOIN analysis on analysis_files.analysis_id = analysis.id " +
             "INNER JOIN study on analysis.study_id = study.id " +
             "WHERE file.id=id)")
    private LocalDate releaseDate;

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
}
