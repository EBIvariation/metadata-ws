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
package uk.ac.ebi.ampt2d.metadata.persistence.services;

import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.LinkedStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;

import java.util.List;

public interface LinkedStudyService {

    /**
     * Find one LinkedStudy record for direct link between two studies.
     * @param studyId
     * @param linkedStudyId
     * @return LinkedStudy object or null
     */
    LinkedStudy findOne(AccessionVersionEntityId studyId, AccessionVersionEntityId linkedStudyId);

    /**
     * Find all studies that could be linked to a given study.
     * The linkages are transitive, e.g.: if A linked to B and B linked to C then A is also linked to C.
     * @param id
     * @return list of studies
     */
    List<Study> findLinkedStudiesById(AccessionVersionEntityId id);

    /**
     * Save the link between an id and all the ids from a list.
     * @param id
     * @param studies
     * @return
     */
    List<LinkedStudy> save(AccessionVersionEntityId id, List<AccessionVersionEntityId> studies);

    /**
     * Delete all the links a given study id has been directly associated with.
     * @param id
     */
    void delete(AccessionVersionEntityId id);

    /**
     * Delete the link between two given study ids.
     * @param id
     * @param linkedStudyId
     */
    void delete(AccessionVersionEntityId id, AccessionVersionEntityId linkedStudyId);

}
