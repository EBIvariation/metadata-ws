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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ena.sra.xml.AssemblyType;

import java.util.Arrays;

public class ReferenceSequenceConverter implements Converter<AssemblyType, ReferenceSequence> {

    @Override
    public ReferenceSequence convert(AssemblyType assemblyType) {
        String refName = assemblyType.getNAME();
        String patch = null;
        // Attempt to extract patch from refName (only for GRC human or mouse assembly names)
        if (refName != null && refName.matches("^GRC[hm]\\d+\\.p\\d+$")) {
            String[] refNameSplit = assemblyType.getNAME().split("\\.", 2);
            refName = refNameSplit[0];
            patch = refNameSplit[1];
        }
        return new ReferenceSequence(
                refName,
                patch,
                Arrays.asList(assemblyType.getAccession()),
                ReferenceSequence.Type.GENOME_ASSEMBLY
        );
    }

}
