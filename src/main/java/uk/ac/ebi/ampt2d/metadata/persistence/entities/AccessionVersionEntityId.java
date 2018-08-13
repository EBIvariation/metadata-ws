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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
public class AccessionVersionEntityId implements Serializable {

    @Size(min = 1, max = 255)
    @NotNull
    private String accession;

    @Min(1)
    private int version;

    AccessionVersionEntityId() {
    }

    public AccessionVersionEntityId(String accession, int version) {
        this.accession = accession;
        this.version = version;
    }

    public AccessionVersionEntityId(String id) {
        try {
            String[] entityId = id.split("\\.");
            this.accession = entityId[0];
            this.version = Integer.parseInt(entityId[1]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Please provide an ID in the form accession.version");
        }
    }

    public String getAccession() {
        return accession;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessionVersionEntityId accessionVersionEntityId = (AccessionVersionEntityId) o;

        if (getVersion() != accessionVersionEntityId.getVersion()) return false;
        return getAccession() != null ? getAccession().equals(accessionVersionEntityId.getAccession()) : accessionVersionEntityId.getAccession() == null;
    }

    @Override
    public int hashCode() {
        int result = getAccession() != null ? getAccession().hashCode() : 0;
        result = 31 * result + getVersion();
        return result;
    }

    @Override
    public String toString() {
        return accession + "." + version;
    }
}
