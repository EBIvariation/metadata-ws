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
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataReferenceSequenceFinderOrPersister;
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataSampleFinderOrPersister;
import uk.ac.ebi.ampt2d.metadata.importer.database.MetadataStudyFinderOrPersister;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractorFromReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractorFromSample;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.AssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceSequenceType;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    private SraXmlParser<StudyType> sraStudyXmlParser;
    private SraXmlParser<AnalysisType> sraAnalysisXmlParser;
    private SraXmlParser<AssemblyType> sraAssemblyXmlParser;
    private SraXmlParser<SampleType> sraSampleXmlParser;

    private Converter<StudyType, Study> studyConverter;
    private Converter<AnalysisType, Analysis> analysisConverter;
    private Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter;
    private Converter<SampleType, Sample> sampleConverter;

    private PublicationExtractorFromStudy publicationExtractorFromStudy;
    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;
    private FileExtractorFromAnalysis fileExtractorFromAnalysis;
    private TaxonomyExtractorFromReferenceSequence taxonomyExtractorFromReferenceSequence;
    private TaxonomyExtractorFromSample taxonomyExtractorFromSample;

    protected MetadataAnalysisPersister metadataAnalysisPersister;
    protected MetadataStudyFinderOrPersister metadataStudyFinderOrPersister;
    private MetadataReferenceSequenceFinderOrPersister metadataReferenceSequenceFinderOrPersister;
    private MetadataSampleFinderOrPersister metadataSampleFinderOrPersister;

    public ObjectsImporter(
            SraXmlRetrieverByAccession sraXmlRetrieverByAccession,

            SraXmlParser<StudyType> sraStudyXmlParser,
            SraXmlParser<AnalysisType> sraAnalysisXmlParser,
            SraXmlParser<AssemblyType> sraAssemblyXmlParser,
            SraXmlParser<SampleType> sraSampleXmlParser,

            Converter<StudyType, Study> studyConverter,
            Converter<AnalysisType, Analysis> analysisConverter,
            Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter,
            Converter<SampleType, Sample> sampleConverter,

            PublicationExtractorFromStudy publicationExtractorFromStudy,
            WebResourceExtractorFromStudy webResourceExtractorFromStudy,
            FileExtractorFromAnalysis fileExtractorFromAnalysis,
            TaxonomyExtractorFromReferenceSequence taxonomyExtractorFromReferenceSequence,
            TaxonomyExtractorFromSample taxonomyExtractorFromSample,

            MetadataStudyFinderOrPersister metadataStudyFinderOrPersister,
            MetadataAnalysisPersister metadataAnalysisPersister,
            MetadataReferenceSequenceFinderOrPersister metadataReferenceSequenceFinderOrPersister,
            MetadataSampleFinderOrPersister metadataSampleFinderOrPersister) {
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;

        this.sraStudyXmlParser = sraStudyXmlParser;
        this.sraAnalysisXmlParser = sraAnalysisXmlParser;
        this.sraAssemblyXmlParser = sraAssemblyXmlParser;
        this.sraSampleXmlParser = sraSampleXmlParser;

        this.studyConverter = studyConverter;
        this.analysisConverter = analysisConverter;
        this.referenceSequenceConverter = referenceSequenceConverter;
        this.sampleConverter = sampleConverter;

        this.publicationExtractorFromStudy = publicationExtractorFromStudy;
        this.webResourceExtractorFromStudy = webResourceExtractorFromStudy;
        this.fileExtractorFromAnalysis = fileExtractorFromAnalysis;
        this.taxonomyExtractorFromReferenceSequence = taxonomyExtractorFromReferenceSequence;
        this.taxonomyExtractorFromSample = taxonomyExtractorFromSample;

        this.metadataStudyFinderOrPersister = metadataStudyFinderOrPersister;
        this.metadataAnalysisPersister = metadataAnalysisPersister;
        this.metadataReferenceSequenceFinderOrPersister = metadataReferenceSequenceFinderOrPersister;
        this.metadataSampleFinderOrPersister = metadataSampleFinderOrPersister;
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
                referenceSequences.add(importReferenceSequence(referenceSequenceAccession));
            }
            analysis.setReferenceSequences(referenceSequences);
            List<Sample> samples = new ArrayList<>();
            for (String sampleAccession : getSampleAccessions(analysisType)) {
                samples.add(importSample(sampleAccession));
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
        ReferenceSequence referenceSequence = null;
        try {
            String assemblyXml = sraXmlRetrieverByAccession.getXml(accession);
            AssemblyType assembly = sraAssemblyXmlParser.parseXml(assemblyXml, accession);
            referenceSequence = referenceSequenceConverter.convert(assembly);
            Taxonomy taxonomy = taxonomyExtractorFromReferenceSequence.getTaxonomy(assembly.getTAXON());
            referenceSequence.setTaxonomy(taxonomy);
            referenceSequence = metadataReferenceSequenceFinderOrPersister.findOrPersistReferenceSequence(
                    referenceSequence);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for ReferenceSequence accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return referenceSequence;
    }

    private Set<String> getReferenceSequenceAccessions(AnalysisType analysisType) {
        Set<String> referenceSequenceAccessions = new HashSet<>();
        AnalysisType.ANALYSISTYPE analysisType1 = analysisType.getANALYSISTYPE();
        // ENA records can contain a reference sequence in either of the three analysis categories:
        // REFERENCE_ALIGNMENT, SEQUENCE_VARIATION, and PROCESSED_READS. All accessions are collected and returned.
        if (analysisType1.isSetREFERENCEALIGNMENT()) {
            referenceSequenceAccessions.add(
                    // TODO: should we use getRefname() or getAccession()?
                    getAccessionFromReferenceSequenceType(analysisType1.getREFERENCEALIGNMENT()));
        }
        else if (analysisType1.isSetSEQUENCEVARIATION()) {
            referenceSequenceAccessions.add(
                    getAccessionFromReferenceSequenceType(analysisType1.getSEQUENCEVARIATION()));
        }
        else if (analysisType1.isSetPROCESSEDREADS()) {
            referenceSequenceAccessions.add(
                    getAccessionFromReferenceSequenceType(analysisType1.getPROCESSEDREADS()));
        }
        referenceSequenceAccessions.remove(null);
        return referenceSequenceAccessions;
    }

    private String getAccessionFromReferenceSequenceType(ReferenceSequenceType referenceSequenceType){
        if (referenceSequenceType == null) {
            return null;
        }
        ReferenceAssemblyType referenceAssemblyType = referenceSequenceType.getASSEMBLY();
        if (referenceAssemblyType == null) {
            return null;
        }
        ReferenceAssemblyType.STANDARD standard = referenceAssemblyType.getSTANDARD();
        if (standard != null) {
            return standard.getAccession();
        }
        return null;
    }

    public Sample importSample(String accession) {
        Sample sample = null;
        try {
            String xml = sraXmlRetrieverByAccession.getXml(accession);
            SampleType sampleType = sraSampleXmlParser.parseXml(xml, accession);
            sample = sampleConverter.convert(sampleType);
            Taxonomy taxonomy = taxonomyExtractorFromSample.getTaxonomy(sampleType.getSAMPLENAME());
            sample.setTaxonomies(Arrays.asList(taxonomy));
            sample = metadataSampleFinderOrPersister.findOrPersistSample(sample);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Sample accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return sample;
    }

    private Set<String> getSampleAccessions(AnalysisType analysisType) {
        Set<String> sampleAccessions = new HashSet<>();
        AnalysisType.SAMPLEREF[] samplerefs = analysisType.getSAMPLEREFArray();
        for (AnalysisType.SAMPLEREF sampleref : samplerefs) {
            sampleAccessions.add(sampleref.getAccession());
        }
        return sampleAccessions;
    }

}
