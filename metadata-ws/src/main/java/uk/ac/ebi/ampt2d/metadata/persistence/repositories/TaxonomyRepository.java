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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;

import java.util.List;

@RepositoryRestResource
public interface TaxonomyRepository extends TaxonomyRepositoryCustom, PagingAndSortingRepository<Taxonomy, Long> {

    String findAllTaxonomyTreeByParentTaxonomyIdQuery = "WITH recursive taxonomyHierarchical(taxonomy_id,id,parent_id) AS(" +
            "SELECT taxonomy_id,id,parent_id FROM taxonomy WHERE taxonomy_id= :taxonomyId " +
            "UNION ALL SELECT t2.taxonomy_id,t2.id,t2.parent_id FROM taxonomy t2 " +
            "INNER JOIN taxonomyHierarchical th ON t2.parent_id = th.taxonomy_id) " +
            "SELECT t.* from taxonomy t,taxonomyHierarchical th where t.taxonomy_id=th.taxonomy_id";
    String findAllTaxonomyTreeByParentTaxonomyNameQuery = "WITH recursive taxonomyHierarchical(taxonomy_id,id,parent_id) AS(" +
            "SELECT taxonomy_id,id,parent_id FROM taxonomy WHERE LOWER(name)= LOWER(:taxonomyName) " +
            "UNION ALL SELECT t2.taxonomy_id,t2.id,t2.parent_id FROM taxonomy t2 " +
            "INNER JOIN taxonomyHierarchical th ON t2.parent_id = th.taxonomy_id) " +
            "SELECT t.* from taxonomy t,taxonomyHierarchical th where t.taxonomy_id=th.taxonomy_id";

    Taxonomy findByTaxonomyId(@Param("taxonomyId") long taxonomyId);

    @Query(value = findAllTaxonomyTreeByParentTaxonomyIdQuery, nativeQuery = true)
    List<Taxonomy> findAllTaxonomyTreeByParentTaxonomyId(@Param("taxonomyId") long taxonomyId);

    @Query(value = findAllTaxonomyTreeByParentTaxonomyNameQuery, nativeQuery = true)
    List<Taxonomy> findAllTaxonomyTreeByParentTaxonomyName(@Param("taxonomyName") String taxonomyName);

}
