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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaxonomyServiceImpl implements TaxonomyService {

    public static final String CLASS = "class";

    public static final String ORDER = "order";

    public static final String GENUS = "genus";

    public static final String SPECIES = "species";

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Override
    public List<Long> findAllTaxonomiesInATreeByTaxonomyIds(long taxonomyId) {
        List<Long> taxonomyIds = new ArrayList<>();
        List<Taxonomy> taxonomies;
        Taxonomy taxonomy = taxonomyRepository.findByTaxonomyId(taxonomyId);
        if (taxonomy == null) {
            return taxonomyIds;
        }
        taxonomyIds.add(taxonomyId);
        String rank = taxonomy.getRank();
        switch (rank) {
            case CLASS:
                taxonomies = taxonomyRepository.findByTaxonomyClass_TaxonomyId(taxonomyId);
                break;
            case ORDER:
                taxonomies = taxonomyRepository.findByTaxonomyOrder_TaxonomyId(taxonomyId);
                break;
            case GENUS:
                taxonomies = taxonomyRepository.findByTaxonomyGenus_TaxonomyId(taxonomyId);
                break;
            case SPECIES:
                taxonomies = taxonomyRepository.findByTaxonomySpecies_TaxonomyId(taxonomyId);
                break;
            default:
                return taxonomyIds;
        }
        taxonomyIds.addAll(taxonomies.parallelStream().map(Taxonomy::getTaxonomyId).collect(Collectors.toList()));
        return taxonomyIds;
    }

    @Override
    public List<Long> findAllTaxonomiesInATreeByTaxonomyName(String taxonomyName) {
        Taxonomy taxonomy = taxonomyRepository.findByName(taxonomyName);
        if (taxonomy == null) {
            return new ArrayList<>();
        }
        return findAllTaxonomiesInATreeByTaxonomyIds(taxonomy.getTaxonomyId());
    }
}
