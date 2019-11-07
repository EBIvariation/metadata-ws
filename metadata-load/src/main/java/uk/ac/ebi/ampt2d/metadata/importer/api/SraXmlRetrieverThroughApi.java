/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
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

import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;

public class SraXmlRetrieverThroughApi implements SraXmlRetrieverByAccession {

    public static final String ENA_API_URL = "https://www.ebi.ac.uk/ena/data/view/{accessionId}&display=xml";

    private RestTemplate restTemplate = new RestTemplate();

    @Retryable(maxAttemptsExpression="#{${ena.api.attempts}}",
            backoff=@Backoff(delayExpression="#{${ena.api.delay}}"))
    @Override
    public String getXml(String accession) {
        return restTemplate.exchange(ENA_API_URL, HttpMethod.GET, null, String.class, accession).getBody();
    }

}
