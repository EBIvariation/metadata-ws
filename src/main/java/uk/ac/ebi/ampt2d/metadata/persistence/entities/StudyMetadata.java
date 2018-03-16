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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Size;
import java.util.List;

public class StudyMetadata {

    public enum Type {

        CASE_CONTROL,

        CONTROL_SET,

        CASE_SET,

        COLLECTION,

        TUMOR,

        MATCHED_NORMAL

    }

    @Id
    private Long id;

    @Column(nullable = false)
    @Size(max = 255)
    private String name;

    @Lob
    private byte[] description;

    private Center center;

    private Taxonomy taxonomy;

    private Assembly assembly;

    private List<Samples> samples;

    private Technology technology;

    private Type type;

    private Platform platform;

    private List<WebResources> resources;

    @ManyToMany
    private List<FileMetadata> studies;

}
