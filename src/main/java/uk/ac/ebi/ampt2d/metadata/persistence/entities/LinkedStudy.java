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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "linked_study")
public class LinkedStudy {

    @EmbeddedId
    private LinkedStudyId linkedStudyId;

    @ManyToOne
    @MapsId("study")
    private Study study;

    @ManyToOne
    @MapsId("linkedStudy")
    private Study linkedStudy;

    public LinkedStudy() {
    }

    public LinkedStudy(LinkedStudyId linkedStudyId) {
        this.linkedStudyId = linkedStudyId;
    }

    public LinkedStudy(Study study, Study linkedStudy) {
        this.linkedStudyId = new LinkedStudyId(study.getId(), linkedStudy.getId());
        this.study = study;
        this.linkedStudy = linkedStudy;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setLinkedStudy(Study linkedStudy) {
        this.linkedStudy = linkedStudy;
    }

    public Study getStudy() {
        return study;
    }

    public Study getLinkedStudy() {
        return linkedStudy;
    }
}
