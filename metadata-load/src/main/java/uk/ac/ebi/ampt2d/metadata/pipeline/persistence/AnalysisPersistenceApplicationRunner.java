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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.pipeline.loader.extractor.FileExtractorFromAnalysis;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalysisPersistenceApplicationRunner implements ApplicationRunner {

    private static final String ANALYSIS_ACCESSION_FILE_PATH = "analysisAccession.file.path";

    private static final Logger ANALYSIS_PERSISTENCE_APPLICATION_LOGGER =
            Logger.getLogger(AnalysisPersistenceApplicationRunner.class.getName());

    private FileExtractorFromAnalysis fileExtractorFromAnalysis;

    public AnalysisPersistenceApplicationRunner(FileExtractorFromAnalysis fileExtractorFromAnalysis) {
        this.fileExtractorFromAnalysis = fileExtractorFromAnalysis;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        List<String> analysisAccessions = readAccessionsFromFile(arguments);
        for (String analysisAccession : analysisAccessions) {
            //TODO Analysis and dependents convertion to metadata model and storing in db
            List<File> files = fileExtractorFromAnalysis.extractFilesFromAnalysis(analysisAccession);
        }

    }

    private List<String> readAccessionsFromFile(ApplicationArguments arguments) throws Exception {
        List<String> analysisAccessionsFilePath = arguments.getOptionValues(ANALYSIS_ACCESSION_FILE_PATH);
        List<String> analysisAccessions = new ArrayList<>();
        if (analysisAccessionsFilePath != null) {
            try {
                analysisAccessions = Files.readAllLines(Paths.get(getClass().getClassLoader()
                        .getResource(analysisAccessionsFilePath.get(0)).toURI()));
            } catch (Exception e) {
                ANALYSIS_PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Provided file path is invalid");
                throw new RuntimeException("Provided file path is invalid/file does not exists");
            }
        }

        return analysisAccessions;
    }

}
