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
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractorFromReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.AssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This importer is mainly used for EGA studies where Study XML doesn't have analysis accessions
 */
public class SraObjectsImporterThroughDatabase extends ObjectsImporter {

    private static final Map<String, String> REFERENCE_MAP;
    static {
        Map<String, String> refMap = new HashMap<>();
        refMap.put("grch37", "GCA_000001405.1");
        refMap.put("grch37.p1", "GCA_000001405.2");
        refMap.put("grch37.p2", "GCA_000001405.3");
        refMap.put("grch37.p3", "GCA_000001405.4");
        refMap.put("grch37.p4", "GCA_000001405.5");
        refMap.put("grch37.p5", "GCA_000001405.6");
        refMap.put("grch37.p6", "GCA_000001405.7");
        refMap.put("grch37.p7", "GCA_000001405.8");
        refMap.put("grch37.p8", "GCA_000001405.9");
        refMap.put("grch37.p9", "GCA_000001405.10");
        refMap.put("grch37.p10", "GCA_000001405.11");
        refMap.put("grch37.p11", "GCA_000001405.12");
        refMap.put("grch37.p12", "GCA_000001405.13");
        refMap.put("grch37.p13", "GCA_000001405.14");
        refMap.put("grch38", "GCA_000001405.15");
        refMap.put("grch38.p1", "GCA_000001405.16");
        refMap.put("grch38.p2", "GCA_000001405.17");
        refMap.put("grch38.p3", "GCA_000001405.18");
        refMap.put("grch38.p4", "GCA_000001405.19");
        refMap.put("grch38.p5", "GCA_000001405.20");
        refMap.put("grch38.p6", "GCA_000001405.21");
        refMap.put("grch38.p7", "GCA_000001405.22");
        refMap.put("grch38.p8", "GCA_000001405.23");
        refMap.put("grch38.p9", "GCA_000001405.24");
        refMap.put("grch38.p10", "GCA_000001405.25");
        refMap.put("grch38.p11", "GCA_000001405.26");
        refMap.put("grch38.p12", "GCA_000001405.27");
        refMap.put("grch38.p13", "GCA_000001405.28");
        REFERENCE_MAP = Collections.unmodifiableMap(refMap);
    }

    private Map<String, Study> accessionsToStudy = new HashMap<>();

    public SraObjectsImporterThroughDatabase(
            SraXmlRetrieverThroughDatabase sraXmlRetrieverThroughDatabase,
            SraXmlParser<StudyType> sraStudyXmlParser,
            Converter<StudyType, Study> studyConverter,
            PublicationExtractorFromStudy publicationExtractorFromStudy,
            WebResourceExtractorFromStudy webResourceExtractorFromStudy,
            TaxonomyExtractor taxonomyExtractor,
            TaxonomyExtractorFromReferenceSequence taxonomyExtractorFromReferenceSequence,
            SraXmlParser<AnalysisType> sraAnalysisXmlParser,
            Converter<AnalysisType, Analysis> analysisConverter,
            FileExtractorFromAnalysis fileExtractorFromAnalysis,
            SraXmlParser<AssemblyType> sraAssemblyXmlParser,
            Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter,
            AnalysisRepository analysisRepository,
            StudyRepository studyRepository,
            ReferenceSequenceRepository referenceSequenceRepository) {
        super(
                sraXmlRetrieverThroughDatabase,
                sraStudyXmlParser,
                sraAnalysisXmlParser,
                sraAssemblyXmlParser,
                studyConverter,
                analysisConverter,
                referenceSequenceConverter,
                publicationExtractorFromStudy,
                webResourceExtractorFromStudy,
                taxonomyExtractor,
                taxonomyExtractorFromReferenceSequence,
                fileExtractorFromAnalysis,
                analysisRepository,
                studyRepository,
                referenceSequenceRepository
        );
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
        return super.importAnalysis(accession);
    }

    @Override
    public ReferenceSequence importReferenceSequence(String accession) {
        return super.importReferenceSequence(accession);
    }

    @Override
    public Sample importSample(String accession) {
        return super.importSample(accession);
    }

    @Override
    protected Analysis extractStudyFromAnalysis(AnalysisType analysisType, Analysis analysis) {
        Study study = importStudyFromAnalysis(analysisType.getSTUDYREF().getAccession());
        analysis.setStudy(study);
        return analysisRepository.save(analysis);
    }

    @Override
    protected Study extractAnalysisFromStudy(StudyType studyType, Study study) {
        return study;
    }

    @Override
    protected String getAccessionFromStandard(ReferenceAssemblyType.STANDARD standard) {
        //TBD: To be removed
        String refName = standard.getRefname();
        String accession = null;
        if (refName != null) {
            accession = REFERENCE_MAP.get(refName.toLowerCase());
            if (accession == null) {
                throw new IllegalArgumentException ("Encountered exception for unknown reference sequence name " + refName);
            }
        }
        return accession;
    }

    private synchronized Study importStudyFromAnalysis(String studyAccession) {
        /* The below get will make sure to return shared study when analyses sharing same study are imported
          in current run */
        Study sharedStudy = accessionsToStudy.get(studyAccession);
        if (sharedStudy != null) {
            return sharedStudy;
        }
        Study study = importStudy(studyAccession);

        /* The below find query will make sure to return shared study when analyses sharing same study are imported
          in different runs */
        study = studyRepository.findOrSave(study);
        accessionsToStudy.put(studyAccession, study);
        return study;
    }

    private void setEnaObjectQuery(String query) {
        ((SraXmlRetrieverThroughDatabase) sraXmlRetrieverByAccession).setEnaObjectQuery(query);
    }

    public Map<String, Study> getAccessionsToStudy() {
        return accessionsToStudy;
    }

}
