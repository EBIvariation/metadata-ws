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

package uk.ac.ebi.ampt2d.metadata.importer.persistence;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalysisPersistenceApplicationRunner implements ApplicationRunner {

    private static final String ANALYSIS_ACCESSION_FILE_PATH = "analysisAccession.file.path";

    private static final Logger ANALYSIS_PERSISTENCE_APPLICATION_LOGGER =
            Logger.getLogger(AnalysisPersistenceApplicationRunner.class.getName());

    private SraRetrieverByAccession analyisRetriever;

    private SraXmlParser sraXmlParser;

    private Converter<AnalysisType, Analysis> analysisConverter;

    private AnalysisRepository analysisRepository;

    public AnalysisPersistenceApplicationRunner(SraRetrieverByAccession analyisRetriever, SraXmlParser sraXmlParser,
                                                AnalysisRepository analysisRepository,
                                                Converter<AnalysisType, Analysis> analysisConverter) {
        this.analyisRetriever = analyisRetriever;
        this.sraXmlParser = sraXmlParser;
        this.analysisRepository = analysisRepository;
        this.analysisConverter = analysisConverter;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        Set<String> analysisAccessions = readAccessionsFromFile(arguments);

        for (String analysisAccession : analysisAccessions) {
            try {
                String xml = analyisRetriever.getXml(analysisAccession);
                AnalysisType analysisType = (AnalysisType) sraXmlParser.parseXml(xml, analysisAccession);
                Analysis analysis = analysisConverter.convert(analysisType);
                analysisRepository.save(analysis);
            } catch (Exception exception) {
                ANALYSIS_PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Encountered Exception for analysis"
                        + analysisAccession);
                ANALYSIS_PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, exception.getMessage());
            }
        }
    }

    private Set<String> readAccessionsFromFile(ApplicationArguments arguments) {
        List<String> analysisAccessionsFilePath = arguments.getOptionValues(ANALYSIS_ACCESSION_FILE_PATH);
        if (analysisAccessionsFilePath == null || analysisAccessionsFilePath.size() == 0) {
            ANALYSIS_PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Please provide analysisAccession file path");
            throw new RuntimeException("Please provide analysisAccession file path");
        }
        String accessionFilePath = analysisAccessionsFilePath.get(0);
        Set<String> analysisAccessions;
        try {
            analysisAccessions = new HashSet<>(Files.readAllLines(Paths.get(getClass().getClassLoader()
                    .getResource(accessionFilePath).toURI())));
        } catch (NullPointerException | URISyntaxException exception) {
            ANALYSIS_PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Provided file path is invalid");
            throw new RuntimeException("Provided file path is invalid/file does not exists");
        } catch (IOException exception) {
            ANALYSIS_PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Provided file is not valid/corrupt");
            throw new RuntimeException("Provided file is not valid/corrupt");
        }

        return analysisAccessions;
    }

}
