/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata.loader.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ena.sra.xml.ANALYSISDocument;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SraAnalysisDocumentAndFileTypeLoaderTest {

    private static final String ANALYSIS_ACCESSION = "ERZ496533";
    private static final String ANALYSIS_INVALID_ACCESSION = "ERZ4965336";
    private static final String ANALYSIS_DOCUMENT_XML = "AnalysisDocument.xml";
    private static final String ANALYSIS_DOCUMENT_NOT_FOUND = "AnalysisDocumentNotFound.xml";

    @Mock
    private RestTemplate restTemplate;

    private SraObjectLoaderByAccession<ANALYSISDocument> sraObjectLoaderByAccession;

    private SraObjectLoaderFromAnalysisDocument<AnalysisFileType> sraObjectLoaderFromAnalysisDocument;

    @Before
    public void restTemplateSetup() throws Exception {
        when(restTemplate.exchange(SraAnalysisDocumentLoader.getEnaApiUrl(),
                HttpMethod.GET, null, String.class, ANALYSIS_ACCESSION)).
                thenReturn(new ResponseEntity(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                        .getResource(ANALYSIS_DOCUMENT_XML).toURI()))), HttpStatus.OK));
        when(restTemplate.exchange(SraAnalysisDocumentLoader.getEnaApiUrl(),
                HttpMethod.GET, null, String.class, ANALYSIS_INVALID_ACCESSION)).
                thenReturn(new ResponseEntity(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                        .getResource(ANALYSIS_DOCUMENT_NOT_FOUND).toURI()))), HttpStatus.OK));
        sraObjectLoaderByAccession = new SraAnalysisDocumentLoader(restTemplate);
        sraObjectLoaderFromAnalysisDocument = new SraAnalysisFileTypeLoader(sraObjectLoaderByAccession);
    }

    @Test
    public void testAnalysisLoad() {
        Map<String, ANALYSISDocument> listOfAnalysisType =
                sraObjectLoaderByAccession.getSraObjects(Arrays.asList(ANALYSIS_ACCESSION));
        assertEquals(1, listOfAnalysisType.size());

        ANALYSISDocument analysisDocument = listOfAnalysisType.get(ANALYSIS_ACCESSION);
        assertEquals(ANALYSIS_ACCESSION, analysisDocument.getANALYSIS().getAccession());
        assertEquals("ERP107353", analysisDocument.getANALYSIS().getSTUDYREF().getAccession());
    }

    @Test
    public void testInvalidAnalysisLoad() {
        Map<String, ANALYSISDocument> listOfAnalysisType =
                sraObjectLoaderByAccession.getSraObjects(Arrays.asList(ANALYSIS_INVALID_ACCESSION));
        assertEquals(0, listOfAnalysisType.size());
    }

    @Test
    public void testAnalysisFileTypeLoad() {
        Map<String, List<AnalysisFileType>> mapOfAnalysisToAnalysisFileTypes =
                sraObjectLoaderFromAnalysisDocument.getSraObjectsFromAnalysisDocument(Arrays.asList(ANALYSIS_ACCESSION));
        assertEquals(1, mapOfAnalysisToAnalysisFileTypes.size());

        List<AnalysisFileType> analysisFileTypes = mapOfAnalysisToAnalysisFileTypes.get(ANALYSIS_ACCESSION);

        assertTrue(analysisFileTypes != null);
        assertEquals(2, analysisFileTypes.size());

        AnalysisFileType analysisFileType1 = analysisFileTypes.get(0);
        assertEquals("ERZ496/ERZ496533/Bailey_ACAN_306.vcf.gz.tbi", analysisFileType1.getFilename());
        assertEquals("59c8ff847a438c8e91288fda1610206d", analysisFileType1.getChecksum());
        assertEquals(AnalysisFileType.Filetype.TABIX, analysisFileType1.getFiletype());

        AnalysisFileType analysisFileType2 = analysisFileTypes.get(1);
        assertEquals("ERZ496/ERZ496533/Bailey_ACAN_306.vcf.gz", analysisFileType2.getFilename());
        assertEquals("e83207c712242079577aa07566e5e32d", analysisFileType2.getChecksum());
        assertEquals(AnalysisFileType.Filetype.VCF, analysisFileType2.getFiletype());
    }

    @Test
    public void testInvalidAnalysisLoadForFiles() {
        Map<String, List<AnalysisFileType>> mapOfAnalysisToAnlaysisFileTypes =
                sraObjectLoaderFromAnalysisDocument.getSraObjectsFromAnalysisDocument(Arrays.asList(ANALYSIS_INVALID_ACCESSION));
        assertEquals(0, mapOfAnalysisToAnlaysisFileTypes.size());
    }

}
