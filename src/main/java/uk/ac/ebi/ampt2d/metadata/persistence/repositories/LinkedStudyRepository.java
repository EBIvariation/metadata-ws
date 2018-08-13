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
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.LinkedStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.LinkedStudyId;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface LinkedStudyRepository extends CrudRepository<LinkedStudy, LinkedStudyId> {

    List<LinkedStudy> findByStudy_IdOrLinkedStudy_Id(AccessionVersionEntityId studyId, AccessionVersionEntityId linkedStudyId);

    @Transactional
    void deleteByStudy_IdAndLinkedStudy_Id(AccessionVersionEntityId studyId, AccessionVersionEntityId linkedStudyId);

    @Transactional
    void deleteLinkedStudiesByStudy_IdOrLinkedStudy_Id(AccessionVersionEntityId studyId, AccessionVersionEntityId linkedStudyId);

}
