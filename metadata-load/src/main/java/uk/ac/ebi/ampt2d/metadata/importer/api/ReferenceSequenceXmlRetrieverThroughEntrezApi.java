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

package uk.ac.ebi.ampt2d.metadata.importer.api;

import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;

public class ReferenceSequenceXmlRetrieverThroughEntrezApi {

    private static final String ID_START_TAG = "<Id>";

    private static final String ID_END_TAG = "</Id>";

    private static final String ENTREZ_API_KEY_QUERY = "&api_key={entrezApiKey}";

    private static final int ID_START_TAG_LENGTH = 4;

    /*
     *  URL to obtain an internal Entrez ID from, given an NCBI database and a sequence accession
    */
    private String entrezApiIdRetrievalUrl =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db={entrezDatabase}&term={accession}";

    /*
     *  URL to obtain assembly metadata from, given its internal Entrez ID.
    */
    private String entrezApiAssemblyRetrievalUrl =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db={entrezDatabase}&id={id}";

    private RestTemplate restTemplate = new RestTemplate();

    private String entrezApiKey;

    public ReferenceSequenceXmlRetrieverThroughEntrezApi(String entrezApiKey) {
        if (entrezApiKey != null && !entrezApiKey.isEmpty()) {
            this.entrezApiKey = entrezApiKey;
            entrezApiIdRetrievalUrl = entrezApiIdRetrievalUrl + ENTREZ_API_KEY_QUERY;
            entrezApiAssemblyRetrievalUrl = entrezApiAssemblyRetrievalUrl + ENTREZ_API_KEY_QUERY;
        }
    }

    private String fetchEntrezId(String accession, String entrezDatabase) {
        return restTemplate.exchange(
                entrezApiIdRetrievalUrl, HttpMethod.GET, null, String.class,
                entrezDatabase, accession, entrezApiKey).getBody();
    }

    private String fetchEntrezData(String id, String entrezDatabase) {
        return restTemplate.exchange(
                entrezApiAssemblyRetrievalUrl, HttpMethod.GET, null, String.class,
                entrezDatabase, id, entrezApiKey).getBody();
    }

    @Retryable(maxAttemptsExpression="#{${entrez.api.attempts}}",
            backoff=@Backoff(delayExpression="#{${entrez.api.delay}}"))
    public String getXml(String accession, String entrezDatabase) {
        String idXml = fetchEntrezId(accession, entrezDatabase);
        String id = idXml.substring(idXml.indexOf(ID_START_TAG) + ID_START_TAG_LENGTH, idXml.indexOf(ID_END_TAG));
        return fetchEntrezData(id, entrezDatabase);
    }

}
