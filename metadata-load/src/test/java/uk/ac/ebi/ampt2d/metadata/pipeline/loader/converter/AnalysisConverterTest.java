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

package uk.ac.ebi.ampt2d.metadata.pipeline.loader.converter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.pipeline.configuration.AnalysisPersistenceApplicationRunnerConfiguration;
import uk.ac.ebi.ampt2d.metadata.pipeline.configuration.MetadataDatabaseConfiguration;
import uk.ac.ebi.ampt2d.metadata.pipeline.configuration.SraApiConfiguration;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.xml.SraXmlParser;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence.Type.ASSEMBLY;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AnalysisPersistenceApplicationRunnerConfiguration.class,
        MetadataDatabaseConfiguration.class,
        SraApiConfiguration.class})
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application.properties", properties = {"import.object=analysis",
        "import.source=API"})
public class AnalysisConverterTest {

    private static final String ANALYSIS_ACCESSION = "ERZ496533";

    private static final String ANALYSIS_DOCUMENT_API_XML = "AnalysisDocumentApi.xml";

    private static final String ANALYSIS_DOCUMENT_DATABASE_XML = "AnalysisDocumentDatabase1.xml";

    private SraXmlParser<AnalysisType> xmlParser;

    @Autowired
    private AnalysisConverter analysisConverter;

    @Before
    public void setUp() {
        xmlParser = new SraAnalysisXmlParser();
    }

    @Test
    public void convertFromApiXml() throws Exception {
        AnalysisType analysisType = getAnalysisType(ANALYSIS_DOCUMENT_API_XML);
        Analysis analysis = analysisConverter.convert(analysisType);

        Assert.assertNotNull(analysis);
        Assert.assertEquals("ERZ496533", analysis.getAccessionVersionId().getAccession());
        Assert.assertEquals(Analysis.Technology.EXOME_SEQUENCING, analysis.getTechnology());
        Assert.assertEquals(1, analysis.getReferenceSequences().size());
        Assert.assertEquals(ASSEMBLY, analysis.getReferenceSequences().get(0).getType());
        Assert.assertEquals("ERP000326", analysis.getStudy().getAccessionVersionId().getAccession());
        Assert.assertEquals(2, analysis.getFiles().size());

    }

    @Test
    public void convertFromDbXml() throws Exception {
        AnalysisType analysisType = getAnalysisType(ANALYSIS_DOCUMENT_DATABASE_XML);
        Analysis analysis = analysisConverter.convert(analysisType);

        Assert.assertNotNull(analysis);
        Assert.assertEquals("ERZ000011", analysis.getAccessionVersionId().getAccession());
        Assert.assertEquals(Analysis.Technology.GWAS, analysis.getTechnology());
        Assert.assertEquals(1, analysis.getReferenceSequences().size());
        Assert.assertEquals(ASSEMBLY, analysis.getReferenceSequences().get(0).getType());
        Assert.assertEquals("ERP000326", analysis.getStudy().getAccessionVersionId().getAccession());
        Assert.assertEquals(1, analysis.getFiles().size());

    }

    private AnalysisType getAnalysisType(String xml) throws Exception {
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(xml).toURI())));
        return xmlParser.parseXml(xmlString, ANALYSIS_ACCESSION);
    }

}