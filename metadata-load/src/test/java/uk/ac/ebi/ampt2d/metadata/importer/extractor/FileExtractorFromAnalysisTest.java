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
package uk.ac.ebi.ampt2d.metadata.pipeline.loader.extractor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraXmlParser;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileExtractorFromAnalysisTest {

    private static final String ANALYSIS_ACCESSION = "ERZ496533";

    private static final String ANALYSIS_DOCUMENT_API_XML = "AnalysisDocumentApi.xml";

    private static final String ANALYSIS_DOCUMENT_DATABASE_XML = "AnalysisDocumentDatabase1.xml";

    private SraXmlParser<AnalysisType> xmlParser;

    @Mock
    private FileRepository fileRepository;

    private AnalysisType analysisType;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    @Before
    public void setUp() {
        xmlParser = new SraAnalysisXmlParser();
        when(fileRepository.save(anyList())).thenReturn(null);
        fileExtractorFromAnalysis = new FileExtractorFromAnalysis(fileRepository);
    }

    @Test
    public void testFileExtractorFromAnalysisApi() throws Exception {
        analysisType = getAnalysisType(ANALYSIS_DOCUMENT_API_XML);
        List<File> files = fileExtractorFromAnalysis.extractFilesFromAnalysis(analysisType);
        assertEquals(2, files.size());
    }

    @Test
    public void testFileExtractorFromAnalysisDatabase() throws Exception {
        analysisType = getAnalysisType(ANALYSIS_DOCUMENT_DATABASE_XML);
        List<File> files = fileExtractorFromAnalysis.extractFilesFromAnalysis(analysisType);
        assertEquals(1, files.size());
    }

    private AnalysisType getAnalysisType(String xml) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, ANALYSIS_ACCESSION);
    }

}
