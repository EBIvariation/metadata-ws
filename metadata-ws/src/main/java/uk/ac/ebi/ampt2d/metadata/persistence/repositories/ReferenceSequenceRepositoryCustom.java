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
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;

import java.util.List;

@NoRepositoryBean
public interface ReferenceSequenceRepositoryCustom extends PagingAndSortingRepository<ReferenceSequence, Long>,
        QueryDslPredicateExecutor<ReferenceSequence> {

    ReferenceSequence findByAccessions(@Param("accessions") List<String> accessions);

    default ReferenceSequence findOrSave(ReferenceSequence referenceSequence) {
        ReferenceSequence existingReferenceSequence = findByAccessions(referenceSequence.getAccessions());
        if (existingReferenceSequence != null) {
            return existingReferenceSequence;
        }
        return save(referenceSequence);
    }
}
