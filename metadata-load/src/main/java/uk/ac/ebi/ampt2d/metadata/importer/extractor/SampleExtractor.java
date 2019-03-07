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

import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;

import java.util.Arrays;

public class SampleExtractor {
    private static Sample sample;

    public SampleExtractor(SampleRepository sampleRepository, TaxonomyExtractor taxonomyExtractor) {
        sample = sampleRepository.save(new Sample(new AccessionVersionId("ERS000156", 1), "E-TABM-722:mmu5", Arrays
                .asList(taxonomyExtractor.getTaxonomy())));
    }

    public Sample getSample() {
        return sample;
    }

}
