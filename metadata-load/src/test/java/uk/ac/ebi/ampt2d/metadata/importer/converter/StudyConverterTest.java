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

package uk.ac.ebi.ampt2d.metadata.importer.converter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.SraApiConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.PersistenceApplicationRunnerConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraStudyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PersistenceApplicationRunnerConfiguration.class,
        MetadataDatabaseConfiguration.class,
        SraApiConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties", properties = {"import.source=API"})
public class StudyConverterTest {

    private static final String STUDY_DOCUMENT_API_XML = "study/studyDocumentAPI";

    private static final String STUDY_DOCUMENT_DATABASE_XML = "study/studyDocumentDB";

    private SraXmlParser<StudyType> xmlParser;

    @Autowired
    private StudyConverter studyConverter;

    @Before
    public void setUp() {
        xmlParser = new SraStudyXmlParser();
    }

    @Test
    public void convertFromApiXml() throws Exception {
        String studyAccession = "ERP015186";
        StudyType studyType = getStudyType(STUDY_DOCUMENT_API_XML, studyAccession);
        Study study = studyConverter.convert(studyType);

        Assert.assertNotNull(study);
        Assert.assertEquals(studyAccession, study.getAccessionVersionId().getAccession());
        Assert.assertEquals("Whole genome resequencing of the human parasite Schistosoma mansoni reveals population history\n" +
                "                and effects of selection", study.getName());
        Assert.assertEquals(LocalDate.parse("2016-04-20"), study.getReleaseDate());
        Assert.assertEquals("Wellcome Trust Sanger Institute", study.getCenter());

    }

    @Test
    public void convertFromDbXml() throws Exception {
        String studyAccession = "ERP000332";
        StudyType studyType = getStudyType(STUDY_DOCUMENT_DATABASE_XML, studyAccession);
        Study study = studyConverter.convert(studyType);

        Assert.assertNotNull(study);
        Assert.assertEquals(studyAccession, study.getAccessionVersionId().getAccession());
        Assert.assertEquals("Breast Cancer Follow Up Series", study.getName());
        Assert.assertEquals(LocalDate.parse("9999-12-31"), study.getReleaseDate());
        Assert.assertEquals("SC", study.getCenter());

    }

    private StudyType getStudyType(String xml, String accession) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, accession);
    }

}