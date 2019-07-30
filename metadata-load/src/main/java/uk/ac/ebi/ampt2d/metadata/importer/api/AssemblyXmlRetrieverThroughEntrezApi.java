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
import org.springframework.web.client.RestTemplate;

public class AssemblyXmlRetrieverThroughEntrezApi {

    private static final String ID_START_TAG = "<Id>";

    private static final String ID_END_TAG = "</Id>";

    private static final String ENTREZ_API_KEY_QUERY = "&api_key={entrezApiKey}";

    /*
     *  URL to obtain an internal Entrez ID from, given a GCF assembly accession.
    */
    private String entrezApiIdRetrievalUrl =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=assembly&term={accession}";

    /*
     *  URL to obtain assembly metadata from, given its internal Entrez ID.
    */
    private String entrezApiAssemblyRetrievalUrl =
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=assembly&id={id}";

    private RestTemplate restTemplate = new RestTemplate();

    private String entrezApiKey;

    public AssemblyXmlRetrieverThroughEntrezApi(String entrezApiKey) {
        if (entrezApiKey != null && !entrezApiKey.isEmpty()) {
            this.entrezApiKey = entrezApiKey;
            entrezApiIdRetrievalUrl = entrezApiIdRetrievalUrl + ENTREZ_API_KEY_QUERY;
            entrezApiAssemblyRetrievalUrl = entrezApiAssemblyRetrievalUrl + ENTREZ_API_KEY_QUERY;
        }
    }

    public String getXml(String accession) {
        String idXml = restTemplate.exchange(entrezApiIdRetrievalUrl, HttpMethod.GET, null, String.class,
                accession, entrezApiKey).getBody();
        String id = idXml.substring(idXml.indexOf(ID_START_TAG) + 4, idXml.indexOf(ID_END_TAG));
        return restTemplate.exchange(entrezApiAssemblyRetrievalUrl, HttpMethod.GET, null, String.class,
                id, entrezApiKey).getBody();
    }
}