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

package uk.ac.ebi.ampt2d.metadata.importer.api;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=API"})
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
public class SraObjectsImporterThroughAPITest {

    @Autowired
    private SraObjectsImporterThroughAPI sraObjectImporter;

    @Test
    public void importStudy() throws Exception {
        Study study = sraObjectImporter.importStudy("ERP000054");
        assertNotNull(study);

        study = sraObjectImporter.importStudy("SRP000392");
        assertNotNull(study);
        assertEquals(1, study.getPublications().size());

        study = sraObjectImporter.importStudy("SRP000118");
        assertNotNull(study);
        assertEquals(1, study.getResources().size());
    }

    @Test
    public void importAnalysisObject() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ496533");
        Assert.assertEquals("ERZ496533", analysis.getAccessionVersionId().getAccession());
        Assert.assertEquals(Analysis.Technology.EXOME_SEQUENCING, analysis.getTechnology());
        Assert.assertEquals(2, analysis.getFiles().size());
    }
}