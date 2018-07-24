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

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class LinkedStudyId implements Serializable {

    private AccessionVersionEntityId study;

    private AccessionVersionEntityId linkedStudy;

    public LinkedStudyId() {
    }

    public LinkedStudyId(AccessionVersionEntityId study, AccessionVersionEntityId linkedStudy) {
        this.study = study;
        this.linkedStudy = linkedStudy;
    }

    public AccessionVersionEntityId getStudy() {
        return study;
    }

    public void setStudy(AccessionVersionEntityId study) {
        this.study = study;
    }

    public AccessionVersionEntityId getLinkedStudy() {
        return linkedStudy;
    }

    public void setLinkedStudy(AccessionVersionEntityId linkedStudy) {
        this.linkedStudy = linkedStudy;
    }

    @Override
    public int hashCode() {
        return study.hashCode() + linkedStudy.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj == this ) {
            return true;
        }

        if ( obj == null || this.getClass() != obj.getClass() ) {
            return false;
        }

        LinkedStudyId lsi = (LinkedStudyId) obj;

        return study.equals(lsi.getStudy()) && linkedStudy.equals(lsi.getLinkedStudy());
    }
}
