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

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.LinkedStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.LinkedStudyId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.LinkedStudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;


public class LinkedStudyServiceImpl implements LinkedStudyService {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private LinkedStudyRepository linkedStudyRepository;

    /**
     * Get a list of ids for all the studies that could be linked to a given study.
     * The linkages are transitive, e.g.: if A linked to B and B linked to C then A is also linked to C.
     * @param id
     * @return list of study ids
     */
    private List<AccessionVersionEntityId> findLinkedStudyIds(AccessionVersionEntityId id) {
        Set<AccessionVersionEntityId> ids = new HashSet<>();
        Set<AccessionVersionEntityId> seen = new HashSet<>();
        Stack<AccessionVersionEntityId> stack = new Stack<>();

        seen.add(id);
        stack.push(id);

        while ( !stack.empty() ) {
            AccessionVersionEntityId top = stack.pop();
            List<LinkedStudy> linkedStudies = linkedStudyRepository.findByStudy_IdOrLinkedStudy_Id(top, top);

            for ( LinkedStudy linkedStudy : linkedStudies ) {
                Arrays.asList(linkedStudy.getStudy().getId(), linkedStudy.getLinkedStudy().getId())
                        .forEach(studyId->{
                            if ( !top.equals(studyId) && !seen.contains(studyId) ) {
                                ids.add(studyId);
                                seen.add(studyId);
                                stack.push(studyId);
                            }
                        });
            }
        }

        return ids.stream().collect(Collectors.toList());
    }

    @Override
    public LinkedStudy findOne(AccessionVersionEntityId studyId, AccessionVersionEntityId linkedStudyId) {
        LinkedStudy linkedStudy = linkedStudyRepository.findOne(new LinkedStudyId(studyId, linkedStudyId));
        if ( linkedStudy != null ) {
            return linkedStudy;
        }

        return linkedStudyRepository.findOne(new LinkedStudyId(linkedStudyId, studyId));
    }

    @Override
    public List<Study> findLinkedStudiesById(AccessionVersionEntityId id) {
        List<AccessionVersionEntityId> ids = findLinkedStudyIds(id);

        QStudy study = QStudy.study;
        Predicate predicate = study.id.in(ids);

        return (List<Study>) studyRepository.findAll(predicate);
    }

    @Override
    public List<LinkedStudy> save(AccessionVersionEntityId id, List<AccessionVersionEntityId> linkedStudyIds) {
        Study study = studyRepository.findOne(id);

        if ( study == null ) {
            return Arrays.asList();
        }

        List<LinkedStudy> linkedStudies = linkedStudyIds.stream().map(linkedStudyId -> {
            if ( !id.equals(linkedStudyId) ) {
                Study linkedStudy = studyRepository.findOne(linkedStudyId);
                if ( linkedStudy != null ) {
                    return new LinkedStudy(study, linkedStudy);
                }
            }

            return null;
        }).collect(Collectors.toList());

        return (List<LinkedStudy>) linkedStudyRepository.save(linkedStudies);
    }

    @Override
    public void delete(AccessionVersionEntityId id) {
        linkedStudyRepository.deleteLinkedStudiesByStudy_IdOrLinkedStudy_Id(id, id);
    }

    @Override
    public void delete(AccessionVersionEntityId id, AccessionVersionEntityId linkedStudyId) {
        linkedStudyRepository.deleteByStudy_IdAndLinkedStudy_Id(id, linkedStudyId);
        linkedStudyRepository.deleteByStudy_IdAndLinkedStudy_Id(linkedStudyId, id);
    }
}
