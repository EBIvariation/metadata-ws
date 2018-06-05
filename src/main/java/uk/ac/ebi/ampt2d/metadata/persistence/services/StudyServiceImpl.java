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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

import java.util.List;
import java.util.stream.Collectors;

public class StudyServiceImpl implements StudyService {

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Override
    public Iterable<Study> findAll(Predicate predicate) {
        return studyRepository.findAll(predicate);
    }

    @Override
    public List<Study> findStudiesByTaxonomyId(long id) {
        List<Taxonomy> taxonomies = taxonomyRepository.findByIdOrAncestorsId(id, id);

        return findStudiesByTaxonomyIn(taxonomies);
    }

    @Override
    public List<Study> findStudiesByTaxonomyName(String name) {
        List<Taxonomy> taxonomies = taxonomyRepository.findByNameOrAncestorsName(name, name);

        return findStudiesByTaxonomyIn(taxonomies);
    }

    private List<Study> findStudiesByTaxonomyIn(List<Taxonomy> taxonomies) {
        List<Long> taxonomyIds = taxonomies.stream().map(Taxonomy::getId)
                .collect(Collectors.toList());

        return studyRepository.findByTaxonomyIdIn(taxonomyIds);
    }

}
