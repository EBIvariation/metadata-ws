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
package uk.ac.ebi.ampt2d.metadata.loader.api;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ena.sra.xml.ANALYSISDocument;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SraAnalysisDocumentLoader implements SraObjectLoaderByAccession<ANALYSISDocument> {

    private static final Logger SRA_ANALYSIS_LOGGER = Logger.getLogger(SraAnalysisDocumentLoader.class.getName());

    private RestTemplate restTemplate;

    public SraAnalysisDocumentLoader(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static String getEnaApiUrl() {
        return ENA_API_URL;
    }

    @Override
    public Map<String, ANALYSISDocument> getSraObjects(List<String> accessionIds) {
        return accessionIds.stream()
                .map(accessionId -> parseAnalysisType(restTemplate.exchange(ENA_API_URL,
                        HttpMethod.GET, null, String.class, accessionId).getBody(), accessionId))
                .filter(analysisDocument -> analysisDocument != null)
                .collect(Collectors.toMap(analysisDocument -> analysisDocument.getANALYSIS().getAccession(),
                        Function.identity()));
    }

    private ANALYSISDocument parseAnalysisType(String xmlString, String accessionId) {
        ANALYSISDocument analysisDocument = null;
        xmlString = removeRootTagsFromXmlString(xmlString);
        try {
            analysisDocument = ANALYSISDocument.Factory.parse(xmlString);
        } catch (Exception exception) {
            SRA_ANALYSIS_LOGGER.log(Level.SEVERE, "Parse exception for accession " + accessionId);
            exception.printStackTrace();
        }
        return analysisDocument;
    }
}
