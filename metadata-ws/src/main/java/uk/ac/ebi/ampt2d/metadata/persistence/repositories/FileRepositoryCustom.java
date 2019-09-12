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

package uk.ac.ebi.ampt2d.metadata.persistence.repositories;

import com.querydsl.core.types.Predicate;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoRepositoryBean
public interface FileRepositoryCustom extends PagingAndSortingRepository<File, Long>,
        QueryDslPredicateExecutor<File> {
    QFile qFile = QFile.file;

    default List<File> findOrSave(List<File> files) {
        List<File> existingFiles;
        Predicate predicate = qFile.name.concat(qFile.hash).in(files.parallelStream().map(file -> file.getName() + file
                .getHash()).collect(Collectors.toList()));
        existingFiles = (List<File>) findAll(predicate);
        Set<String> existingFileNamesAndHash = existingFiles.parallelStream().map(file -> file.getName() + file.getHash
                ()).collect(Collectors.toSet());
        List<File> notSavedFiles = files.parallelStream().filter(file -> !existingFileNamesAndHash.contains(file
                .getName() + file.getHash())).collect(Collectors.toList());
        files.clear();
        files.addAll(existingFiles);
        files.addAll((List<File>) save(notSavedFiles));
        return files;
    }

}