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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DbAccessionVersionId implements Comparable<DbAccessionVersionId>, Serializable {

    @Size(min = 1, max = 32)
    @NotNull
    private String db;

    @Size(min = 1, max = 32)
    @NotNull
    private String accession;

    @Size(min = 1, max = 32)
    private String version;

    DbAccessionVersionId() {
    }

    public DbAccessionVersionId(String db, String accession) {
        this(db, accession, "1");
    }

    public DbAccessionVersionId(String db, String accession, String version) {
        this.db = db;
        this.accession = accession;
        this.version = version;
    }

    public String getDb() {
        return db;
    }

    public String getAccession() {
        return accession;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbAccessionVersionId that = (DbAccessionVersionId) o;
        return Objects.equals(db, that.db) &&
                Objects.equals(accession, that.accession) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = db.hashCode();
        result = 31 * result + accession.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public int compareTo(DbAccessionVersionId o) {
        if ( !db.equals(o.getDb()) ) {
            return db.compareTo(o.getDb());
        }

        if ( !accession.equals(o.getAccession()) ) {
            return accession.compareTo(o.getAccession());
        }

        return version.compareTo(o.getVersion());
    }

    @Override
    public String toString() {
        return db + "_" + accession + "." + version;
    }
}
