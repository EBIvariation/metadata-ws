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

import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.SraXmlRetrieverByAccession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SraXmlRetrieverThroughApi implements SraXmlRetrieverByAccession {

    public static final String ENA_API_URL = "https://www.ebi.ac.uk/ena/browser/api/xml/{accessionId}";

    private static final Logger XML_RETRIEVE_LOGGER = Logger.getLogger(ObjectsImporter.class.getName());

    private RestTemplate restTemplate = new RestTemplate();

    @Retryable(maxAttemptsExpression="#{${ena.api.attempts}}", backoff=@Backoff(delayExpression="#{${ena.api.delay}}"))
    @Override
    public String getXml(String accession) {
        try {
            return restTemplate.getForEntity(ENA_API_URL, String.class, accession).getBody();
        } catch (HttpClientErrorException e) {
            // When ENA XML API returns a 404 Not Found response, it represents a special non-critical class of errors.
            // In this case, import of the object should not be aborted and the transaction should not be rolled back.
            // Hence, in this case the method will return `null` in place of an XML and will not raise an excepion.
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                XML_RETRIEVE_LOGGER.log(Level.WARNING, "Accession not found in ENA " + accession);
                return null;
            }
            throw e;
        }
    }

}
