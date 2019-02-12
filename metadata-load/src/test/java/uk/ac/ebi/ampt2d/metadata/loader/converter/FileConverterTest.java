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
package uk.ac.ebi.ampt2d.metadata.loader.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileConverterTest {

    @Mock
    private AnalysisFileType analysisFileType;

    @Test
    public void testFileConverter() {
        initializeAnalysisFileType();
        File file = new FileConverter().convert(analysisFileType);
        assertAnalysisFileTypeEqualsFile(analysisFileType, file);
    }

    private void initializeAnalysisFileType() {
        when(analysisFileType.getFilename()).thenReturn("EGAF00001");
        when(analysisFileType.getFiletype()).thenReturn(AnalysisFileType.Filetype.VCF);
        when(analysisFileType.getChecksum()).thenReturn("checksum");
    }

    private void assertAnalysisFileTypeEqualsFile(AnalysisFileType analysisFileType, File file) {
        assertEquals(analysisFileType.getFilename(), file.getName());
        assertEquals(analysisFileType.getFiletype().toString().toUpperCase(), file.getType().toString());
        assertEquals(analysisFileType.getChecksum(), file.getHash());
    }

}
