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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QTaxonomy;
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
    public List<Study> findStudiesByPredicate(Predicate predicate) {
        return (List<Study>) studyRepository.findAll(predicate);
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
        QTaxonomy taxonomy = QTaxonomy.taxonomy;
        Predicate predicate = taxonomy.id.eq(id).or(taxonomy.ancestors.any().id.eq(id));

        List<Taxonomy> taxonomies = (List<Taxonomy>) taxonomyRepository.findAll(predicate);

        return findStudiesByTaxonomyIn(taxonomies);
    }

    @Override
    public List<Study> findStudiesByTaxonomyName(String name) {
        QTaxonomy taxonomy = QTaxonomy.taxonomy;
        Predicate predicate = taxonomy.name.equalsIgnoreCase(name).or(taxonomy.ancestors.any().name.equalsIgnoreCase(name));

        List<Taxonomy> taxonomies = (List<Taxonomy>) taxonomyRepository.findAll(predicate);

        return findStudiesByTaxonomyIn(taxonomies);
    }

    private List<Study> findStudiesByTaxonomyIn(List<Taxonomy> taxonomies) {
        List<Long> taxonomyIds = taxonomies.stream().map(Taxonomy::getId)
                .collect(Collectors.toList());

        QStudy study = QStudy.study;
        Predicate predicate = study.taxonomy.id.in(taxonomyIds);

        return findStudiesByPredicate(predicate);
    }

}
