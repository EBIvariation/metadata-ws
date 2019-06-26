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
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ena.sra.xml.AssemblyType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MetadataImporterMainApplicationConfiguration.class,
        MetadataDatabaseConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties", properties = {"import.source=API"})
public class ReferenceSequenceConverterTest {

    private static final String ASSEMBLY_DOCUMENT_API_XML = "assembly/AssemblyDocumentAPI.xml";

    private SraXmlParser<AssemblyType> xmlParser;

    private Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter;

    @Before
    public void setUp() {
        xmlParser = new SraAssemblyXmlParser();
        referenceSequenceConverter = new ReferenceSequenceConverter();
    }

    @Test
    public void convertFromApiXml() throws Exception {
        String assemblyAccession = "GCA_000002305.1";
        AssemblyType assemblyType = getAssemblyType(ASSEMBLY_DOCUMENT_API_XML, assemblyAccession);
        ReferenceSequence referenceSequence = referenceSequenceConverter.convert(assemblyType);
        assertNotNull(referenceSequence);
        assertEquals(Arrays.asList(assemblyAccession), referenceSequence.getAccessions());
        assertEquals("EquCab2.0", referenceSequence.getName());
        assertEquals(ReferenceSequence.Type.ASSEMBLY, referenceSequence.getType());
    }

    private AssemblyType getAssemblyType(String xml, String accession) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, accession);
    }

}
