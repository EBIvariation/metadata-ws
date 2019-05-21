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

import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;

public class MetadataReferenceSequenceFinderOrPersister {

    private ReferenceSequenceRepository referenceSequenceRepository;

    public MetadataReferenceSequenceFinderOrPersister(ReferenceSequenceRepository referenceSequenceRepository) {
        this.referenceSequenceRepository = referenceSequenceRepository;
    }

    public ReferenceSequence findOrPersistReferenceSequence(ReferenceSequence referenceSequence) {
        /* The below find query will make sure to return the same reference sequence when analyses sharing same
        reference sequence are imported in different runs */
        ReferenceSequence existingReferenceSequence = referenceSequenceRepository.findByAccessions(
                referenceSequence.getAccessions());
        if (existingReferenceSequence != null) {
            return existingReferenceSequence;
        }
        return persistReferenceSequence(referenceSequence);
    }

    private ReferenceSequence persistReferenceSequence(ReferenceSequence referenceSequence) {
        return referenceSequenceRepository.save(referenceSequence);
    }

}
