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

package uk.ac.ebi.ampt2d.metadata.importer.extractor;

import com.querydsl.core.types.dsl.BooleanExpression;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.AnalysisType.ANALYSISTYPE.SEQUENCEVARIATION;
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceSequenceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReferenceSequenceExtractorFromAnalysis {

    private static QReferenceSequence qReferenceSequence = QReferenceSequence.referenceSequence;

    private ReferenceSequenceRepository referenceSequenceRepository;

    public ReferenceSequenceExtractorFromAnalysis(ReferenceSequenceRepository referenceSequenceRepository) {
        this.referenceSequenceRepository = referenceSequenceRepository;
    }

    public List<ReferenceSequence> getReferenceType(AnalysisType analysisType) {
        ReferenceSequenceType referenceAlignment = analysisType.getANALYSISTYPE().getREFERENCEALIGNMENT();
        SEQUENCEVARIATION sequencevariation = analysisType.getANALYSISTYPE().getSEQUENCEVARIATION();
        List<ReferenceSequence> referenceSequences = new ArrayList<>();
        ReferenceAssemblyType referenceAssemblyType = null;
        if (referenceAlignment != null) {
            referenceAssemblyType = referenceAlignment.getASSEMBLY();
        } else if (sequencevariation != null) {
            referenceAssemblyType = sequencevariation.getASSEMBLY();
        }

        if (referenceAssemblyType != null && referenceAssemblyType.isSetSTANDARD()) {
            referenceSequences = getReferenceSequences(referenceAssemblyType);
        }

        return referenceSequences;
    }

    private List<ReferenceSequence> getReferenceSequences(ReferenceAssemblyType referenceAssemblyType) {
        ReferenceAssemblyType.STANDARD standard = referenceAssemblyType.getSTANDARD();
        String referenceSequenceName = standard.getRefname();
        String patch = "UNSPECIFIED";
        String accession = standard.getAccession();
        List<String> accessions = Arrays.asList(accession);

        //Other than EGA Assembly Xml others doesn't have name on the analysis xml(need to fetch assembly xml)
        if (referenceSequenceName == null) {
            referenceSequenceName = accession;
        }

        BooleanExpression predicate = qReferenceSequence.name.equalsIgnoreCase(referenceSequenceName).and
                (qReferenceSequence.patch.equalsIgnoreCase(patch));

        ReferenceSequence referenceSequence = referenceSequenceRepository.findOne(predicate);

        if (referenceSequence != null) {
            return Arrays.asList(referenceSequence);
        }
        return Arrays.asList(referenceSequenceRepository.save(new ReferenceSequence(referenceSequenceName, patch,
                accessions, ReferenceSequence.Type.ASSEMBLY)));
    }
}
