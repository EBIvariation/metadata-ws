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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=API"})
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SraObjectsImporterThroughApiTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private SraObjectsImporterThroughApi sraObjectImporter;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Before
    public void setUp() {
        analysisRepository.deleteAll();
        studyRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        sampleRepository.deleteAll();
        taxonomyRepository.deleteAll();
    }

    @Test
    public void importStudy() throws Exception {
        // This study contains two analyses with referenceSequences
        Study study = sraObjectImporter.importStudy("ERP006576");
        assertNotNull(study);
        assertEquals("ERP006576", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(2014, 8, 4), study.getReleaseDate());
        assertEquals("Sanger Institute Mouse Genomes Project v3", study.getName());
        assertEquals(2, study.getAnalyses().size());

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

        expectedException.expect(AnalysisWithoutReferenceSequenceException.class);
        sraObjectImporter.importStudy("ERP001296");
    }

    @Test
    public void importAnalysisObject() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ496533");
        assertEquals("ERZ496533", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.EXOME_SEQUENCING, analysis.getTechnology());
        assertEquals(2, analysis.getFiles().size());

        analysis = sraObjectImporter.importAnalysis("ERZ094050");
        assertEquals("ERZ094050", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
        assertEquals(5, analysis.getFiles().size());
        List<ReferenceSequence> referenceSequences = analysis.getReferenceSequences();
        assertEquals(24, referenceSequences.size());
        assertEquals(1, analysis.getSamples().size());

        //studies and analysis aren't imported when source is API and if we start with importAnalysis
        assertEquals(0, studyRepository.count());
        assertEquals(0, analysisRepository.count());
    }

    @Test(expected = AnalysisWithoutReferenceSequenceException.class)
    public void importAnalysisObjectWithoutReferenceSequence() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ000001");
        assertNull(analysis);
    }

    @Test
    public void importReferenceSequenceObject() throws Exception {
        ReferenceSequence referenceSequence = sraObjectImporter.importReferenceSequence("GCA_000002305.1", "assembly");
        assertEquals("GCA_000002305.1", referenceSequence.getAccession());
        assertEquals("EquCab2.0", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.GENOME_ASSEMBLY, referenceSequence.getType());
        Taxonomy taxonomy = referenceSequence.getTaxonomy();
        assertEquals(9796, taxonomy.getTaxonomyId());
        assertEquals("Equus caballus", taxonomy.getName());
        assertEquals(1, referenceSequenceRepository.count());
    }

    @Test
    public void importReferenceSequenceTranscriptome() throws Exception {
        ReferenceSequence referenceSequence = sraObjectImporter.importReferenceSequence("GAAA01000000", "nuccore");
        assertEquals("GAAA01000000", referenceSequence.getAccession());
        assertEquals("TSA: Latimeria chalumnae, transcriptome shotgun assembly", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.TRANSCRIPTOME_SHOTGUN_ASSEMBLY, referenceSequence.getType());
        Taxonomy taxonomy = referenceSequence.getTaxonomy();
        assertEquals(7897, taxonomy.getTaxonomyId());
        assertEquals("Latimeria chalumnae", taxonomy.getName());
        assertEquals(1, referenceSequenceRepository.count());
    }

    @Test
    public void importReferenceSequenceGcfAssemblyAccessionWithApiKey() throws Exception {
        ReferenceSequence referenceSequence = sraObjectImporter.importReferenceSequence("GCF_000001405.12", "assembly");
        assertEquals("GCF_000001405.12", referenceSequence.getAccession());
        assertEquals("NCBI36", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.GENOME_ASSEMBLY, referenceSequence.getType());
        Taxonomy taxonomy = referenceSequence.getTaxonomy();
        assertEquals(9606, taxonomy.getTaxonomyId());
        assertEquals("Homo sapiens", taxonomy.getName());
        assertEquals(1, referenceSequenceRepository.count());

        referenceSequence = sraObjectImporter.importReferenceSequence("GCF_000001405.39", "assembly");
        assertEquals("GCF_000001405.39", referenceSequence.getAccession());
        assertEquals("GRCh38", referenceSequence.getName());
        assertEquals("p13", referenceSequence.getPatch());
        assertEquals(ReferenceSequence.Type.GENOME_ASSEMBLY, referenceSequence.getType());
        taxonomy = referenceSequence.getTaxonomy();
        assertEquals(9606, taxonomy.getTaxonomyId());
        assertEquals("Homo sapiens", taxonomy.getName());
        assertEquals(2, referenceSequenceRepository.count());
    }

    @Test
    public void importSamplesObject() throws Exception {
        SraXmlParser<AnalysisType> analysisTypeSraXmlParser = new SraAnalysisXmlParser();
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource("analysis/AnalysisDocumentBig.xml").toURI())));
        AnalysisType analysisType = analysisTypeSraXmlParser.parseXml(xmlString, "ERZ015710");
        List<Sample> sample = sraObjectImporter.importSamples(analysisType);
        // The assertions here are different from the ones in `SraObjectsImporterThroughDB`, despite the same analysis
        // with the same samples is being imported. This is because in the API case, the samples are being imported in
        // the order in which they appear in the XML; whereas in the DB case the information is obtained via a bulk SQL
        // query, where the samples are ordered by their accession.
        assertEquals(1092, sample.size());
        assertEquals("SRS000621", sample.get(0).getAccessionVersionId().getAccession());
        assertEquals("NA12286", sample.get(0).getName());
        assertEquals("SAMN00801378", sample.get(0).getBioSampleAccession());
        assertEquals("SRS003719", sample.get(1091).getAccessionVersionId().getAccession());
        assertEquals("NA19776", sample.get(1091).getName());
        assertEquals("SAMN00007785", sample.get(1091).getBioSampleAccession());
        assertEquals(1092, sampleRepository.count());
        sample.clear();
        sample = sraObjectImporter.importSamples(analysisType);
        assertEquals(1092, sample.size());
        assertEquals("SRS000621", sample.get(0).getAccessionVersionId().getAccession());
        assertEquals("NA12286", sample.get(0).getName());
        assertEquals("SAMN00801378", sample.get(0).getBioSampleAccession());
        assertEquals("SRS003719", sample.get(1091).getAccessionVersionId().getAccession());
        assertEquals("NA19776", sample.get(1091).getName());
        assertEquals("SAMN00007785", sample.get(1091).getBioSampleAccession());
        assertEquals(1092, sampleRepository.count());
    }

    @Test
    public void importSampleObject() throws Exception {
        Sample sample = sraObjectImporter.importSample("ERS000156");
        assertNotNull(sample);
        assertEquals("ERS000156", sample.getAccessionVersionId().getAccession());
        assertEquals("E-TABM-722:mmu5", sample.getName());
        assertEquals("SAMEA907007", sample.getBioSampleAccession());
    }

}
