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
package uk.ac.ebi.ampt2d.metadata.importer.xml;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SraStudyXmlParserTest {

    public static final String STUDY_ACCESSION_API = "ERP015186";

    public static final String STUDY_ACCESSION_DB = "ERP000332";

    private SraXmlParser<StudyType> xmlParser;

    @Before
    public void setUp() {
        xmlParser = new SraStudyXmlParser();
    }

    @Test
    public void parseXmlApi() throws XmlException, URISyntaxException, IOException {
        StudyType study = getStudyType(STUDY_ACCESSION_API, "study/StudyDocumentAPI.xml");
        assertEquals("S. mansoni pop genomics", study.getAlias());
        assertEquals(STUDY_ACCESSION_API, study.getAccession());
    }

    @Test
    public void parseXmlDatabase() throws XmlException, URISyntaxException, IOException {
        StudyType study = getStudyType(STUDY_ACCESSION_DB, "study/StudyDocumentDB.xml");
        assertEquals("Breast Cancer Follow Up Series-sc-2010-09-08", study.getAlias());
        assertEquals(STUDY_ACCESSION_DB, study.getAccession());
    }

    @Test(expected = XmlException.class)
    public void parseNotFoundXml() throws XmlException, URISyntaxException, IOException {
        StudyType study = getStudyType(STUDY_ACCESSION_DB, "study/StudyDocumentNotFound.xml");
    }

    private StudyType getStudyType(String studyAccession, String studyDocumentPath)
            throws XmlException, URISyntaxException, IOException {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(studyDocumentPath).toURI())));
        return xmlParser.parseXml(xmlString, studyAccession);
    }

}