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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QSample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoRepositoryBean
public interface SampleRepositoryCustom extends PagingAndSortingRepository<Sample, Long>,
        QueryDslPredicateExecutor<Sample> {
    QSample qSample = QSample.sample;

    default Sample findOrSave(Sample sample) {
        Sample existingSample = findOne(
                qSample.accessionVersionId.accession.eq(sample.getAccessionVersionId().getAccession()).and(
                qSample.accessionVersionId.version.eq(sample.getAccessionVersionId().getVersion()))
        );
        if (existingSample != null) {
            return existingSample;
        }
        return save(sample);
    }

    default List<Sample> findOrSaveList(List<Sample> sampleIn) {
        List<Sample> existingSampleList;
        Predicate predicate = qSample.accessionVersionId.accession.in(sampleIn.stream()
                .map(Sample::getAccessionVersionId).map(AccessionVersionId::getAccession).collect(Collectors.toList()))
                .and(qSample.accessionVersionId.version.in(sampleIn.stream()
                        .map(Sample::getAccessionVersionId).map(AccessionVersionId::getVersion).collect(Collectors.toList())));
        existingSampleList = (List<Sample>) findAll(predicate);
        List<String> existingSampleIdList = existingSampleList.stream().map(Sample::getAccessionVersionId)
                .map(AccessionVersionId::getAccession).collect(Collectors.toList());

        List<Sample> missingSampleList = new ArrayList<>();
        for (Sample s : sampleIn) {
            if (!existingSampleIdList.contains(s.getAccessionVersionId().getAccession())) {
                missingSampleList.add(s);
            }
        }
        missingSampleList = (List<Sample>) save(missingSampleList);

        List<Sample> sampleList = new ArrayList<>();
        sampleList.addAll(existingSampleList);
        sampleList.addAll(missingSampleList);
        return sampleList;
    }

}
