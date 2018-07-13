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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

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
    public Study findOneStudyByAccession(AccessionVersionEntityId id) {
        QStudy study = QStudy.study;
        Predicate predicate = study.id.accession.equalsIgnoreCase(id.getAccession())
                .and(study.id.version.eq(id.getVersion()));

        return findOneStudyByPredicate(predicate);
    }

    @Override
    public List<Study> findStudiesByPredicate(Predicate predicate) {
        return (List<Study>) studyRepository.findAll(predicate);
    }

    @Override
    public List<Study> findStudiesByAccession(String accession) {
        QStudy study = QStudy.study;
        Predicate predicate = study.id.accession.equalsIgnoreCase(accession);

        List<Study> studies = findStudiesByPredicate(predicate);

        studies.sort((new Comparator<Study>() {
            @Override
            public int compare(Study o1, Study o2) {
                return o1.getId().getVersion() - o2.getId().getVersion();
            }
        }).reversed());

        return studies.subList(0, studies.size() > 0 ? 1 : 0);
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
        Predicate predicate = study.taxonomy.id.eq(id).or(study.taxonomy.ancestors.any().id.eq(id));

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
    public Study patch(Study study, String patch) throws Exception {
        Study study1 = objectMapper.readerForUpdating(study).readValue(patch);

        return studyRepository.save(study1);
    }

}
