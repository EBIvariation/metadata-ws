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
import uk.ac.ebi.ampt2d.metadata.persistence.events.AnalysisEventHandler;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ObjectsImporter {

    public static final String ASSEMBLY_END_TAG = "</ASSEMBLY>";

    public static final String ENTRY_END_TAG = "</entry>";

    private static final Logger IMPORT_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

    // XML retrievers. First is used as default, second for cases where API retrieval mode must be forced
    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    protected SraXmlRetrieverThroughApi sraxmlRetrieverByAccessionForceApi;

    protected AssemblyXmlRetrieverThroughEntrezApi assemblyXmlRetrieverThroughEntrezApi;

    // Entity repositories
    protected StudyRepository studyRepository;

    protected AnalysisRepository analysisRepository;

    protected ReferenceSequenceRepository referenceSequenceRepository;

    protected SampleRepository sampleRepository;

    protected TaxonomyRepository taxonomyRepository;

    // Entity XML parsers
    private SraXmlParser<StudyType> sraStudyXmlParser;

    private SraXmlParser<AnalysisType> sraAnalysisXmlParser;

    private SraXmlParser<AssemblyType> sraAssemblyXmlParser;

    private SraXmlParser<ReferenceSequence> sraEntryXmlParser;

    private EntrezAssemblyXmlParser entrezAssemblyXmlParser;

    protected SraXmlParser<SampleType> sraSampleXmlParser;

    // Entity converters
    private Converter<StudyType, Study> studyConverter;

    private Converter<AnalysisType, Analysis> analysisConverter;

    private Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter;

    protected Converter<SampleType, Sample> sampleConverter;

    // Extractors
    private PublicationExtractorFromStudy publicationExtractorFromStudy;

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public ObjectsImporter(
            SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
            SraXmlRetrieverThroughApi sraxmlRetrieverByAccessionForceApi,
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
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;
        this.sraxmlRetrieverByAccessionForceApi = sraxmlRetrieverByAccessionForceApi;
        this.assemblyXmlRetrieverThroughEntrezApi = assemblyXmlRetrieverThroughEntrezApi;

        this.sraStudyXmlParser = sraStudyXmlParser;
        this.sraAnalysisXmlParser = sraAnalysisXmlParser;
        this.sraAssemblyXmlParser = sraAssemblyXmlParser;
        this.sraEntryXmlParser = sraEntryXmlParser;
        this.entrezAssemblyXmlParser = entrezAssemblyXmlParser;
        this.sraSampleXmlParser = sraSampleXmlParser;

        this.studyConverter = studyConverter;
        this.analysisConverter = analysisConverter;
        this.referenceSequenceConverter = referenceSequenceConverter;
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
            List<ReferenceSequence> referenceSequences = new ArrayList<>();
            for (String referenceSequenceAccession : getReferenceSequenceAccessions(analysisType)) {
                referenceSequences.add(importReferenceSequence(referenceSequenceAccession));
            }
            analysis.setReferenceSequences(referenceSequences);
            AnalysisEventHandler.validateReferenceSequenceLink(analysis);
            analysis.setFiles(fileExtractorFromAnalysis.getFiles(analysisType));
            List<Sample> samples = importSamples(analysisType);
            analysis.setSamples(samples);
            analysis = extractStudyFromAnalysis(analysisType, analysis);
        } catch (Exception exception) {
            analysis = null;
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Analysis accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }

        return analysis;
    }

    protected abstract Analysis extractStudyFromAnalysis(AnalysisType analysisType, Analysis analysis);

    public ReferenceSequence importReferenceSequence(String accession) {
        ReferenceSequence referenceSequence = null;
        Taxonomy taxonomy;
        try {
            // Reference sequences must always be imported through API, even with import mode = DB
            final String gcfAccessionPattern = "GCF_[\\d]*\\.?[\\d]*";
            if (accession.matches(gcfAccessionPattern)) {
                String assemblyXml = assemblyXmlRetrieverThroughEntrezApi.getXml(accession);
                referenceSequence = entrezAssemblyXmlParser.parseXml(assemblyXml, accession);
            } else {
                String referenceSequenceXml = sraxmlRetrieverByAccessionForceApi.getXml(accession);
                if (referenceSequenceXml.contains(ASSEMBLY_END_TAG)) {
                    AssemblyType assembly = sraAssemblyXmlParser.parseXml(referenceSequenceXml, accession);
                    referenceSequence = referenceSequenceConverter.convert(assembly);
                } else if (referenceSequenceXml.contains(ENTRY_END_TAG)) {
                    referenceSequence = sraEntryXmlParser.parseXml(referenceSequenceXml, accession);
                } else {
                    return referenceSequence;
                }
            }
            taxonomy = taxonomyRepository.findOrSave(referenceSequence.getTaxonomy());
            referenceSequence.setTaxonomy(taxonomy);
            referenceSequence = referenceSequenceRepository.findOrSave(referenceSequence);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for ReferenceSequence accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return referenceSequence;
    }

    private Set<String> getReferenceSequenceAccessions(AnalysisType analysis) {
        Set<String> referenceSequenceAccessions = new HashSet<>();
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
            return referenceSequenceAccessions;
        }
        referenceSequenceAccessions.addAll(getSequenceOrTsaAccessions(referenceSequenceType.getSEQUENCEArray()));
        String accession = getAssemblyAccession(referenceSequenceType);
        if (accession != null) {
            referenceSequenceAccessions.add(accession);
        }
        return referenceSequenceAccessions;
    }

    private Set<String> getSequenceOrTsaAccessions(ReferenceSequenceType.SEQUENCE[] sequences) {
        Set<String> sequenceOrTsaAccessions = new HashSet<>();
        if (sequences == null || sequences.length == 0) {
            return sequenceOrTsaAccessions;
        }
        for (ReferenceSequenceType.SEQUENCE sequence : sequences) {
            sequenceOrTsaAccessions.add(sequence.getAccession());
        }
        return sequenceOrTsaAccessions;
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

    public List<Sample> importSamples(AnalysisType analysisType) {
        Set<String> sampleSet = getSampleAccessions(analysisType);
        List<String> accessions = new ArrayList<>(sampleSet);
        List<Sample> samples = new ArrayList<>();
        try {
            Map<String, String> idXmlMap = sraXmlRetrieverByAccession.getXmls(accessions);
            SampleType sampleType;
            for (Map.Entry<String, String> entry : idXmlMap.entrySet()) {
                sampleType = sraSampleXmlParser.parseXml(entry.getValue(), entry.getKey());
                Sample sampleElement = sampleConverter.convert(sampleType);
                Taxonomy taxonomy = taxonomyRepository.findOrSave(extractTaxonomyFromSample(sampleType));
                sampleElement.setTaxonomies(Arrays.asList(taxonomy));
                samples.add(sampleElement);
            }
            samples = sampleRepository.findOrSave(samples);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Sample accession " + accessions);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return samples;
    }

    protected Taxonomy extractTaxonomyFromSample(SampleType sampleType) {
        SampleType.SAMPLENAME sampleName = sampleType.getSAMPLENAME();
        return new Taxonomy(sampleName.getTAXONID(), sampleName.getSCIENTIFICNAME());
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
