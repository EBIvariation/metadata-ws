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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "linked_study")
@IdClass(LinkedStudyId.class)
public class LinkedStudy {

    @Id
    @ManyToOne
    private Study study;

    @Id
    @ManyToOne
    private Study linkedStudy;

    public LinkedStudy() {
    }

    public LinkedStudy(Study study, Study linkedStudy) {
        this.study = study;
        this.linkedStudy = linkedStudy;
    }

    public Study getStudy() {
        return study;
    }

    public Study getLinkedStudy() {
        return linkedStudy;
    }
}
