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

package uk.ac.ebi.ampt2d.metadata.importer;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataAnalysisPersister;
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataStudyFinderOrPersister;
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
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceSequenceType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    protected MetadataAnalysisPersister metadataAnalysisPersister;

    protected MetadataStudyFinderOrPersister metadataStudyFinderOrPersister;

    private SraXmlParser<StudyType> sraStudyXmlParser;

    private SraXmlParser<AnalysisType> sraAnalysisXmlParser;

    private Converter<StudyType, Study> studyConverter;

    private Converter<AnalysisType, Analysis> analysisConverter;

    private PublicationExtractorFromStudy publicationExtractorFromStudy;

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;

    private TaxonomyExtractor taxonomyExtractor;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public ObjectsImporter(SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
                           SraXmlParser<StudyType> sraStudyXmlParser, SraXmlParser<AnalysisType> sraAnalysisXmlParser,
                           Converter<StudyType, Study> studyConverter,
                           Converter<AnalysisType, Analysis> analysisConverter,
                           PublicationExtractorFromStudy publicationExtractorFromStudy,
                           WebResourceExtractorFromStudy webResourceExtractorFromStudy,
                           TaxonomyExtractor taxonomyExtractor,
                           FileExtractorFromAnalysis fileExtractorFromAnalysis,
                           MetadataAnalysisPersister metadataAnalysisPersister,
                           MetadataStudyFinderOrPersister metadataStudyFinderOrPersister) {
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;
        this.sraStudyXmlParser = sraStudyXmlParser;
        this.sraAnalysisXmlParser = sraAnalysisXmlParser;
        this.studyConverter = studyConverter;
        this.analysisConverter = analysisConverter;
        this.publicationExtractorFromStudy = publicationExtractorFromStudy;
        this.webResourceExtractorFromStudy = webResourceExtractorFromStudy;
        this.taxonomyExtractor = taxonomyExtractor;
        this.fileExtractorFromAnalysis = fileExtractorFromAnalysis;
        this.metadataAnalysisPersister = metadataAnalysisPersister;
        this.metadataStudyFinderOrPersister = metadataStudyFinderOrPersister;
    }

    public Study importStudy(String accession) {
        Study study = null;
        try {
            String xml = sraXmlRetrieverByAccession.getXml(accession);
            StudyType studyType = sraStudyXmlParser.parseXml(xml, accession);
            study = studyConverter.convert(studyType);
            StudyType.STUDYLINKS studylinks = studyType.getSTUDYLINKS();
            study.setPublications(publicationExtractorFromStudy.getPublications(studylinks));
            study.setResources(webResourceExtractorFromStudy.getWebResources(studylinks));
            study.setTaxonomy(taxonomyExtractor.getTaxonomy());
            study = extractAnalysisFromStudy(studyType, study);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return study;
    }

    protected abstract Study extractAnalysisFromStudy(StudyType studyType, Study study);

    public Analysis importAnalysis(String accession) {
        Analysis analysis = null;
        try {
            String xml = sraXmlRetrieverByAccession.getXml(accession);
            AnalysisType analysisType = sraAnalysisXmlParser.parseXml(xml, accession);
            analysis = analysisConverter.convert(analysisType);
            analysis.setFiles(fileExtractorFromAnalysis.getFiles(analysisType));
            List<ReferenceSequence> referenceSequences = new ArrayList<>();
            for (String referenceSequenceAccession : getReferenceSequenceAccessions(analysisType)) {
                //TODO ReferenceSequence Import
                //referenceSequences.add(importReferenceSequence(referenceSequenceAccession));
            }
            analysis.setReferenceSequences(referenceSequences);
            List<Sample> samples = new ArrayList<>();
            for (String sampleAccession : getSampleAccessions(analysisType)) {
                //TODO Sample Import
                //samples.add(importSample(sampleAccession));
            }
            analysis.setSamples(samples);
            analysis = extractStudyFromAnalysis(analysisType, analysis);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Analysis accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }

        return analysis;
    }

    protected abstract Analysis extractStudyFromAnalysis(AnalysisType analysisType, Analysis analysis);

    public ReferenceSequence importReferenceSequence(String accession) {
        return null;
    }

    public Sample importSample(String accession) {
        return null;
    }

    private Set<String> getSampleAccessions(AnalysisType analysisType) {
        Set<String> sampleAccessions = new HashSet<>();
        AnalysisType.SAMPLEREF[] samplerefs = analysisType.getSAMPLEREFArray();
        for (AnalysisType.SAMPLEREF sampleref : samplerefs) {
            sampleAccessions.add(sampleref.getAccession());
        }
        return sampleAccessions;
    }

    private Set<String> getReferenceSequenceAccessions(AnalysisType analysisType) {
        Set<String> referenceSequenceAccessions = new HashSet<>();
        ReferenceSequenceType referenceSequenceType = analysisType.getANALYSISTYPE().getREFERENCEALIGNMENT();
        if (referenceSequenceType == null) {
            return referenceSequenceAccessions;
        }
        ReferenceAssemblyType referenceAssemblyType = referenceSequenceType.getASSEMBLY();
        if (referenceAssemblyType == null) {
            return referenceSequenceAccessions;
        }
        ReferenceAssemblyType.STANDARD standard = referenceAssemblyType.getSTANDARD();
        if (standard != null) {
            referenceSequenceAccessions.add(standard.getAccession());
        }
        return referenceSequenceAccessions;
    }
}
