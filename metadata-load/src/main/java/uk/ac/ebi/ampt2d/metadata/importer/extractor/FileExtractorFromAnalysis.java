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

package uk.ac.ebi.ampt2d.metadata.importer.extractor;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.converter.FileConverter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QFile;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisFileType;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileExtractorFromAnalysis {

    private static final Logger FILE_EXTRACT_SERVICE_LOGGER = Logger.getLogger(FileExtractorFromAnalysis.class.getName());

    private static final QFile qFile = QFile.file;

    private Converter<AnalysisFileType, File> fileConverter;

    private FileRepository fileRepository;

    public FileExtractorFromAnalysis(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.fileConverter = new FileConverter();
    }

    public List<File> getFiles(AnalysisType analysisType) throws Exception {
        List<File> files;

        try {
            files = fileRepository.findOrSave(getFilesOfAnalysis(analysisType));
        } catch (Exception exception) {
            String message = "Encountered exception when extracting files from analysis ";
            FILE_EXTRACT_SERVICE_LOGGER.log(Level.SEVERE, message + analysisType.getAccession());
            FILE_EXTRACT_SERVICE_LOGGER.log(Level.SEVERE, exception.getMessage());
            throw exception;
        }
        return files;
    }

    private List<File> getFilesOfAnalysis(AnalysisType analysis) {
        return Arrays.asList(analysis.getFILES().getFILEArray()).stream().map(analysisFileType -> fileConverter.convert
                (analysisFileType)).collect(Collectors.toList());
    }
}
