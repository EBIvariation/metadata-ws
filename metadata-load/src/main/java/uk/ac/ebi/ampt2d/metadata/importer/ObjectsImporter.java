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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraXmlRetrieverThroughApi;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.WebResourceExtractorFromStudy;
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
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceSequenceType;
import uk.ac.ebi.ena.sra.xml.SampleType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

    public static final String ASSEMBLY_END_TAG = "</ASSEMBLY>";

    public static final String ENTRY_END_TAG = "</entry";

    // XML retrievers. First is used as default, second for cases where API retrieval mode must be forced
    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    protected SraXmlRetrieverThroughApi sraxmlRetrieverByAccessionForceApi;

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

    private SraXmlParser<SampleType> sraSampleXmlParser;

    // Entity converters
    private Converter<StudyType, Study> studyConverter;

    private Converter<AnalysisType, Analysis> analysisConverter;

    private Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter;

    private Converter<SampleType, Sample> sampleConverter;

    // Extractors
    private PublicationExtractorFromStudy publicationExtractorFromStudy;

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public ObjectsImporter(
            SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
            SraXmlRetrieverThroughApi sraxmlRetrieverByAccessionForceApi,

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

            StudyRepository studyRepository,
            AnalysisRepository analysisRepository,
            ReferenceSequenceRepository referenceSequenceRepository,
            SampleRepository sampleRepository,
            TaxonomyRepository taxonomyRepository) {
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;
        this.sraxmlRetrieverByAccessionForceApi = sraxmlRetrieverByAccessionForceApi;

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
            // Reference sequences must always be imported through API, even with import mode = DB
            String referenceSequenceXml = sraxmlRetrieverByAccessionForceApi.getXml(accession);
            if (referenceSequenceXml.contains(ASSEMBLY_END_TAG)) {
                AssemblyType assembly = sraAssemblyXmlParser.parseXml(referenceSequenceXml, accession);
                referenceSequence = referenceSequenceConverter.convert(assembly);
                Taxonomy taxonomy = taxonomyRepository.findOrSave(extractTaxonomyFromAssembly(assembly));
                referenceSequence.setTaxonomy(taxonomy);
            } else if (referenceSequenceXml.contains(ENTRY_END_TAG)) {
                referenceSequence = getReferenceSequenceFromEntryXml(referenceSequenceXml);
            }
            referenceSequence = referenceSequenceRepository.findOrSave(referenceSequence);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for ReferenceSequence accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return referenceSequence;
    }

    protected String getAccessionFromStandard(ReferenceAssemblyType.STANDARD standard) {
        return standard.getAccession();
    }

    private ReferenceSequence getReferenceSequenceFromEntryXml(String referenceSequenceXml) throws Exception {
        String tsa = "Transcriptome Shotgun Assembly";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(referenceSequenceXml)));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        String referenceSequenceAccession = (String) xpath.evaluate("/ROOT/entry/@accession", document,
                XPathConstants.STRING);
        String referenceSequenceName = (String) xpath.evaluate("/ROOT/entry/description", document,
                XPathConstants.STRING);
        ReferenceSequence.Type referenceSequenceType1 = ReferenceSequence.Type.GENE;
        String referenceSequenceType = (String) xpath.evaluate("/ROOT/entry/keyword", document,
                XPathConstants.STRING);
        if (referenceSequenceType != null && referenceSequenceType.contains(tsa)) {
            referenceSequenceType1 = ReferenceSequence.Type.TRANSCRIPTOME;
        }
        ReferenceSequence referenceSequence = new ReferenceSequence(referenceSequenceName, null, Arrays.asList
                (referenceSequenceAccession), referenceSequenceType1);
        String taxonomyName = (String) xpath.evaluate("/ROOT/entry/feature/taxon/@scientificName", document,
                XPathConstants.STRING);
        long taxonomyId = Long.parseLong((String) xpath.evaluate("/ROOT/entry/feature/taxon/@taxId", document,
                XPathConstants.STRING));
        Taxonomy taxonomy = taxonomyRepository.findOrSave(new Taxonomy(taxonomyId, taxonomyName));
        referenceSequence.setTaxonomy(taxonomy);
        return referenceSequence;
    }

    private Taxonomy extractTaxonomyFromAssembly(AssemblyType assemblyType) {
        AssemblyType.TAXON taxon = assemblyType.getTAXON();
        return new Taxonomy(taxon.getTAXONID(), taxon.getSCIENTIFICNAME());
    }

    private Set<String> getReferenceSequenceAccessions(AnalysisType analysisType) {
        Set<String> referenceSequenceAccessions = new HashSet<>();
        AnalysisType.ANALYSISTYPE analysisType1 = analysisType.getANALYSISTYPE();
        // Analysis records can contain a reference sequence in either of the three analysis categories:
        // REFERENCE_ALIGNMENT, SEQUENCE_VARIATION, and PROCESSED_READS. It is guaranteed that each analysis contains
        // at most one of these three types.
        if (analysisType1.isSetREFERENCEALIGNMENT()) {
            String accession = getAccessionFromReferenceSequenceType(analysisType1.getREFERENCEALIGNMENT());
            if (accession != null) {
                referenceSequenceAccessions.add(accession);
            }
        }
        if (analysisType1.isSetSEQUENCEVARIATION()) {
            AnalysisType.ANALYSISTYPE.SEQUENCEVARIATION sequencevariation = analysisType1.getSEQUENCEVARIATION();
            Set<String> accessions = getSequenceOrTsaAccessions(sequencevariation);
            referenceSequenceAccessions.addAll(accessions);
            String accession = getAccessionFromReferenceSequenceType(sequencevariation);
            if (accession != null) {
                referenceSequenceAccessions.add(accession);
            }
        }
        if (analysisType1.isSetPROCESSEDREADS()) {
            String accession = getAccessionFromReferenceSequenceType(analysisType1.getPROCESSEDREADS());
            if (accession != null) {
                referenceSequenceAccessions.add(accession);
            }
        }
        return referenceSequenceAccessions;
    }

    private Set<String> getSequenceOrTsaAccessions(AnalysisType.ANALYSISTYPE.SEQUENCEVARIATION sequencevariation) {
        Set<String> sequenceOrTsaAccessions = new HashSet<>();
        ReferenceSequenceType.SEQUENCE[] sequences = sequencevariation.getSEQUENCEArray();
        if (sequences == null || sequences.length == 0) {
            return sequenceOrTsaAccessions;
        }
        for (ReferenceSequenceType.SEQUENCE sequence : sequences) {
            sequenceOrTsaAccessions.add(sequence.getAccession());
        }
        return sequenceOrTsaAccessions;
    }

    private String getAccessionFromReferenceSequenceType(ReferenceSequenceType referenceSequenceType) {
        if (referenceSequenceType != null) {
            ReferenceAssemblyType referenceAssemblyType = referenceSequenceType.getASSEMBLY();
            if (referenceAssemblyType != null) {
                ReferenceAssemblyType.STANDARD standard = referenceAssemblyType.getSTANDARD();
                if (standard != null) {
                    return getAccessionFromStandard(standard);
                }
            }
        }
        return null;
    }

    public Sample importSample(String accession) {
        Sample sample = null;
        try {
            String xml = sraXmlRetrieverByAccession.getXml(accession);
            SampleType sampleType = sraSampleXmlParser.parseXml(xml, accession);
            sample = sampleConverter.convert(sampleType);
            Taxonomy taxonomy = taxonomyRepository.findOrSave(extractTaxonomyFromSample(sampleType));
            sample.setTaxonomies(Arrays.asList(taxonomy));
            sample = sampleRepository.findOrSave(sample);
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Sample accession " + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }
        return sample;
    }

    private Taxonomy extractTaxonomyFromSample(SampleType sampleType) {
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
