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

import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

public class MetadataStudyFinderOrPersister {

    private static QStudy qStudy = QStudy.study;

    private StudyRepository studyRepository;

    public MetadataStudyFinderOrPersister(StudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    public Study findOrPersistStudy(Study study) {
        /* The below find query will make sure to return shared study when analyses sharing same study are imported
          in different runs */
        Study sharedStudy = studyRepository.findOne(qStudy.accessionVersionId.accession.eq(study
                .getAccessionVersionId().getAccession()).and(qStudy.accessionVersionId.version.eq(study
                .getAccessionVersionId().getVersion())));
        if (sharedStudy != null) {
            return sharedStudy;
        }
        return persistStudy(study);
    }

    public Study persistStudy(Study study) {
        return studyRepository.save(study);
    }
}
