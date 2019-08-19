/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.TaxonomyTree;

import java.util.List;

@RepositoryRestResource
public interface TaxonomyTreeRepository extends PagingAndSortingRepository<TaxonomyTree, Long>,
        QueryDslPredicateExecutor<TaxonomyTree> {

    @RestResource(path = "findByTaxonomySpecies")
    TaxonomyTree findByTaxonomySpecies_TaxonomyId(@Param("speciesId") Long speciesId);

    @RestResource(path = "findByTaxonomyClass")
    List<TaxonomyTree> findByTaxonomyClass_TaxonomyId(@Param("classId") Long classId);

    @RestResource(path = "findByTaxonomyOrder")
    List<TaxonomyTree> findByTaxonomyOrder_TaxonomyId(@Param("orderId") Long orderId);

    @RestResource(path = "findByTaxonomyGenus")
    List<TaxonomyTree> findByTaxonomyGenus_TaxonomyId(@Param("genusId") Long genusId);
}
