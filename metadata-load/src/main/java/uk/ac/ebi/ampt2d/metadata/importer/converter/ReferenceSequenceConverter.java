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
package uk.ac.ebi.ampt2d.metadata.importer.converter;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AssemblyType;

import java.util.Arrays;

public class ReferenceSequenceConverter implements Converter<AssemblyType, ReferenceSequence> {

    private TaxonomyRepository taxonomyRepository;
    private ReferenceSequenceRepository referenceSequenceRepository;

    public ReferenceSequenceConverter(TaxonomyRepository taxonomyRepository,
                                      ReferenceSequenceRepository referenceSequenceRepository) {
        this.taxonomyRepository = taxonomyRepository;
        this.referenceSequenceRepository = referenceSequenceRepository;
    }

    @Override
    public ReferenceSequence convert(AssemblyType assemblyType) {
        Taxonomy taxonomy = findExistingorSaveandReturnTaxonomy(assemblyType);
        return convertSaveAndReturnNewReferenceSequence(assemblyType, taxonomy);
    }

    private Taxonomy findExistingorSaveandReturnTaxonomy(AssemblyType assemblyType) {
        long taxonomyId = assemblyType.getTAXON().getTAXONID();
        Taxonomy taxonomy = taxonomyRepository.findByTaxonomyId(taxonomyId);
        if (taxonomy == null) {
            taxonomy = taxonomyRepository.save(new Taxonomy(taxonomyId, assemblyType.getTAXON().getSCIENTIFICNAME()));
        }
        return taxonomy;
    }

    private ReferenceSequence convertSaveAndReturnNewReferenceSequence(AssemblyType assemblyType, Taxonomy taxonomy) {
        return referenceSequenceRepository.save(
                new ReferenceSequence(
                        assemblyType.getNAME(),
                        "NOT_SPECIFIED",  // ENA only specifies accession+version, not the patch
                        Arrays.asList(assemblyType.getAccession()),
                        ReferenceSequence.Type.ASSEMBLY,  // ENA data model only has ASSEMBLY type
                        taxonomy
                )
        );
    }

}
