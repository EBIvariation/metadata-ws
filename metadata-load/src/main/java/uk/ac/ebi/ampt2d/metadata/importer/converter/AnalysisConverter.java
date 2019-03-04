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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.AnalysisType.ANALYSISTYPE.SEQUENCEVARIATION;

public class AnalysisConverter implements Converter<AnalysisType, Analysis> {

    @Override
    public Analysis convert(AnalysisType analysisType) {
        return new Analysis(new AccessionVersionId(analysisType.getAccession(), 1), analysisType.getTITLE(),
                analysisType.getDESCRIPTION(), Analysis.Type.UNSPECIFIED,
                getTechnology(analysisType), getPlatform(analysisType));
    }

    private String getPlatform(AnalysisType analysisType) {
        SEQUENCEVARIATION sequencevariation = analysisType.getANALYSISTYPE().getSEQUENCEVARIATION();
        String platform = "UNSPECIFIED";
        if (sequencevariation != null && sequencevariation.getPLATFORM() != null) {
            platform = sequencevariation.getPLATFORM();
        }
        return platform;
    }

    private Analysis.Technology getTechnology(AnalysisType analysisType) {
        SEQUENCEVARIATION sequencevariation = analysisType.getANALYSISTYPE().getSEQUENCEVARIATION();
        if (sequencevariation != null && sequencevariation.getEXPERIMENTTYPEArray().length != 0) {
            switch (sequencevariation.getEXPERIMENTTYPEArray()[0].toString()) {
                case "Exome sequencing":
                    return Analysis.Technology.EXOME_SEQUENCING;
                case "Genotyping by array":
                    return Analysis.Technology.GENOTYPING;
                case "Curation":
                    return Analysis.Technology.CURATION;
                case "Genotyping by sequencing":
                    return Analysis.Technology.GENOTYPING;
            }
        }
        return Analysis.Technology.UNSPECIFIED;
    }
}
