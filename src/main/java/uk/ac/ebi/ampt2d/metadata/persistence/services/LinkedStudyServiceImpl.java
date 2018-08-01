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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QLinkedStudy;
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

    private List<LinkedStudy> findLinkedStudies(AccessionVersionEntityId id) {
        QLinkedStudy linkedStudy = QLinkedStudy.linkedStudy1;
        Predicate predicate = linkedStudy.study.id.eq(id).or(linkedStudy.linkedStudy.id.eq(id));

        return (List<LinkedStudy>) linkedStudyRepository.findAll(predicate);
    }

    private List<AccessionVersionEntityId> findLinkedStudyIds(AccessionVersionEntityId id) {
        Set<AccessionVersionEntityId> ids = new HashSet<>();
        Set<AccessionVersionEntityId> seen = new HashSet<>();
        Stack<AccessionVersionEntityId> stack = new Stack<>();

        seen.add(id);
        stack.push(id);

        while ( !stack.empty() ) {
            AccessionVersionEntityId top = stack.pop();
            List<LinkedStudy> linkedStudies = findLinkedStudies(top);

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
        Study study = studyRepository.findOne(studyId);
        Study linkedStudy = studyRepository.findOne(linkedStudyId);

        if ( study == null || linkedStudy == null) {
            return null;
        }

        LinkedStudyId linkedStudyId1 = new LinkedStudyId(studyId, linkedStudyId);
        LinkedStudy linkedStudy1 = linkedStudyRepository.findOne(linkedStudyId1);

        if ( linkedStudy1 != null ) {
            return linkedStudy1;
        }

        LinkedStudyId linkedStudyId2 = new LinkedStudyId(linkedStudyId, studyId);
        LinkedStudy linkedStudy2 = linkedStudyRepository.findOne(linkedStudyId2);

        if ( linkedStudy2 != null ) {
            return linkedStudy2;
        }

        return null;
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
        List<LinkedStudy> linkedStudies = findLinkedStudies(id);

        linkedStudyRepository.delete(linkedStudies);
    }

    @Override
    public void delete(AccessionVersionEntityId id, AccessionVersionEntityId linkedStudyId) {
        LinkedStudy linkedStudy1 = findOne(id, linkedStudyId);

        if ( linkedStudy1 != null ) {
            linkedStudyRepository.delete(linkedStudy1);
        }

        LinkedStudy linkedStudy2 = findOne(linkedStudyId, id);

        if ( linkedStudy2 != null ) {
            linkedStudyRepository.delete(linkedStudy2);
        }
    }
}
