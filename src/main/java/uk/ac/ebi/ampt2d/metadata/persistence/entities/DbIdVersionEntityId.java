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
public class DbIdVersionEntityId implements Comparable<DbIdVersionEntityId>, Serializable {

    @Size(min = 1, max = 32)
    @NotNull
    private String db;

    @Size(min = 1, max = 32)
    @NotNull
    private String id;

    @Size(min = 1, max = 32)
    private String version;

    DbIdVersionEntityId() {
    }

    public DbIdVersionEntityId(String db, String id) {
        this(db, id, "1");
    }

    public DbIdVersionEntityId(String db, String id, String version) {
        this.db = db;
        this.id = id;
        this.version = version;
    }

    public String getDb() {
        return db;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbIdVersionEntityId that = (DbIdVersionEntityId) o;
        return Objects.equals(db, that.db) &&
                Objects.equals(id, that.id) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = db.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public int compareTo(DbIdVersionEntityId o) {
        if ( !db.equals(o.getDb()) ) {
            return db.compareTo(o.getDb());
        }

        if ( !id.equals(o.getId()) ) {
            return id.compareTo(o.getId());
        }

        return version.compareTo(o.getVersion());
    }

    @Override
    public String toString() {
        return db + "_" + id + "." + version;
    }
}
