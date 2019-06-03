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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This importer is mainly used for EGA studies where Study XML doesn't have analysis accessions
 */
public class SraObjectsImporterThroughDatabase extends ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(SraObjectsImporterThroughDatabase.class.getName());

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
        String refName = standard.getRefname();
        String accession = null;

        try {
            switch (refName) {
                case "GRCh37":
                    accession = "GCA_000001405.1";
                    break;

                case "GRCh37.p1":
                    accession = "GCA_000001405.2";
                    break;

                case "GRCh37.p2":
                    accession = "GCA_000001405.3";
                    break;

                case "GRCh37.p3":
                    accession = "GCA_000001405.4";
                    break;

                case "GRCh37.p4":
                    accession = "GCA_000001405.5";
                    break;

                case "GRCh37.p5":
                    accession = "GCA_000001405.6";
                    break;

                case "GRCh37.p6":
                    accession = "GCA_000001405.7";
                    break;

                case "GRCh37.p7":
                    accession = "GCA_000001405.8";
                    break;

                case "GRCh37.p8":
                    accession = "GCA_000001405.9";
                    break;

                case "GRCh37.p9":
                    accession = "GCA_000001405.10";
                    break;

                case "GRCh37.p10":
                    accession = "GCA_000001405.11";
                    break;

                case "GRCh37.p11":
                    accession = "GCA_000001405.12";
                    break;

                case "GRCh37.p12":
                    accession = "GCA_000001405.13";
                    break;

                case "GRCh37.p13":
                    accession = "GCA_000001405.14";
                    break;

                case "GRCh38":
                    accession = "GCA_000001405.15";
                    break;

                case "GRCh38.p1":
                    accession = "GCA_000001405.16";
                    break;

                case "GRCh38.p2":
                    accession = "GCA_000001405.17";
                    break;

                case "GRCh38.p3":
                    accession = "GCA_000001405.18";
                    break;

                case "GRCh38.p4":
                    accession = "GCA_000001405.19";
                    break;

                case "GRCh38.p5":
                    accession = "GCA_000001405.20";
                    break;

                case "GRCh38.p6":
                    accession = "GCA_000001405.21";
                    break;

                case "GRCh38.p7":
                    accession = "GCA_000001405.22";
                    break;

                case "GRCh38.p8":
                    accession = "GCA_000001405.23";
                    break;

                case "GRCh38.p9":
                    accession = "GCA_000001405.24";
                    break;

                case "GRCh38.p10":
                    accession = "GCA_000001405.25";
                    break;

                case "GRCh38.p11":
                    accession = "GCA_000001405.26";
                    break;

                case "GRCh38.p12":
                    accession = "GCA_000001405.27";
                    break;

                case "GRCh38.p13":
                    accession = "GCA_000001405.28";
                    break;

                default:
                    throw new Exception("Encountered exception for unknown reference sequence name " + refName);
            }
        } catch (Exception e) {
            IMPORT_LOGGER.log(Level.SEVERE, e.getMessage());
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
