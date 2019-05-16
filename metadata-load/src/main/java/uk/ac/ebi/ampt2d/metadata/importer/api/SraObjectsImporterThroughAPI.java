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

package uk.ac.ebi.ampt2d.metadata.importer.api;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraXmlRetrieverThroughDatabase;
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
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.ReferenceAssemblyType;
import uk.ac.ebi.ena.sra.xml.ReferenceSequenceType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SraObjectsImporterThroughAPI implements ObjectsImporter {

    private static final Logger IMPORT_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

    protected SraXmlRetrieverByAccession sraXmlRetrieverByAccession;

    private SraXmlParser<StudyType> sraStudyXmlParser;

    private SraXmlParser<AnalysisType> sraAnalysisXmlParser;

    private Converter<StudyType, Study> studyConverter;

    private Converter<AnalysisType, Analysis> analysisConverter;

    private PublicationExtractorFromStudy publicationExtractorFromStudy;

    private WebResourceExtractorFromStudy webResourceExtractorFromStudy;

    private TaxonomyExtractor taxonomyExtractor;

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public SraObjectsImporterThroughAPI(SraXmlRetrieverByAccession sraXmlRetrieverByAccession,
                                        SraXmlParser<StudyType> sraStudyXmlParser,
                                        Converter<StudyType, Study> studyConverter,
                                        PublicationExtractorFromStudy publicationExtractorFromStudy,
                                        WebResourceExtractorFromStudy webResourceExtractorFromStudy,
                                        TaxonomyExtractor taxonomyExtractor,
                                        SraXmlParser<AnalysisType> sraAnalysisXmlParser,
                                        Converter<AnalysisType, Analysis> analysisConverter,
                                        FileExtractorFromAnalysis fileExtractorFromAnalysis) {
        this.sraXmlRetrieverByAccession = sraXmlRetrieverByAccession;
        this.sraStudyXmlParser = sraStudyXmlParser;
        this.studyConverter = studyConverter;
        this.taxonomyExtractor = taxonomyExtractor;
        this.publicationExtractorFromStudy = publicationExtractorFromStudy;
        this.webResourceExtractorFromStudy = webResourceExtractorFromStudy;
        this.sraAnalysisXmlParser = sraAnalysisXmlParser;
        this.analysisConverter = analysisConverter;
        this.fileExtractorFromAnalysis = fileExtractorFromAnalysis;
    }

    @Override
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
            if (!(sraXmlRetrieverByAccession instanceof SraXmlRetrieverThroughDatabase)) {
                for (String analysisAccession : getAnalysisAccessions(studyType)) {
                    Analysis analysis = importAnalysis(analysisAccession);
                    analysis.setStudy(study);
                }
            }
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for accession" + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());

        }
        return study;
    }

    @Override
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
            if (sraXmlRetrieverByAccession instanceof SraXmlRetrieverThroughDatabase) {
                Study study = importStudyFromAnalysis(analysisType.getSTUDYREF().getAccession());
                analysis.setStudy(study);
            }
        } catch (Exception exception) {
            IMPORT_LOGGER.log(Level.SEVERE, "Encountered Exception for Analysis accession" + accession);
            IMPORT_LOGGER.log(Level.SEVERE, exception.getMessage());
        }

        return analysis;
    }

    protected Study importStudyFromAnalysis(String studyAccession) {
        return this.importStudyFromAnalysis(studyAccession);
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

    @Override
    public ReferenceSequence importReferenceSequence(String accession) {
        return null;
    }

    @Override
    public Sample importSample(String accession) {
        return null;
    }

    private Set<String> getAnalysisAccessions(StudyType studyType) {

        Set<String> analysisAccessions = new HashSet<>();

        StudyType.STUDYLINKS studylinks = studyType.getSTUDYLINKS();
        if (studylinks == null) {
            return analysisAccessions;
        }
        LinkType[] studyLinkArrays = studylinks.getSTUDYLINKArray();
        String analyses = null;
        for (int i = 0; i < studyLinkArrays.length; i++) {
            XRefType xRefType = studyLinkArrays[i].getXREFLINK();
            if (xRefType != null && xRefType.getDB().equals("ENA-ANALYSIS")) {
                analyses = xRefType.getID();
                break;
            }
        }
        if (analyses == null) {
            return analysisAccessions;
        }
        Stream.of(analyses.split(","))
                .forEach(analysis -> {
                    String[] range = analysis.split("-");
                    if (range.length > 1) {
                        String startRange = range[0];
                        String endRange = range[1];
                        String analysisAccessionPrefix = startRange.split("\\d")[0];
                        int startRangeNumericPart = Integer.valueOf(startRange.split("[A-Z]*[^\\d]")[1]);
                        int endRangeNumericPart = Integer.valueOf(endRange.split("[A-Z]*[^\\d]")[1]);
                        int lengthOfNumericPartOfAccessionWithZeros = startRange.length() - analysisAccessionPrefix.length();
                        for (int i = startRangeNumericPart; i <= endRangeNumericPart; i++) {
                            analysisAccessions.add(analysisAccessionPrefix + String.format
                                    ("%0" + lengthOfNumericPartOfAccessionWithZeros + "d", i));
                        }
                    } else {
                        analysisAccessions.add(analysis);
                    }
                });

        return analysisAccessions;
    }
}