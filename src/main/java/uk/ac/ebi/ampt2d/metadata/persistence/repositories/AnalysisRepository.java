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

import io.swagger.annotations.ApiOperation;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QAnalysis;

import java.util.List;

@RepositoryRestResource
public interface AnalysisRepository extends PagingAndSortingRepository<Analysis, Long>,
        QueryDslPredicateExecutor<Analysis>, QuerydslBinderCustomizer<QAnalysis> {

    default void customize(QuerydslBindings bindings, QAnalysis qAnalysis) {
        bindings.bind(qAnalysis.platform).first((path, value) -> path.equalsIgnoreCase(value));
    }

    @ApiOperation(value = "Get the latest version of Analysis based on accession")
    @RestResource(path = "/accession")
    List<Analysis> findFirstByAccessionVersionId_AccessionOrderByAccessionVersionId_VersionDesc
            (@Param("accession") String accession);
}
