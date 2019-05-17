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

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=DB"})
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
public class SraObjectsImporterThroughDBTest {

    @Autowired
    private ObjectsImporter sraObjectImporter;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Before
    public void setUp() {
        analysisRepository.deleteAll();
        studyRepository.deleteAll();
    }

    @Test
    @Category(OracleDbCategory.class)
    public void importStudy() throws Exception {
        Study study = sraObjectImporter.importStudy("ERP000860");
        assertNotNull(study);
        assertEquals("ERP000860", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(9999, 12, 31), study.getReleaseDate());
        assertEquals("UK10K_OBESITY_SCOOP", study.getName());

        study = sraObjectImporter.importStudy("ERP000858");
        assertNotNull(study);
        assertEquals("ERP000858", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(9999, 12, 31), study.getReleaseDate());
        assertEquals("UK10K_NEURO_MUIR", study.getName());
        assertEquals(2, studyRepository.count());
        assertEquals(0, analysisRepository.count()); //analyses aren't imported when source is database
    }

    @Test
    @Category(OracleDbCategory.class)
    public void importAnalysisObject() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ000011");
        assertNotNull(analysis);
        assertEquals("ERZ000011", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
        assertEquals(1, analysis.getFiles().size());
        assertEquals("ERP000860", analysis.getStudy().getAccessionVersionId().getAccession());
    }

}