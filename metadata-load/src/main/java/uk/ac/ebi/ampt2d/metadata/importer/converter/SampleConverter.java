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

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ena.sra.xml.QualifiedNameType;
import uk.ac.ebi.ena.sra.xml.SampleType;

public class SampleConverter implements Converter<SampleType, Sample> {

    @Override
    public Sample convert(SampleType sampleType) {
        return new Sample(
                new AccessionVersionId(sampleType.getAccession(), 1),
                sampleType.getAlias(),
                extractBioSampleId(sampleType)
        );
    }

    /**
     * Given SampleType, extract and return BioSample ID. If no BioSample cross-reference is present, return null.
     */
    private String extractBioSampleId(SampleType sampleType) {
        for (QualifiedNameType externalId : sampleType.getIDENTIFIERS().getEXTERNALIDArray()) {
            if (externalId.getNamespace().equals("BioSample")) {
                return externalId.getStringValue();
            }
        }
        return null;
    }

}
