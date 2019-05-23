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

package uk.ac.ebi.ampt2d.metadata.importer.extractor;

import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.SampleType;

public class TaxonomyExtractorFromSample {

    private TaxonomyRepository taxonomyRepository;

    public TaxonomyExtractorFromSample(TaxonomyRepository taxonomyRepository) {
        this.taxonomyRepository = taxonomyRepository;
    }

    public Taxonomy getTaxonomy(SampleType.SAMPLENAME sampleName) {
        return findOrCreateTaxonomy(sampleName);
    }

    private Taxonomy findOrCreateTaxonomy(SampleType.SAMPLENAME sampleName) {
        int taxonomyId = sampleName.getTAXONID();
        Taxonomy taxonomy = taxonomyRepository.findByTaxonomyId(taxonomyId);
        if (taxonomy == null) {
            taxonomy = taxonomyRepository.save(new Taxonomy(taxonomyId, sampleName.getSCIENTIFICNAME()));
        }
        return taxonomy;
    }

}
