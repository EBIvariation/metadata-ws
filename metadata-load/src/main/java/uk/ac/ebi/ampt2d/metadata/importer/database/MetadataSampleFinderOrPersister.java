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

package uk.ac.ebi.ampt2d.metadata.importer.database;

import uk.ac.ebi.ampt2d.metadata.persistence.entities.QSample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;

public class MetadataSampleFinderOrPersister {

    private static QSample qSample = QSample.sample;

    private SampleRepository SampleRepository;

    public MetadataSampleFinderOrPersister(SampleRepository SampleRepository) {
        this.SampleRepository = SampleRepository;
    }

    public Sample findOrPersistSample(Sample Sample) {
        /* The below find query will make sure to return existing Sample if it exists, otherwise persist a new one. */
        Sample existingSample = SampleRepository.findOne(
                qSample.accessionVersionId.accession.eq(Sample.getAccessionVersionId().getAccession()).and(
                qSample.accessionVersionId.version.eq(Sample.getAccessionVersionId().getVersion()))
        );
        if (existingSample != null) {
            return existingSample;
        }
        return persistSample(Sample);
    }

    public Sample persistSample(Sample Sample) {
        return SampleRepository.save(Sample);
    }
}
