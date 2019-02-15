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
package uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SraAnalysisXmlParserTest {

    private SraXmlParser<AnalysisType> xmlParser;

    @Before
    public void setUp() {
        xmlParser = new SraAnalysisXmlParser();
    }

    @Test
    public void parseXmlApi() throws XmlException, URISyntaxException, IOException {
        String analysisAccession = "ERZ496533";
        String analysisDocumentPath = "AnalysisDocumentApi.xml";
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(analysisDocumentPath).toURI())));

        AnalysisType analysis = xmlParser.parseXml(xmlString, analysisAccession);
        assertEquals("DNA sequencing ACAN", analysis.getTITLE());
        assertEquals("GCA_000002305.1",
                            analysis.getANALYSISTYPE().getSEQUENCEVARIATION().getASSEMBLY().getSTANDARD().getAccession());
        assertEquals(2, analysis.getFILES().sizeOfFILEArray());
    }

    @Test
    public void parseXmlDatabase() throws XmlException, URISyntaxException, IOException {
        String analysisAccession = "ERZ496533";
        String analysisDocumentPath = "AnalysisDocumentDatabase.xml";
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(analysisDocumentPath).toURI())));

        AnalysisType analysis = xmlParser.parseXml(xmlString, analysisAccession);
        assertEquals("DNA sequencing ACAN", analysis.getTITLE());
        assertEquals("GCA_000002305.1",
                     analysis.getANALYSISTYPE().getSEQUENCEVARIATION().getASSEMBLY().getSTANDARD().getAccession());
        assertEquals(2, analysis.getFILES().sizeOfFILEArray());
    }

    @Test(expected = XmlException.class)
    public void parseNotFoundXml() throws XmlException, URISyntaxException, IOException {
        String analysisAccession = "ERZ4965336";
        String analysisDocumentPath = "AnalysisDocumentNotFound.xml";
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(analysisDocumentPath).toURI())));

        AnalysisType analysis = xmlParser.parseXml(xmlString, analysisAccession);
    }

}