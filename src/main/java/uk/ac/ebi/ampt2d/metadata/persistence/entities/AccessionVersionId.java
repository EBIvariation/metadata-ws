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

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Embeddable
public class AccessionVersionId implements Serializable {

    private String accession;

    @ApiModelProperty(example = "1")
    @Min(1)
    private Integer version;

    AccessionVersionId() {
    }

    public AccessionVersionId(String accession, Integer version) {
        this.accession = accession;
        this.version = version;
    }

    public String getAccession() {
        return accession;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessionVersionId accessionVersionId = (AccessionVersionId) o;

        if (getVersion() != accessionVersionId.getVersion()) return false;
        return getAccession() != null ? getAccession().equals(accessionVersionId.getAccession()) : accessionVersionId.getAccession() == null;
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
