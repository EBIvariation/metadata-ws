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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ena.sra.xml.AnalysisType;
import uk.ac.ebi.ena.sra.xml.AssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceSequenceType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

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

    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    protected StudyRepository studyRepository;

    protected AnalysisRepository analysisRepository;

    protected ReferenceSequenceRepository referenceSequenceRepository;

    private SraXmlParser<StudyType> sraStudyXmlParser;

    private SraXmlParser<AnalysisType> sraAnalysisXmlParser;

    private SraXmlParser<AssemblyType> sraAssemblyXmlParser;

    private Converter<StudyType, Study> studyConverter;

    private Converter<AnalysisType, Analysis> analysisConverter;

    private Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter;

    private PublicationExtractorFromStudy publicationExtractorFromStudy;

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;

    private TaxonomyExtractor taxonomyExtractor;

    private TaxonomyExtractorFromReferenceSequence taxonomyExtractorFromReferenceSequence;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public ObjectsImporter(SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
                           SraXmlParser<StudyType> sraStudyXmlParser,
                           SraXmlParser<AnalysisType> sraAnalysisXmlParser,
                           SraXmlParser<AssemblyType> sraAssemblyXmlParser,
                           Converter<StudyType, Study> studyConverter,
                           Converter<AnalysisType, Analysis> analysisConverter,
                           Converter<AssemblyType, ReferenceSequence> referenceSequenceConverter,
                           PublicationExtractorFromStudy publicationExtractorFromStudy,
                           WebResourceExtractorFromStudy webResourceExtractorFromStudy,
                           TaxonomyExtractor taxonomyExtractor,
                           TaxonomyExtractorFromReferenceSequence taxonomyExtractorFromReferenceSequence,
                           FileExtractorFromAnalysis fileExtractorFromAnalysis,
                           AnalysisRepository analysisRepository,
                           StudyRepository studyRepository,
                           ReferenceSequenceRepository referenceSequenceRepository) {
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;
        this.sraStudyXmlParser = sraStudyXmlParser;
        this.sraAnalysisXmlParser = sraAnalysisXmlParser;
        this.sraAssemblyXmlParser = sraAssemblyXmlParser;
        this.studyConverter = studyConverter;
        this.analysisConverter = analysisConverter;
        this.referenceSequenceConverter = referenceSequenceConverter;
        this.publicationExtractorFromStudy = publicationExtractorFromStudy;
        this.webResourceExtractorFromStudy = webResourceExtractorFromStudy;
        this.taxonomyExtractor = taxonomyExtractor;
        this.taxonomyExtractorFromReferenceSequence = taxonomyExtractorFromReferenceSequence;
        this.fileExtractorFromAnalysis = fileExtractorFromAnalysis;
        this.analysisRepository = analysisRepository;
        this.studyRepository = studyRepository;
        this.referenceSequenceRepository = referenceSequenceRepository;
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
            ReferenceSequence referenceSequenceSEQVAR = buildReferenceSequenceOfSEQUENCEVARIATION(analysisType);
            if (referenceSequenceSEQVAR != null) {
                referenceSequenceSEQVAR = referenceSequenceRepository.findOrSave(referenceSequenceSEQVAR);
                referenceSequences.add(referenceSequenceSEQVAR);
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

    private ReferenceSequence buildReferenceSequenceOfSEQUENCEVARIATION(AnalysisType analysisType) {
        ReferenceSequence referenceSequence = null;

        AnalysisType.ANALYSISTYPE analysistype = analysisType.getANALYSISTYPE();
        if (analysistype.isSetSEQUENCEVARIATION()) {
            ReferenceSequenceType referenceSequenceType = analysistype.getSEQUENCEVARIATION();
            if (referenceSequenceType != null) {
                ReferenceAssemblyType referenceAssemblyType = referenceSequenceType.getASSEMBLY();
                if (referenceAssemblyType != null) {
                    ReferenceAssemblyType.STANDARD standard = referenceAssemblyType.getSTANDARD();
                    if (standard != null) {
                        String refName = standard.getRefname();
                        String accession = null;
                        if (refName != null) {
                            accession = REFERENCE_MAP.get(refName.toLowerCase());
                            if (accession == null) {
                                throw new IllegalArgumentException ("Encountered exception for unknown reference sequence name " + refName);
                            }
                        }

                        String[] refNameSplit = refName.split(".");
                        String patch = null;
                        if (refNameSplit.length == 2) {
                            patch = refNameSplit[1];
                        }
                        ArrayList<String> accessionList = new ArrayList<>(Arrays.asList(accession));
                        referenceSequence = new ReferenceSequence(refName, patch, accessionList,  ReferenceSequence.Type.ASSEMBLY);
                        referenceSequence.setTaxonomy(taxonomyExtractor.getTaxonomy());
                    } //(standard != null)
                }
            }
        }
        return referenceSequence;
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
            referenceSequence = referenceSequenceRepository.findOrSave(referenceSequence);
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
            String accession = getAccessionFromReferenceSequenceType(analysisType1.getREFERENCEALIGNMENT());
            if (accession != null) {
                referenceSequenceAccessions.add(accession);
            }
        }
        if (analysisType1.isSetSEQUENCEVARIATION()) {
            String accession = getAccessionFromReferenceSequenceType(analysisType1.getSEQUENCEVARIATION());
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

    protected String getAccessionFromStandard(ReferenceAssemblyType.STANDARD standard) {
        return standard.getAccession();
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

}
