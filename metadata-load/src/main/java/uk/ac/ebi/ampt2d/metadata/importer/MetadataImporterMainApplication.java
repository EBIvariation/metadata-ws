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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraObjectsImporterThroughAPI;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraObjectsImporterThroughDatabase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class MetadataImporterMainApplication implements ApplicationRunner {

    private static final String ACCESSION_FILE_PATH = "accessions.file.path";

    private static final Logger METADATA_IMPORTER_MAIN_APPLICATION_LOGGER =
            Logger.getLogger(MetadataImporterMainApplication.class.getName());

    private ObjectsImporter objectsImporter;

    public MetadataImporterMainApplication(ObjectsImporter objectsImporter) {
        this.objectsImporter = objectsImporter;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MetadataImporterMainApplication.class, args);
    }

    /**
     * This method executes the task of importing studies or analyses based on import source.
     * We are starting with analyses in case of objects import through database because the study xmls in database
     * do not contain analysis accessions to import.
     *
     * @param applicationArguments
     */
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        Set<String> accessions = readAccessionsFromFile(applicationArguments);
        if (objectsImporter instanceof SraObjectsImporterThroughDatabase) {
            accessions.parallelStream().forEach(accession -> objectsImporter.importAnalysis(accession));
        } else if (objectsImporter instanceof SraObjectsImporterThroughAPI) {
            accessions.parallelStream().forEach(accession -> objectsImporter.importStudy(accession));
        } else {
            throw new RuntimeException("ObjectsImporter instance not known/supported");
        }
    }

    private Set<String> readAccessionsFromFile(ApplicationArguments applicationArguments) {
        List<String> accessionsFilePath = applicationArguments.getOptionValues(ACCESSION_FILE_PATH);
        if (accessionsFilePath == null || accessionsFilePath.size() == 0) {
            METADATA_IMPORTER_MAIN_APPLICATION_LOGGER.log(Level.SEVERE, "Please provide accessions.file.path");
            throw new RuntimeException("Please provide accessions.file.path");
        }
        String accessionFilePath = accessionsFilePath.get(0);
        Set<String> accessions;
        try {
            accessions = new HashSet<>(Files.readAllLines(Paths.get(MetadataImporterMainApplication.class
                    .getClassLoader().getResource(accessionFilePath).toURI())));
        } catch (NullPointerException | URISyntaxException exception) {
            String message = "Provided file path is invalid/file does not exists";
            METADATA_IMPORTER_MAIN_APPLICATION_LOGGER.log(Level.SEVERE, message);
            throw new RuntimeException(message);
        } catch (IOException exception) {
            String message = "Provided file is not valid/corrupt";
            METADATA_IMPORTER_MAIN_APPLICATION_LOGGER.log(Level.SEVERE, message);
            throw new RuntimeException(message);
        }

        return accessions;
    }
}
