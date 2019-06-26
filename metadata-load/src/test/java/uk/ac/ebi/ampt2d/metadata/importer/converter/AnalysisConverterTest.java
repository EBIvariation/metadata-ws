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
import org.springframework.core.convert.converter.Converter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.configuration.MetadataImporterMainApplicationConfiguration;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MetadataImporterMainApplicationConfiguration.class,
        MetadataDatabaseConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties", properties = {"import.source=API"})
public class AnalysisConverterTest {

    private static final String ANALYSIS_DOCUMENT_API_XML = "analysis/AnalysisDocumentAPI.xml";

    private static final String ANALYSIS_DOCUMENT_DATABASE_XML = "analysis/AnalysisDocumentDB.xml";

    private Converter<AnalysisType, Analysis> analysisConverter;

    private SraXmlParser<AnalysisType> analysisTypeSraXmlParser;

    @Before
    public void setUp() {
        analysisTypeSraXmlParser = new SraAnalysisXmlParser();
        analysisConverter = new AnalysisConverter();
    }

    @Test
    public void convertFromApiXml() throws Exception {
        String accession = "ERZ496533";
        AnalysisType analysisType = getAnalysisType(ANALYSIS_DOCUMENT_API_XML, accession);
        Analysis analysis = analysisConverter.convert(analysisType);
        assertNotNull(analysis);
        assertEquals(accession, analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.EXOME_SEQUENCING, analysis.getTechnology());
    }

    @Test
    public void convertFromDBXml() throws Exception {
        String accession = "ERZ000011";
        AnalysisType analysisType = getAnalysisType(ANALYSIS_DOCUMENT_DATABASE_XML, accession);
        Analysis analysis = analysisConverter.convert(analysisType);
        assertNotNull(analysis);
        assertEquals(accession, analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
    }

    private AnalysisType getAnalysisType(String xml, String accession) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return analysisTypeSraXmlParser.parseXml(xmlString, accession);
    }

}