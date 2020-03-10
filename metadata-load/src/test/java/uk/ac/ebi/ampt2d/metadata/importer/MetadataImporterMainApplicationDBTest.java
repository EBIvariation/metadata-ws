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

package uk.ac.ebi.ampt2d.metadata.importer;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.database.OracleDbCategory;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraObjectsImporterThroughDatabase;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = "import.source=DB")
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@EnableRetry
public class MetadataImporterMainApplicationDBTest {

    @Autowired
    private MetadataImporterMainApplication metadataImporterMainApplication;

    @Autowired
    private SraObjectsImporterThroughDatabase sraObjectsImporterThroughDatabase;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Test
    @Category(OracleDbCategory.class)
    public void run() throws Exception {
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=src/test/resources/analysis/EgaAnalysisAccessions.txt"}));
        assertEquals(2, analysisRepository.count());

        assertEquals(2, studyRepository.count());
        assertEquals(2, referenceSequenceRepository.count());
        assertEquals(2, sampleRepository.count());

        /*
         *  Two taxonomies are imported one for referenceSequence and one for sample
         *  4081 (Solanum pennellii) and 28526(Solanum lycopersicum) both share the same parent but does not have a
         *   Class.
         *
         *  4069(Solanales) - order
         *      4107(Solanum) - genus
         *          4081 (Solanum pennellii) and 28526(Solanum lycopersicum)
         */

        assertEquals(4, taxonomyRepository.count());

        sraObjectsImporterThroughDatabase.getAccessionsToStudy().clear();

        // Import additional analyses into already imported study
        metadataImporterMainApplication.run(new DefaultApplicationArguments(
                new String[]{"--accessions.file.path=src/test/resources/analysis/EgaAnalysisAccessionsSharedStudyPreviousImport.txt"}));
        assertEquals(2, studyRepository.count());
        assertEquals(4, analysisRepository.count());
        assertEquals(4, sampleRepository.count());
    }

}
