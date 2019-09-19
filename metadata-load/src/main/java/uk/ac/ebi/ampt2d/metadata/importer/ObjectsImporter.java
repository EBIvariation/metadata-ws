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
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.ampt2d.metadata.importer.api.ReferenceSequenceXmlRetrieverThroughEntrezApi;
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
import uk.ac.ebi.ampt2d.metadata.persistence.events.AnalysisEventHandler;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
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

    // XML retrievers: first is used for all entities except reference sequence, and the second one specifically for
    // reference sequences.
    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    protected ReferenceSequenceXmlRetrieverThroughEntrezApi referenceSequenceXmlRetrieverThroughEntrezApi;

    // Entity repositories
    protected StudyRepository studyRepository;

    protected AnalysisRepository analysisRepository;

    protected ReferenceSequenceRepository referenceSequenceRepository;

    protected SampleRepository sampleRepository;

    protected TaxonomyRepository taxonomyRepository;

    // Entity XML parsers
    private SraXmlParser<StudyType> sraStudyXmlParser;

    private SraXmlParser<AnalysisType> sraAnalysisXmlParser;

    protected SraXmlParser<SampleType> sraSampleXmlParser;

    private EntrezAssemblyXmlParser entrezAssemblyXmlParser;

    // Entity converters
    private Converter<StudyType, Study> studyConverter;

    private Converter<AnalysisType, Analysis> analysisConverter;

    protected Converter<SampleType, Sample> sampleConverter;

    // Extractors
    private PublicationExtractorFromStudy publicationExtractorFromStudy;

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public ObjectsImporter(
            SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
            ReferenceSequenceXmlRetrieverThroughEntrezApi referenceSequenceXmlRetrieverThroughEntrezApi,

            SraXmlParser<StudyType> sraStudyXmlParser,
            SraXmlParser<AnalysisType> sraAnalysisXmlParser,
            EntrezAssemblyXmlParser entrezAssemblyXmlParser,
            SraXmlParser<SampleType> sraSampleXmlParser,

            Converter<StudyType, Study> studyConverter,
            Converter<AnalysisType, Analysis> analysisConverter,
            Converter<SampleType, Sample> sampleConverter,

            PublicationExtractorFromStudy publicationExtractorFromStudy,
            WebResourceExtractorFromStudy webResourceExtractorFromStudy,
            FileExtractorFromAnalysis fileExtractorFromAnalysis,

            StudyRepository studyRepository,
            AnalysisRepository analysisRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            SampleRepository sampleRepository,
            TaxonomyRepository taxonomyRepository) {
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;
        this.referenceSequenceXmlRetrieverThroughEntrezApi = referenceSequenceXmlRetrieverThroughEntrezApi;

        this.sraStudyXmlParser = sraStudyXmlParser;
        this.sraAnalysisXmlParser = sraAnalysisXmlParser;
        this.entrezAssemblyXmlParser = entrezAssemblyXmlParser;
        this.sraSampleXmlParser = sraSampleXmlParser;

        this.studyConverter = studyConverter;
        this.analysisConverter = analysisConverter;
        this.sampleConverter = sampleConverter;

        this.publicationExtractorFromStudy = publicationExtractorFromStudy;
        this.webResourceExtractorFromStudy = webResourceExtractorFromStudy;
        this.fileExtractorFromAnalysis = fileExtractorFromAnalysis;

        this.studyRepository = studyRepository;
        this.analysisRepository = analysisRepository;
        this.referenceSequenceRepository = referenceSequenceRepository;
        this.taxonomyRepository = taxonomyRepository;
        this.sampleRepository = sampleRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Study importStudy(String accession) throws Exception {
        String xml = sraXmlRetrieverByAccession.getXml(accession);
        StudyType studyType = sraStudyXmlParser.parseXml(xml, accession);
        Study study = studyConverter.convert(studyType);
        StudyType.STUDYLINKS studylinks = studyType.getSTUDYLINKS();
        study.setPublications(publicationExtractorFromStudy.getPublications(studylinks));
        study.setResources(webResourceExtractorFromStudy.getWebResources(studylinks));
        study = extractAnalysisFromStudy(studyType, study);
        return study;
    }

    protected abstract Study extractAnalysisFromStudy(StudyType studyType, Study study) throws Exception;

    @Transactional(rollbackFor = Exception.class)
    public Analysis importAnalysis(String accession) throws Exception {
        String xml = sraXmlRetrieverByAccession.getXml(accession);
        AnalysisType analysisType = sraAnalysisXmlParser.parseXml(xml, accession);
        Analysis analysis = analysisConverter.convert(analysisType);

        // Import everything related to reference sequences
        List<ReferenceSequence> referenceSequences = new ArrayList<>();
        ReferenceSequenceType referenceSequenceType = getReferenceSequenceType(analysisType);
        // Import an assembly, if present
        String assemblyAccession = getAssemblyAccession(referenceSequenceType);
        if (assemblyAccession != null) {
            referenceSequences.add(importReferenceSequence(assemblyAccession, "assembly"));
        }
        // Import all sequence and TSA accessions, if present
        for (String sequenceOrTsaAccession : getSequenceAndTsaAccessions(referenceSequenceType)) {
            referenceSequences.add(importReferenceSequence(sequenceOrTsaAccession, "nuccore"));
        }
        analysis.setReferenceSequences(referenceSequences);
        AnalysisEventHandler.validateReferenceSequenceLink(analysis);

        analysis.setFiles(fileExtractorFromAnalysis.getFiles(analysisType));
        List<Sample> samples = importSamples(analysisType);
        analysis.setSamples(samples);
        analysis = extractStudyFromAnalysis(analysisType, analysis);
        return analysis;
    }

    protected abstract Analysis extractStudyFromAnalysis(AnalysisType analysisType, Analysis analysis)
            throws Exception;

    /**
     * Based on an accession and type, imports a reference sequence.
     * @param accession: INSDC accession of a reference sequence.
     * @param referenceSequenceKind: Reference sequence kind; can be either "assembly" or "nuccore".
     * @return Ready ReferenceSequence entity
     */
    public ReferenceSequence importReferenceSequence(String accession, String referenceSequenceKind) throws Exception {
        ReferenceSequence referenceSequence = referenceSequenceRepository.findByAccession(accession);
        if (referenceSequence != null) {
            return referenceSequence;
        }
        try {
            Taxonomy taxonomy;
            // Import XML through Entrez API
            String assemblyXml = referenceSequenceXmlRetrieverThroughEntrezApi.getXml(accession, referenceSequenceKind);
            referenceSequence = entrezAssemblyXmlParser.parseXml(assemblyXml, accession, referenceSequenceKind);
            // Taxonomy of a reference sequence might already be saved in the database
            taxonomy = taxonomyRepository.findOrSave(referenceSequence.getTaxonomy());
            referenceSequence.setTaxonomy(taxonomy);
            referenceSequence = referenceSequenceRepository.findOrSave(referenceSequence);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for ReferenceSequence accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
            throw exception;
        }
        return referenceSequence;
    }

    private ReferenceSequenceType getReferenceSequenceType(AnalysisType analysis) {
        AnalysisType.ANALYSISTYPE analysisType = analysis.getANALYSISTYPE();
        // Analysis records can contain a reference sequence in either of the three analysis categories:
        // REFERENCE_ALIGNMENT, SEQUENCE_VARIATION, and PROCESSED_READS. It is guaranteed that each analysis contains
        // at most one of these three types.
        ReferenceSequenceType referenceSequenceType;
        if (analysisType.isSetREFERENCEALIGNMENT()) {
            referenceSequenceType = analysisType.getREFERENCEALIGNMENT();
        } else if (analysisType.isSetSEQUENCEVARIATION()) {
            referenceSequenceType = analysisType.getSEQUENCEVARIATION();
        } else if (analysisType.isSetPROCESSEDREADS()) {
            referenceSequenceType = analysisType.getPROCESSEDREADS();
        } else {
            return null;
        }
        return referenceSequenceType;
    }

    private String getAssemblyAccession(ReferenceSequenceType referenceSequenceType) {
        if (referenceSequenceType != null) {
            ReferenceAssemblyType referenceAssemblyType = referenceSequenceType.getASSEMBLY();
            if (referenceAssemblyType != null) {
                ReferenceAssemblyType.STANDARD standard = referenceAssemblyType.getSTANDARD();
                if (standard != null) {
                    return standard.getAccession();
                }
            }
        }
        return null;
    }

    private Set<String> getSequenceAndTsaAccessions(ReferenceSequenceType referenceSequenceType) {
        Set<String> sequenceAndTsaAccessions = new HashSet<>();
        if (referenceSequenceType != null) {
            ReferenceSequenceType.SEQUENCE[] sequences = referenceSequenceType.getSEQUENCEArray();
            if (sequences != null && sequences.length > 0) {
                for (ReferenceSequenceType.SEQUENCE sequence : sequences) {
                    sequenceAndTsaAccessions.add(sequence.getAccession());
                }
            }
        }
        return sequenceAndTsaAccessions;
    }

    public List<Sample> importSamples(AnalysisType analysisType) throws Exception {
        List<Sample> samples = new ArrayList<>();
        for (String sampleAccession : getSampleAccessions(analysisType)) {
            samples.add(importSample(sampleAccession));
        }
        samples = sampleRepository.findOrSave(samples);
        return samples;
    }

    public Sample importSample(String accession) throws Exception {
        Sample sample = null;
        try {
            String xml = sraXmlRetrieverByAccession.getXml(accession);
            SampleType sampleType = sraSampleXmlParser.parseXml(xml, accession);
            sample = sampleConverter.convert(sampleType);
            Taxonomy taxonomy = taxonomyRepository.findOrSave(extractTaxonomyFromSample(sampleType));
            sample.setTaxonomies(Arrays.asList(taxonomy));
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Sample accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
            throw exception;
        }
        return sample;
    }

    protected Taxonomy extractTaxonomyFromSample(SampleType sampleType) {
        SampleType.SAMPLENAME sampleName = sampleType.getSAMPLENAME();
        return new Taxonomy(sampleName.getTAXONID(), sampleName.getSCIENTIFICNAME(), "no rank");
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
