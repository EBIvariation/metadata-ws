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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.time.LocalDate;

public class StudyExtractor {

    private StudyRepository studyRepository;

    private TaxonomyExtractor taxonomyExtractor;

    public StudyExtractor(StudyRepository studyRepository, TaxonomyExtractor taxonomyExtractor) {
        this.studyRepository = studyRepository;
        this.taxonomyExtractor = taxonomyExtractor;
    }

    public Study getStudy(String accession) {
        QStudy qStudy = QStudy.study;
        Study study = studyRepository.findOne(qStudy.accessionVersionId.accession.equalsIgnoreCase(accession).and
                (qStudy.accessionVersionId.version.eq(1)));
        if (study == null) {
            study = studyRepository.save(new Study(new AccessionVersionId(accession, 1), "UK10K_OBESITY_SCOOP",
                    "SCOOP Desription", "SC", LocalDate.of(2015, 10, 2), taxonomyExtractor.getTaxonomy()));
        }
        return study;
    }

}
