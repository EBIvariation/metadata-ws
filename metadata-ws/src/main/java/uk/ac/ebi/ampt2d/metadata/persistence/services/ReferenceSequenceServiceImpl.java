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

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

import java.util.List;

public class ReferenceSequenceServiceImpl implements ReferenceSequenceService {

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Override
    public List<ReferenceSequence> findReferenceSequencesByTaxonomyId(long id) {
        List<Taxonomy> taxonomyList = taxonomyRepository.findAllTaxonomyTreeByParentTaxonomyId(id);
        return getReferenceSequences(taxonomyList);
    }

    @Override
    public List<ReferenceSequence> findReferenceSequencesByTaxonomyName(String name) {
        List<Taxonomy> taxonomyList = taxonomyRepository.findAllTaxonomyTreeByParentTaxonomyName(name);
        return getReferenceSequences(taxonomyList);
    }

    public List<ReferenceSequence> getReferenceSequences(List<Taxonomy> taxonomyList) {
        QReferenceSequence referenceSequence = QReferenceSequence.referenceSequence;
        Predicate predicate = referenceSequence.taxonomy.taxonomyId.in(taxonomyList.parallelStream().map(taxonomy ->
                taxonomy.getTaxonomyId()).toArray(Long[]::new));
        return (List<ReferenceSequence>) referenceSequenceRepository.findAll(predicate);
    }

}
