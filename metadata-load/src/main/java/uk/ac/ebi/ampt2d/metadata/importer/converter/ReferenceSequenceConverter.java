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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ena.sra.xml.AssemblyType;

import java.util.Arrays;

public class ReferenceSequenceConverter implements Converter<AssemblyType, ReferenceSequence> {

    @Override
    public ReferenceSequence convert(AssemblyType assemblyType) {
        StringBuilder refName = new StringBuilder(assemblyType.getNAME());
        String patch = getPatch(refName);
        ReferenceSequence referenceSequence = new ReferenceSequence(
                refName.toString(),
                patch,
                Arrays.asList(assemblyType.getAccession()),
                ReferenceSequence.Type.GENOME_ASSEMBLY
        );
        referenceSequence.setTaxonomy(extractTaxonomyFromAssembly(assemblyType));
        return referenceSequence;
    }

    public static String getPatch(StringBuilder refName) {
        String refNameStr = refName.toString();
        String patch = null;
        // Attempt to extract patch from refName (only for GRC human or mouse assembly names)
        if (refNameStr != null && refNameStr.matches("^GRC[hm]\\d+\\.p\\d+$")) {
            String[] refNameSplit = refNameStr.split("\\.", 2);
            refName.replace(refName.indexOf("."), refName.length(), "");
            patch = refNameSplit[1];
        }
        return patch;
    }

    private Taxonomy extractTaxonomyFromAssembly(AssemblyType assemblyType) {
        AssemblyType.TAXON taxon = assemblyType.getTAXON();
        return new Taxonomy(taxon.getTAXONID(), taxon.getSCIENTIFICNAME());
    }
}
