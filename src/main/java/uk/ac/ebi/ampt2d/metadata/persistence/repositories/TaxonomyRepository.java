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
package uk.ac.ebi.ampt2d.metadata.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;

import java.util.List;

@RepositoryRestResource
public interface TaxonomyRepository extends CrudRepository<Taxonomy, Long> {

    @RestResource(path = "findById")
    List<Taxonomy> findById(@Param("id") long id );

    @RestResource(path = "findByName")
    List<Taxonomy> findByName(@Param("name") String name );

    @RestResource(path = "findByAncestorsId")
    List<Taxonomy> findByAncestorsId(@Param("id") long id );

    @RestResource(path = "findByAncestorsName")
    List<Taxonomy> findByAncestorsName(@Param("name") String name );

    @RestResource(path = "findByIdOrAncestorsId")
    List<Taxonomy> findByIdOrAncestorsId(@Param("id") long id,
                                         @Param("ancestorsId") long ancestorsId);

    @RestResource(path = "findByNameOrAncestorsName")
    List<Taxonomy> findByNameOrAncestorsName(@Param("name") String name,
                                             @Param("ancestorsName") String ancestorsName);

}
