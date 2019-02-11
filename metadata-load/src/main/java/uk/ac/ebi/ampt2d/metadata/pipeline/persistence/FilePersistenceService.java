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
import uk.ac.ebi.ampt2d.metadata.pipeline.converter.SraToAmpt2dConverter;
import uk.ac.ebi.ampt2d.metadata.pipeline.importer.SraObjectExtractorFromAnalysis;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "import.object", havingValue = "files")
public class FilePersistenceService implements ApplicationRunner {

    public static final String ANALYSIS_ACCESSION_FILE_PATH = "analysisAccession.file.path";

    private static final Logger FILE_PERSIST_SERVICE_LOGGER = Logger.getLogger(FilePersistenceService.class.getName());

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SraToAmpt2dConverter<AnalysisFileType, File> fileTypeConverter;

    @Autowired
    private SraObjectExtractorFromAnalysis sraObjectExtractorFromAnalysis;

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        List<String> analysisAccessions = readAccessionsFromFile(arguments);
        Map<String, List<AnalysisFileType>> analysisAccessionToFiles = getSraObjectsFromAnalysis(analysisAccessions);
        List<File> files = convertAnalysisFilesToMetadataFiles(analysisAccessionToFiles);
        persistFilesToDatabase(files);
    }

    private Map<String, List<AnalysisFileType>> getSraObjectsFromAnalysis(List<String> accessions) {
        FILE_PERSIST_SERVICE_LOGGER.log(Level.INFO,
                "Fetching files from analysis.AnalysisAccessionsCount: " + accessions.size());
        return sraObjectExtractorFromAnalysis.getSraObjectsFromAnalysis(accessions);
    }

    private List<File> convertAnalysisFilesToMetadataFiles(Map<String, List<AnalysisFileType>>
                                                                   analysisAccessionToFiles) {
        FILE_PERSIST_SERVICE_LOGGER.log(Level.INFO,
                "Converting AnalysisFiles To Metadata Files.AnalysisCount: " + analysisAccessionToFiles.size());
        return fileTypeConverter.convertModels(analysisAccessionToFiles.values().stream()
                .flatMap(l -> l.stream()).collect(Collectors.toList()));
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

    private List<String> readAccessionsFromFile(ApplicationArguments arguments) throws Exception {
        List<String> analysisAccessionsFilePath = arguments.getOptionValues(ANALYSIS_ACCESSION_FILE_PATH);
        List<String> analysisAccessions = new ArrayList<>();
        if (analysisAccessionsFilePath != null) {
            analysisAccessions = Arrays.asList(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                    .getResource(analysisAccessionsFilePath.get(0)).toURI()))).split("\n"));
        }

        return analysisAccessions;
    }

}
