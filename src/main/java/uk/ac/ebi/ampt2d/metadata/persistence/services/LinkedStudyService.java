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

    LinkedStudy findOne(AccessionVersionEntityId studyId, AccessionVersionEntityId linkedStudyId);

    List<Study> findLinkedStudiesById(AccessionVersionEntityId id);

    List<LinkedStudy> save(AccessionVersionEntityId id, List<AccessionVersionEntityId> studies);

    void delete(AccessionVersionEntityId id);

    void delete(AccessionVersionEntityId id, AccessionVersionEntityId linkedStudyId);

}
