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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=API"})
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SraObjectsImporterThroughAPITest {

    @Autowired
    private SraObjectsImporterThroughAPI sraObjectImporter;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Before
    public void setUp() {
        analysisRepository.deleteAll();
        studyRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        sampleRepository.deleteAll();
    }

    @Test
    public void importStudy() throws Exception {
        // This study contains two analyses and one reference sequence
        Study study = sraObjectImporter.importStudy("ERP006576");
        assertNotNull(study);
        assertEquals("ERP006576", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(2014, 8, 4), study.getReleaseDate());
        assertEquals("Sanger Institute Mouse Genomes Project v3", study.getName());

        // Below two studies doesn't have analysis associated with it
        study = sraObjectImporter.importStudy("SRP000392");
        assertNotNull(study);
        assertEquals("SRP000392", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(2010, 2, 26), study.getReleaseDate());
        assertEquals("Isolate from a patient with gastric carcinoma", study.getName());
        assertEquals(1, study.getPublications().size());

        study = sraObjectImporter.importStudy("SRP000118");
        assertNotNull(study);
        assertEquals("SRP000118", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(2010, 2, 26), study.getReleaseDate());
        assertEquals("Reference genome for the Human Microbiome Project", study.getName());
        assertEquals(1, study.getResources().size());

        assertEquals(3, studyRepository.count());
        assertEquals(2, analysisRepository.count());
        assertEquals(21, referenceSequenceRepository.count());
        assertEquals(25, sampleRepository.count());
    }

    @Test
    public void importAnalysisObject() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ496533");
        assertEquals("ERZ496533", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.EXOME_SEQUENCING, analysis.getTechnology());
        assertEquals(2, analysis.getFiles().size());

        analysis = sraObjectImporter.importAnalysis("ERZ015345");
        assertEquals("ERZ015345", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
        assertEquals(7, analysis.getFiles().size());
        List<ReferenceSequence> referenceSequences = analysis.getReferenceSequences();
        assertEquals(2, referenceSequences.size());
        ReferenceSequence referenceSequence = referenceSequences.get(0);
        assertEquals("CM000673", referenceSequence.getAccessions().get(0));
        assertEquals("Homo sapiens chromosome 11, GRCh38 reference primary assembly.", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.GENE, referenceSequence.getType());
        referenceSequence = referenceSequences.get(1);
        assertEquals("GCA_000001405.1", referenceSequence.getAccessions().get(0));
        assertEquals("GRCh37", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.ASSEMBLY, referenceSequence.getType());
        assertEquals(1092, analysis.getSamples().size());

        //studies and analysis aren't imported when source is API and if we start with importAnalysis
        assertEquals(0, studyRepository.count());
        assertEquals(0, analysisRepository.count());
    }

    @Test
    public void importAnalysisObjectWithoutReferenceSequence() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ748187");
        assertEquals("ERZ748187", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
        assertEquals(2, analysis.getFiles().size());
        assertEquals(0, analysis.getReferenceSequences().size());
    }

    @Test
    public void importReferenceSequenceObject() throws Exception {
        ReferenceSequence referenceSequence = sraObjectImporter.importReferenceSequence("GCA_000002305.1");
        assertEquals(Arrays.asList("GCA_000002305.1"), referenceSequence.getAccessions());
        assertEquals("EquCab2.0", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.ASSEMBLY, referenceSequence.getType());
        Taxonomy taxonomy = referenceSequence.getTaxonomy();
        assertEquals(9796, taxonomy.getTaxonomyId());
        assertEquals("Equus caballus", taxonomy.getName());
        assertEquals(1, referenceSequenceRepository.count());
    }

    @Test
    public void importReferenceSequenceTranscriptome() throws Exception {
        ReferenceSequence referenceSequence = sraObjectImporter.importReferenceSequence("GAAA01000000");
        assertEquals(Arrays.asList("GAAA01000000"), referenceSequence.getAccessions());
        assertEquals("Latimeria chalumnae, TSA project GAAA01000000 data", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.TRANSCRIPTOME, referenceSequence.getType());
        Taxonomy taxonomy = referenceSequence.getTaxonomy();
        assertEquals(7897, taxonomy.getTaxonomyId());
        assertEquals("Latimeria chalumnae", taxonomy.getName());
        assertEquals(1, referenceSequenceRepository.count());
    }

    @Test
    public void importSampleObject() throws Exception {
        Sample sample = sraObjectImporter.importSample("ERS000156");
        assertNotNull(sample);
        assertEquals("ERS000156", sample.getAccessionVersionId().getAccession());
        assertEquals("E-TABM-722:mmu5", sample.getName());
    }

}
