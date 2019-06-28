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
import uk.ac.ebi.ena.sra.xml.AssemblyType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SraAssemblyXmlParserTest {

    public static final String ASSEMBLY_ACCESSION_API = "GCA_000002305.1";

    private SraXmlParser<AssemblyType> xmlParser;

    @Before
    public void setUp() {
        xmlParser = new SraAssemblyXmlParser();
    }

    @Test
    public void parseXmlApi() throws XmlException, URISyntaxException, IOException {
        AssemblyType assembly = getAssemblyType(ASSEMBLY_ACCESSION_API, "assembly/AssemblyDocumentAPI.xml");
        assertEquals(ASSEMBLY_ACCESSION_API, assembly.getAccession());
        assertEquals("EquCab2.0", assembly.getAlias());
    }

    @Test(expected = XmlException.class)
    public void parseNotFoundXml() throws XmlException, URISyntaxException, IOException {
        AssemblyType assembly = getAssemblyType(ASSEMBLY_ACCESSION_API, "assembly/AssemblyDocumentNotFound.xml");
    }

    private AssemblyType getAssemblyType(String assemblyAccession, String assemblyDocumentPath)
            throws XmlException, URISyntaxException, IOException {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(assemblyDocumentPath).toURI())));
        return xmlParser.parseXml(xmlString, assemblyAccession);
    }

}
