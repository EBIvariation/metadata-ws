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
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Duo extends Auditable<Long> {

    @ApiModelProperty(position = 1, value = "Data use ontology custom generated id", readOnly = true, required = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GenericGenerator(name = "duo_hash_generator",
            strategy = "uk.ac.ebi.ampt2d.metadata.persistence.idgenerator.DuoIdGenerator")
    @GeneratedValue(generator = "duo_hash_generator")
    private long id;

    @ApiModelProperty(position = 2, dataType = "java.lang.String", notes = "Url to a data use condition")
    @NotNull
    @JsonProperty
    @ManyToOne(optional = false)
    private CrossReference condition;

    @ApiModelProperty(position = 3, dataType = "java.util.List", notes = "Urls to modifiers")
    @ManyToMany
    @JsonProperty
    private List<CrossReference> modifiers;

    public CrossReference getCondition() {
        return condition;
    }

    public List<CrossReference> getModifiers() {
        return modifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Duo duo = (Duo) o;
        if ( condition == null || !condition.equals(duo.getCondition()) ) {
            return false;
        }

        if ( modifiers.size() != duo.getModifiers().size() ) {
            return false;
        }

        List<CrossReference> self = new ArrayList<>(modifiers);
        List<CrossReference> that = new ArrayList<>(duo.getModifiers());
        Collections.sort(self);
        Collections.sort(that);
        return self.equals(that);
    }

    @Override
    public int hashCode() {
        int result = 0;

        if ( condition != null ) {
            result = condition.hashCode();
        }

        if ( modifiers.size() > 0 ) {
            List<CrossReference> copy = new ArrayList<>(modifiers);
            Collections.sort(copy);
            for ( CrossReference cr : copy ) {
                result =  31 * result + cr.hashCode();
            }
        }

        return result;
    }

    public Long getId() {
        return id;
    }
}
