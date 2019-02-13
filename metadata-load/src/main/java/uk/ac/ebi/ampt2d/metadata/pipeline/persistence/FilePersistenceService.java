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

package uk.ac.ebi.ampt2d.metadata.pipeline.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.pipeline.extractor.FileExtractorFromAnalysis;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.core.xml.SraXmlParser;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@ConditionalOnProperty(name = "import.object", havingValue = "files")
public class FilePersistenceService implements ApplicationRunner {

    public static final String ANALYSIS_ACCESSION_FILE_PATH = "analysisAccession.file.path";

    private static final Logger FILE_PERSIST_SERVICE_LOGGER = Logger.getLogger(FilePersistenceService.class.getName());

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SraRetrieverByAccession sraRetrieverByAccession;

    @Autowired
    private SraXmlParser<AnalysisType> sraXmlParser;

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        List<String> analysisAccessions = readAccessionsFromFile(arguments);
        List<File> files = extractFilesFromAnalysis(analysisAccessions);
        persistFilesToDatabase(files);
    }

    private List<String> readAccessionsFromFile(ApplicationArguments arguments) throws Exception {
        List<String> analysisAccessionsFilePath = arguments.getOptionValues(ANALYSIS_ACCESSION_FILE_PATH);
        List<String> analysisAccessions = new ArrayList<>();
        if (analysisAccessionsFilePath != null) {
            analysisAccessions = Arrays.asList(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                    .getResource(analysisAccessionsFilePath.get(0)).toURI()))).split("\n"));
        }

        return analysisAccessions;
    }

    private List<File> extractFilesFromAnalysis(List<String> analysisAccessions) {
        FileExtractorFromAnalysis fileExtractorFromAnalysis = new FileExtractorFromAnalysis();
        List<File> files = new ArrayList<>();
        for (String analysisAccession : analysisAccessions) {
            try {
                String analysisXml = sraRetrieverByAccession.getXml(analysisAccession);
                AnalysisType analysisType = sraXmlParser.parseXml(analysisXml, analysisAccession);
                files.addAll(fileExtractorFromAnalysis.getFilesOfAnalysis(analysisType));
            } catch (Exception exception) {
                FILE_PERSIST_SERVICE_LOGGER.log(Level.INFO, "Encountered Exception for analysis "
                        + analysisAccession + exception.getMessage());
            }
        }
        return files;
    }

    private void persistFilesToDatabase(List<File> files) {
        FILE_PERSIST_SERVICE_LOGGER.log(Level.INFO, "Files count to be persisted : " + files.size());
        int savedFileCount = 0;
        for (File file : files) {
            try {
                fileRepository.save(file);
                savedFileCount++;
            } catch (Exception e) {
                FILE_PERSIST_SERVICE_LOGGER.log(Level.INFO, "Duplicate File" + file.getHash());
            }
        }
        FILE_PERSIST_SERVICE_LOGGER.log(Level.INFO, "Successfully Persisted Files count : " + savedFileCount);
    }

}
