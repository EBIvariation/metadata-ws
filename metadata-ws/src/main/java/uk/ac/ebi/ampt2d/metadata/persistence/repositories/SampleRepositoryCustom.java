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

    default List<Sample> findOrSave(List<Sample> sampleIn) {
        if (sampleIn.isEmpty()) {
            return sampleIn;
        }
        List<Sample> existingSamples;
        Predicate predicate = qSample.accessionVersionId.accession.concat(qSample.accessionVersionId.version.stringValue())
                .in(sampleIn.stream().map(s -> s.getAccessionVersionId().getAccession()
                + s.getAccessionVersionId().getVersion()).collect(Collectors.toList()));
        existingSamples = (List<Sample>) findAll(predicate);
        List<AccessionVersionId> existingSampleIdList = existingSamples.stream().map(Sample::getAccessionVersionId)
                .collect(Collectors.toList());

        List<Sample> missingSamples = sampleIn.stream()
                .filter(s -> !existingSampleIdList.contains(s.getAccessionVersionId())).collect(Collectors.toList());
        missingSamples = (List<Sample>) save(missingSamples);

        List<Sample> samples = new ArrayList<>();
        samples.addAll(existingSamples);
        samples.addAll(missingSamples);
        return samples;
    }

}
