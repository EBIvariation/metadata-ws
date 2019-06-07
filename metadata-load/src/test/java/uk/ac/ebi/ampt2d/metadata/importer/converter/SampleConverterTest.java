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
import uk.ac.ebi.ampt2d.metadata.importer.api.SraApiConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataImporterMainApplicationConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraSampleXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ena.sra.xml.SampleType;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MetadataImporterMainApplicationConfiguration.class,
        MetadataDatabaseConfiguration.class,
        SraApiConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties", properties = {"import.source=API"})
public class SampleConverterTest {

    private static final String SAMPLE_DOCUMENT_API_XML = "sample/SampleDocumentAPI.xml";

    private static final String SAMPLE_DOCUMENT_DATABASE_XML = "sample/SampleDocumentDB.xml";

    private SraXmlParser<SampleType> xmlParser;

    private SampleConverter sampleConverter;

    @Before
    public void setUp() {
        xmlParser = new SraSampleXmlParser();
        sampleConverter = new SampleConverter();
    }

    private void testSample(String sampleAccession, String sampleDocumentXmlPath,
                            String expectedSampleName) throws Exception {
        SampleType sampleType = getSampleType(sampleDocumentXmlPath, sampleAccession);
        Sample sample = sampleConverter.convert(sampleType);
        assertNotNull(sample);
        assertEquals(sampleAccession, sample.getAccessionVersionId().getAccession());
        assertEquals(expectedSampleName, sample.getName());
    }

    @Test
    public void convertFromApiXml() throws Exception {
        testSample("ERS000156", SAMPLE_DOCUMENT_API_XML, "E-TABM-722:mmu5");
    }

    @Test
    public void convertFromDbXml() throws Exception {
        testSample("ERS000002", SAMPLE_DOCUMENT_DATABASE_XML,
                   "Solexa sequencing of Saccharomyces cerevisiae strain SK1 random 200 bp library");
    }

    private SampleType getSampleType(String xml, String accession) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, accession);
    }

}
