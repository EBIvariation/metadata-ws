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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataImporterMainApplicationConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraProjectXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Project;
import uk.ac.ebi.ena.sra.xml.ProjectType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MetadataImporterMainApplicationConfiguration.class,
        MetadataDatabaseConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties", properties = {"import.source=API"})
public class ProjectConverterTest {

    private static final String PROJECT_DOCUMENT_API_XML = "project/ProjectDocumentAPI.xml";

    private static final String PROJECT_DOCUMENT_DATABASE_XML = "project/ProjectDocumentDB.xml";

    private SraXmlParser<ProjectType> xmlParser;

    private ProjectConverter projectConverter;

    @Before
    public void setUp() {
        xmlParser = new SraProjectXmlParser();
        projectConverter = new ProjectConverter();
    }

    @Test
    public void convertFromApiXml() throws Exception {
        String projectAccession = "PRJEB6911";
        ProjectType projectType = getProjectType(PROJECT_DOCUMENT_API_XML, projectAccession);
        Project project = projectConverter.convert(projectType);

        assertNotNull(project);
        assertEquals(projectAccession, project.getAccessionVersionId().getAccession());
        assertEquals("Sanger Institute Mouse Genomes Project v3", project.getName());
        assertEquals(LocalDate.parse("2014-08-04"), project.getReleaseDate());
        assertEquals("Wellcome Trust Sanger Institute", project.getCenter());
    }

    @Test
    public void convertFromDbXml() throws Exception {
        String projectAccession = "PRJEB6911";
        ProjectType projectType = getProjectType(PROJECT_DOCUMENT_DATABASE_XML, projectAccession);
        Project project = projectConverter.convert(projectType);
        assertNotNull(project);
        assertEquals(projectAccession, project.getAccessionVersionId().getAccession());
        assertEquals("Sanger Institute Mouse Genomes Project v3", project.getName());
        assertEquals(LocalDate.parse("9999-12-31"), project.getReleaseDate());
        assertEquals("Wellcome Trust Sanger Institute", project.getCenter());
    }

    private ProjectType getProjectType(String xml, String accession) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, accession);
    }

}