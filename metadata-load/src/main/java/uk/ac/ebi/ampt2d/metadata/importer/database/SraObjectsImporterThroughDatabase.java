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
import uk.ac.ebi.ampt2d.metadata.importer.api.AssemblyXmlRetrieverThroughEntrezApi;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraXmlRetrieverThroughApi;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.EntrezAssemblyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.AssemblyType;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This importer is mainly used for EGA studies where Study XML doesn't have analysis accessions
 */
public class SraObjectsImporterThroughDatabase extends ObjectsImporter {

    private Map<String, Study> accessionsToStudy = new HashMap<>();

    private static final Logger IMPORT_LOGGER = Logger.getLogger(SraObjectsImporterThroughDatabase.class.getName());

    public SraObjectsImporterThroughDatabase(
            SraXmlRetrieverThroughDatabase sraXmlRetrieverThroughDatabase,
            SraXmlRetrieverThroughApi sraXmlRetrieverThroughApi,
            AssemblyXmlRetrieverThroughEntrezApi assemblyXmlRetrieverThroughEntrezApi,

            SraXmlParser<StudyType> sraStudyXmlParser,
            SraXmlParser<AnalysisType> sraAnalysisXmlParser,
            SraXmlParser<AssemblyType> sraAssemblyXmlParser,
            SraXmlParser<ReferenceSequence> sraEntryXmlParser,
            EntrezAssemblyXmlParser entrezAssemblyXmlParser,
            SraXmlParser<SampleType> sraSampleXmlParser,

            Converter<StudyType, Study> studyConverter,
            Converter<AnalysisType, Analysis> analysisConverter,
            Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter,
            Converter<SampleType, Sample> sampleConverter,

            PublicationExtractorFromStudy publicationExtractorFromStudy,
            WebResourceExtractorFromStudy webResourceExtractorFromStudy,
            FileExtractorFromAnalysis fileExtractorFromAnalysis,

            StudyRepository studyRepository,
            AnalysisRepository analysisRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            SampleRepository sampleRepository,
            TaxonomyRepository taxonomyRepository) {
        super(
                sraXmlRetrieverThroughDatabase,
                sraXmlRetrieverThroughApi,
                assemblyXmlRetrieverThroughEntrezApi,


                sraStudyXmlParser,
                sraAnalysisXmlParser,
                sraAssemblyXmlParser,
                sraEntryXmlParser,
                entrezAssemblyXmlParser,
                sraSampleXmlParser,

                studyConverter,
                analysisConverter,
                referenceSequenceConverter,
                sampleConverter,

                publicationExtractorFromStudy,
                webResourceExtractorFromStudy,
                fileExtractorFromAnalysis,

                studyRepository,
                analysisRepository,
                referenceSequenceRepository,
                sampleRepository,
                taxonomyRepository
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
    public List<Sample> importSamples(AnalysisType analysisType) {
        setEnaObjectQuery(EnaObjectQuery.SAMPLE_QUERY);
        String analysisAccession = analysisType.getAccession();
        List<Sample> sampleList = new ArrayList<>();
        try {
            Map<String, String> idXmlMap = sraXmlRetrieverByAccession.getXmls(Arrays.asList(analysisAccession));
            SampleType sampleType;
            for (Map.Entry<String, String> entry : idXmlMap.entrySet()) {
                sampleType = sraSampleXmlParser.parseXml(entry.getValue(), entry.getKey());
                Sample sampleElement = sampleConverter.convert(sampleType);
                Taxonomy taxonomy = taxonomyRepository.findOrSave(extractTaxonomyFromSample(sampleType));
                sampleElement.setTaxonomies(Arrays.asList(taxonomy));
                sampleList.add(sampleElement);
            }
            sampleList = sampleRepository.findOrSaveList(sampleList);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Analysis accession " + analysisAccession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }

        setEnaObjectQuery(EnaObjectQuery.ANALYSIS_QUERY);
        return sampleList;
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
