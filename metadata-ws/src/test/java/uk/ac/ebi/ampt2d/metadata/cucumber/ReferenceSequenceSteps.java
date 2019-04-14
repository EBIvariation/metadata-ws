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
package uk.ac.ebi.ampt2d.metadata.cucumber;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ReferenceSequenceSteps {

    @Autowired
    private MockMvc mockMvc;

    @Then("^the response should contain one reference sequence$")
    public void checkResponseListSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1));
    }

    @When("^I request POST /reference-sequences with JSON-like payload:$")
    public void performPostOnResourceUriWithJsonData(String jsonLikePayload) throws Exception {
        String[] values = jsonLikePayload.split(",");
        String json = "{";
        for (String value : values) {
            if (value.contains("taxonomy")) {
                String taxonomyKey = value.substring(value.indexOf(":") + 1);
                taxonomyKey = taxonomyKey.replace("\"", "").trim();
                String taxonomyUrl = CommonStates.getUrl(taxonomyKey);
                json += "\"taxonomy\": \"" + taxonomyUrl + "\"";
                continue;
            }
            json += value + ",";
        }
        json += "}";
        CommonStates.setResultActions(mockMvc.perform(post("/reference-sequences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.getBytes())));
    }
}
