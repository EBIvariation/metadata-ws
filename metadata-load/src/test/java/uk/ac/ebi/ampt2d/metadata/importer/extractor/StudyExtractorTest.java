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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MetadataDatabaseConfiguration.class)
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties")
public class StudyExtractorTest {

    public static final String STUDY_ACCESSION = "ERP000326";

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    private StudyExtractor studyExtractor;

    @Test
    public void testStudyExtract() {
        studyExtractor = new StudyExtractor(studyRepository, new TaxonomyExtractor(taxonomyRepository));
        Study study = studyExtractor.getStudy(STUDY_ACCESSION);
        Assert.assertNotNull(study);
        Assert.assertEquals(1, study.getId().longValue());
        Assert.assertEquals(STUDY_ACCESSION, study.getAccessionVersionId().getAccession());
    }
}
