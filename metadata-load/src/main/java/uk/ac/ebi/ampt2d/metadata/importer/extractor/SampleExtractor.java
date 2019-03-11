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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QSample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;

import java.util.Arrays;

public class SampleExtractor {
    private SampleRepository sampleRepository;

    private TaxonomyExtractor taxonomyExtractor;

    public SampleExtractor(SampleRepository sampleRepository, TaxonomyExtractor taxonomyExtractor) {
        this.sampleRepository = sampleRepository;
        this.taxonomyExtractor = taxonomyExtractor;
    }

    public Sample getSample(String accession) {
        QSample qSample = QSample.sample;
        Sample sample = sampleRepository.findOne(qSample.accessionVersionId.accession.equalsIgnoreCase(accession).and
                (qSample.accessionVersionId.version.eq(1)));
        if (sample == null) {
            sample = sampleRepository.save(new Sample(new AccessionVersionId(accession, 1),
                    "UK10K_SCOOP5013826-sc-2011-08-18T15:01:15Z-1027679",
                    Arrays.asList(taxonomyExtractor.getTaxonomy())));
        }
        return sample;
    }

}
