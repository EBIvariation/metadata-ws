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

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QTaxonomyTree;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.TaxonomyTree;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyTreeRepository;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyTreeServiceImpl implements TaxonomyTreeService {

    @Autowired
    private TaxonomyTreeRepository taxonomyTreeRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Override
    public List<TaxonomyTree> findTaxonomyTreesById(long taxonomyId) {
        QTaxonomyTree qTaxonomyTree = QTaxonomyTree.taxonomyTree;
        return (List<TaxonomyTree>) taxonomyTreeRepository.findAll(qTaxonomyTree.taxonomySpecies.taxonomyId.eq(taxonomyId)
                .or(qTaxonomyTree.taxonomyGenus.isNotNull().and(qTaxonomyTree.taxonomyGenus.taxonomyId.eq(taxonomyId)))
                .or(qTaxonomyTree.taxonomyClass.isNotNull().and(qTaxonomyTree.taxonomyClass.taxonomyId.eq(taxonomyId)))
                .or(qTaxonomyTree.taxonomyOrder.isNotNull().and(qTaxonomyTree.taxonomyOrder.taxonomyId.eq(taxonomyId))));
    }

    @Override
    public List<TaxonomyTree> findTaxonomyTreesByName(String taxonomyName) {
        Taxonomy taxonomy = taxonomyRepository.findByName(taxonomyName);
        if (taxonomy == null) {
            return new ArrayList<>();
        }
        return findTaxonomyTreesById(taxonomy.getTaxonomyId());
    }
}
