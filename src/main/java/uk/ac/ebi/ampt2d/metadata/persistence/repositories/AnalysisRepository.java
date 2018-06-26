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
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;

import java.util.List;

@RepositoryRestResource
public interface AnalysisRepository extends CrudRepository<Analysis, AccessionVersionEntityId>,
        QueryDslPredicateExecutor<Analysis> {

    @ApiOperation(value = "Get the latest version of Analysis based on accession")
    @RestResource(path = "/accession")
    List<Analysis> findFirstById_AccessionOrderById_VersionDesc(@Param("accession") String accession);
}
