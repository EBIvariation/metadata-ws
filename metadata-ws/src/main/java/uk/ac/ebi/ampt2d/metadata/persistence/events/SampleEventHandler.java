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
package uk.ac.ebi.ampt2d.metadata.persistence.events;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeLinkDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeLinkSave;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException;
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;

@RepositoryEventHandler(Sample.class)
public class SampleEventHandler {

    @HandleBeforeCreate
    @HandleBeforeSave
    public void validateTaxonomies(Sample sample) {
        validateTaxonomyLink(sample);
    }

    @HandleBeforeLinkDelete
    @HandleBeforeLinkSave
    public void validateTaxonomies(Sample sample, Object taxonomy) {
        validateTaxonomyLink(sample);
    }

    private void validateTaxonomyLink(Sample sample) {
        if (sample.getTaxonomies() == null || sample.getTaxonomies().size() == 0) {
            throw new SampleWithoutTaxonomyException();
        } else if (sample.getTaxonomies().contains(null)) {
            throw new InvalidTaxonomyException();
        }
    }

}
