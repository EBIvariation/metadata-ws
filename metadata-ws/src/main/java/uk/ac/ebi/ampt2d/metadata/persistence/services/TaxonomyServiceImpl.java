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

package uk.ac.ebi.ampt2d.metadata.persistence.services;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QTaxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaxonomyServiceImpl implements TaxonomyService {

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Long> findAllSpeciesAndSubspeciesTaxonomyIdsInATaxonomyTreeByTaxonomyId(long taxonomyId) {
        QTaxonomy qTaxonomy = QTaxonomy.taxonomy;
        QTaxonomy qTaxonomySpecies = new QTaxonomy("qTaxonomySpecies");
        QTaxonomy qTaxonomyGenus = new QTaxonomy("qTaxonomyGenus");
        QTaxonomy qTaxonomyOrder = new QTaxonomy("qTaxonomyOrder");
        QTaxonomy qTaxonomyClass = new QTaxonomy("qTaxonomyClass");

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        List<Taxonomy> taxonomies = (List<Taxonomy>) jpaQueryFactory.from(qTaxonomy)
                .leftJoin(qTaxonomy.taxonomySpecies, qTaxonomySpecies)
                .leftJoin(qTaxonomy.taxonomyGenus, qTaxonomyGenus)
                .leftJoin(qTaxonomy.taxonomyOrder, qTaxonomyOrder)
                .leftJoin(qTaxonomy.taxonomyClass, qTaxonomyClass)
                .where(qTaxonomySpecies.taxonomyId.eq(taxonomyId)
                        .or(qTaxonomyOrder.taxonomyId.eq(taxonomyId)
                                .or(qTaxonomyGenus.taxonomyId.eq(taxonomyId))
                                .or(qTaxonomyClass.taxonomyId.eq(taxonomyId)))
                        .or(qTaxonomy.taxonomyId.eq(taxonomyId))).fetch();
        return getSpeciesAndSubspeciesTaxonomyIds(taxonomies);
    }

    @Override
    public List<Long> findAllSpeciesAndSubspeciesTaxonomyIdsInATaxonomyTreeByTaxonomyName(String taxonomyName) {
        Taxonomy taxonomy = taxonomyRepository.findByName(taxonomyName);
        if (taxonomy == null) {
            return new ArrayList<>();
        }
        return findAllSpeciesAndSubspeciesTaxonomyIdsInATaxonomyTreeByTaxonomyId(taxonomy.getTaxonomyId());
    }

    public List<Long> getSpeciesAndSubspeciesTaxonomyIds(List<Taxonomy> taxonomies) {
        return taxonomies.parallelStream().filter(taxonomy -> {
            String rank = taxonomy.getRank();
            if (rank.equals("class") || rank.equals("order") || rank.equals("genus")) {
                return false;
            }
            return true;
        }).map(taxonomy -> taxonomy.getTaxonomyId()).collect(Collectors.toList());
    }
}
