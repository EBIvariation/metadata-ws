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
package uk.ac.ebi.ampt2d.metadata.parser;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;
import uk.ac.ebi.ena.sra.xml.AnalysisSetType;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisFileTypeFromSetTest {
    @Test
    public void testAnalysisFileParser() throws Exception {
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ANALYSIS_SET>\n" +
                "  <ANALYSIS alias=\"uk10k_scoop5013826.vcf.gz-vcf_analysis-sc-20120330\" center_name=\"SC\" broker_name=\"EGA\" analysis_center=\"SC\" accession=\"ERZ000011\">\n" +
                "    <IDENTIFIERS>\n" +
                "      <PRIMARY_ID>ERZ000011</PRIMARY_ID>\n" +
                "      <SUBMITTER_ID namespace=\"SC\">uk10k_scoop5013826.vcf.gz-vcf_analysis-sc-20120330</SUBMITTER_ID>\n" +
                "    </IDENTIFIERS>\n" +
                "    <TITLE>Variant calling of UK10K_SCOOP5013826</TITLE>\n" +
                "    <DESCRIPTION>REL-2011-07-14_variant_calling_of_UK10K_OBESITY_SCOOP</DESCRIPTION>\n" +
                "    <STUDY_REF refname=\"UK10K_exome_sequence__SCOOP_samples-sc-2011-08-18T14:40:03Z-1706\" refcenter=\"SC\" accession=\"ERP000860\"/>\n" +
                "    <SAMPLE_REF refname=\"UK10K_SCOOP5013826-sc-2011-08-18T15:01:15Z-1027679\" refcenter=\"SC\" label=\"UK10K_SCOOP5013826\" accession=\"ERS049026\">\n" +
                "      <IDENTIFIERS>\n" +
                "        <PRIMARY_ID>ERS049026</PRIMARY_ID>\n" +
                "      </IDENTIFIERS>\n" +
                "    </SAMPLE_REF>\n" +
                "    <ANALYSIS_TYPE>\n" +
                "      <SEQUENCE_VARIATION>\n" +
                "        <ASSEMBLY>\n" +
                "          <STANDARD refname=\"GRCh37\"/>\n" +
                "        </ASSEMBLY>\n" +
                "      </SEQUENCE_VARIATION>\n" +
                "    </ANALYSIS_TYPE>\n" +
                "    <FILES>\n" +
                "      <FILE filename=\"UK10K_SCOOP5013826.vcf.gz\" filetype=\"vcf\" checksum_method=\"MD5\" checksum=\"980aad09354c5bc984e23d2f74efdf3b\" unencrypted_checksum=\"641127f130e67cf39a65a5e245c9ecb\"/>\n" +
                "      <FILE filename=\"ERZ000/ERZ000001/do131_Input_liver_none_mmuC57BL65_CRI01.sra.sorted.bam\" filetype=\"bam\" checksum_method=\"MD5\" checksum=\"15191d68bdd5c1ad23c943c3da3730c7\"/>" +
                "    </FILES>\n" +
                "  </ANALYSIS>\n" +
                "</ANALYSIS_SET>\n";

        AnalysisFileTypeFromSet analysisFileTypeFromSet = new AnalysisFileTypeFromSet();
        AnalysisSetType analysisSetType = analysisFileTypeFromSet.getAnalysisSet(xmlStr);
        List<AnalysisFileType> analysisFileList = analysisFileTypeFromSet.extract(analysisSetType);
        assertAnalysisFileList(analysisFileList);
    }

    private void assertAnalysisFileList(List<AnalysisFileType> analysisFileList) {
        assertEquals(analysisFileList.size(), 2);
        assertEquals(analysisFileList.get(0).getFilename(), "UK10K_SCOOP5013826.vcf.gz");
        assertEquals(analysisFileList.get(0).getFiletype(), AnalysisFileType.Filetype.Enum.forInt(AnalysisFileType.Filetype.INT_VCF));
        assertEquals(analysisFileList.get(0).getChecksum(), "980aad09354c5bc984e23d2f74efdf3b");
        assertEquals(analysisFileList.get(0).getChecksumMethod(), AnalysisFileType.ChecksumMethod.Enum.forString("MD5"));
        assertEquals(analysisFileList.get(1).getFilename(), "ERZ000/ERZ000001/do131_Input_liver_none_mmuC57BL65_CRI01.sra.sorted.bam");
        assertEquals(analysisFileList.get(1).getFiletype(), AnalysisFileType.Filetype.Enum.forInt(AnalysisFileType.Filetype.INT_BAM));
        assertEquals(analysisFileList.get(1).getChecksum(), "15191d68bdd5c1ad23c943c3da3730c7");
        assertEquals(analysisFileList.get(1).getChecksumMethod(), AnalysisFileType.ChecksumMethod.Enum.forString("MD5"));
    }

    @Test
    public void testAnalysisFileParserWrongInput() throws Exception {
        String xmlStr = "/<?wrong xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ANALYSIS_SET>\n" +
                "  <ANALYSIS alias=\"uk10k_scoop5013826.vcf.gz-vcf_analysis-sc-20120330\" center_name=\"SC\" broker_name=\"EGA\" analysis_center=\"SC\" accession=\"ERZ000011\">\n" +
                "  </ANALYSIS>\n" +
                "</ANALYSIS_SET>\n";

        AnalysisFileTypeFromSet analysisFileTypeFromSet = new AnalysisFileTypeFromSet();
        Throwable exception = assertThrows(XmlException.class, () -> analysisFileTypeFromSet.getAnalysisSet(xmlStr));
        assertEquals("error: Unexpected element: CDATA", exception.getMessage());
    }
}
