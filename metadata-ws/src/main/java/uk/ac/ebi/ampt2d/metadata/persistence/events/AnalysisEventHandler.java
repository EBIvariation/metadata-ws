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
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException;
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;

@RepositoryEventHandler(Analysis.class)
public class AnalysisEventHandler {

    @HandleBeforeCreate
    @HandleBeforeSave
    public void validateReferenceSequences(Analysis analysis) {
        validateReferenceSequenceLink(analysis);
    }

    @HandleBeforeLinkDelete
    @HandleBeforeLinkSave
    public void validateReferenceSequences(Analysis analysis, Object referenceSequence) {
        validateReferenceSequenceLink(analysis);
    }

    private void validateReferenceSequenceLink(Analysis analysis) {
        if (analysis.getReferenceSequences() == null || analysis.getReferenceSequences().size() == 0) {
            throw new AnalysisWithoutReferenceSequenceException();
        } else if (analysis.getReferenceSequences().contains(null)) {
            throw new InvalidReferenceSequenceException();
        } else if (analysis.getReferenceSequences().size() > 1) {
            boolean invalidRefSeq = analysis.getReferenceSequences().stream().anyMatch(r -> !r.getType().equals(ReferenceSequence.Type.GENE));
            if (invalidRefSeq) {
                throw new InvalidReferenceSequenceException("Invalid type of reference sequences. " +
                        "When multiple reference sequence URLs are provided, all of them should point to gene sequences");
            }
        }
    }

}
