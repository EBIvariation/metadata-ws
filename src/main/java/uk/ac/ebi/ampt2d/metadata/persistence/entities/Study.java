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
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class Study {

    public enum Type {

        CASE_CONTROL,

        CONTROL_SET,

        CASE_SET,

        COLLECTION,

        TUMOR,

        MATCHED_NORMAL

    }

    public enum Technology {

        GWAS,

        EXOME_SEQUENCING,

        GENOTYPING,

        ARRAY,

        CURATION

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Size(max = 255, min = 1)
    @JsonProperty
    @Column(nullable = false)
    private String name;

    @NotNull
    @NotBlank
    @JsonProperty
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Size(max = 255, min = 1)
    @JsonProperty
    @Column(nullable = false)
    private String center;

    @ManyToOne
    private Taxonomy taxonomy;

    @ManyToOne
    private Assembly assembly;

    @OneToMany
    private List<Sample> samples;

    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Technology technology;

    @NotNull
    @JsonProperty
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @NotNull
    @Size(max = 255)
    @JsonProperty
    @Column(nullable = false)
    private String platform;

    @OneToMany
    private List<WebResource> resources;

    @ManyToMany
    private List<File> studies;

}
