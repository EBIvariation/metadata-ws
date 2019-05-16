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

package uk.ac.ebi.ampt2d.metadata.importer.database;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraObjectsImporterThroughAPI;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.HashMap;
import java.util.Map;

/**
 * This importer is mainly used for EGA studies where Study XML doesn't have analysis accessions
 */
public class SraObjectsImporterThroughDatabase extends SraObjectsImporterThroughAPI {

    private static Map<String, Study> accessionsToStudy = new HashMap<>();

    public SraObjectsImporterThroughDatabase(SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
                                             SraXmlParser<StudyType> sraStudyXmlParser,
                                             Converter<StudyType, Study> studyConverter,
                                             PublicationExtractorFromStudy publicationExtractorFromStudy,
                                             WebResourceExtractorFromStudy webResourceExtractorFromStudy,
                                             TaxonomyExtractor taxonomyExtractor,
                                             SraXmlParser<AnalysisType> sraAnalysisXmlParser,
                                             Converter<AnalysisType, Analysis> analysisConverter,
                                             FileExtractorFromAnalysis fileExtractorFromAnalysis) {
        super(sraXmlRetrieverByAccession, sraStudyXmlParser, studyConverter, publicationExtractorFromStudy,
                webResourceExtractorFromStudy, taxonomyExtractor, sraAnalysisXmlParser, analysisConverter,
                fileExtractorFromAnalysis);
    }

    @Override
    public Study importStudy(String accession) {
        setEnaObjectQuery(EnaObjectQuery.STUDY_QUERY);
        Study study = super.importStudy(accession);
        setEnaObjectQuery(EnaObjectQuery.ANALYSIS_QUERY);
        return study;
    }

    @Override
    public Analysis importAnalysis(String accession) {
        setEnaObjectQuery(EnaObjectQuery.ANALYSIS_QUERY);
        Analysis analysis = super.importAnalysis(accession);
        return analysis;
    }

    @Override
    public Study importStudyFromAnalysis(String studyAccession) {
        Study sharedStudy = accessionsToStudy.get(studyAccession);
        if (sharedStudy != null) {
            return sharedStudy;
        }
        Study study = importStudy(studyAccession);
        accessionsToStudy.put(studyAccession, study);
        return study;
    }

    @Override
    public ReferenceSequence importReferenceSequence(String accession) {
        return super.importReferenceSequence(accession);
    }

    @Override
    public Sample importSample(String accession) {
        return super.importSample(accession);
    }

    public void setEnaObjectQuery(String query) {
        ((SraXmlRetrieverThroughDatabase) sraXmlRetrieverByAccession).setEnaObjectQuery(query);
    }
}
