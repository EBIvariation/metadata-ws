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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StudyServiceImpl implements StudyService {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Study findOneStudyByPredicate(Predicate predicate) {
        return studyRepository.findOne(predicate);
    }

    @Override
    public Study findOneStudyById(Long id) {
        QStudy study = QStudy.study;
        Predicate predicate = study.id.eq(id);
        return findOneStudyByPredicate(predicate);
    }

    @Override
    public List<Study> findStudiesByPredicate(Predicate predicate) {
        return (List<Study>) studyRepository.findAll(predicate);
    }

    @Override
    public Study findStudyByAccession(String accession) {
        QStudy study = QStudy.study;
        Predicate predicate = study.accessionVersionId.accession.equalsIgnoreCase(accession);

        List<Study> studies = findStudiesByPredicate(predicate);

        studies.sort((new Comparator<Study>() {
            @Override
            public int compare(Study o1, Study o2) {
                return o1.getAccessionVersionId().getVersion() - o2.getAccessionVersionId().getVersion();
            }
        }).reversed());

        return studies.isEmpty() ? null : studies.get(0);
    }

    @Override
    public List<Study> findStudiesByReleaseDate(LocalDate from, LocalDate to) {
        QStudy study = QStudy.study;
        Predicate predicate = study.releaseDate.between(from, to);

        return findStudiesByPredicate(predicate);
    }

    @Override
    public List<Study> findStudiesByTextSearch(String searchTerm) {
        QStudy study = QStudy.study;
        Predicate predicate = study.name.containsIgnoreCase(searchTerm).
                or(study.description.containsIgnoreCase(searchTerm));

        return findStudiesByPredicate(predicate);
    }

    @Override
    public List<Study> findStudiesByTaxonomyId(long id) {
        QStudy study = QStudy.study;
        Predicate predicate = study.taxonomy.taxonomyIdentifier.eq(id).
                or(study.taxonomy.ancestors.any().taxonomyIdentifier.eq(id));

        return findStudiesByPredicate(predicate);
    }

    @Override
    public List<Study> findStudiesByTaxonomyName(String name) {
        QStudy study = QStudy.study;
        Predicate predicate = study.taxonomy.name.equalsIgnoreCase(name).
                or(study.taxonomy.ancestors.any().name.equalsIgnoreCase(name));

        return findStudiesByPredicate(predicate);
    }

    @Override
    public List<Study> findLinkedStudies(Long id) {
        Study study = studyRepository.findOne(id);
        if (study == null) {
            return Arrays.asList();
        }

        Set<Study> studies = new HashSet<>();

        List<Study> parents = findParentStudy(study);
        parents.forEach(parent -> {
            studies.add(parent);
            studies.addAll(parent.getChildStudies());
        });

        studies.addAll(study.getChildStudies());

        return studies.stream()
                .filter(study1 -> !study1.getId().equals(id))
                .collect(Collectors.toList());
    }

    private List<Study> findParentStudy(Study child) {
        QStudy study = QStudy.study;
        Predicate predicate = study.childStudies.any().eq(child);

        return findStudiesByPredicate(predicate);
    }

    @Override
    public Study patch(Study study, String patch) throws Exception {
        Study study1 = objectMapper.readerForUpdating(study).readValue(patch);

        return studyRepository.save(study1);
    }

}