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

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.ampt2d.metadata.security.AuthorizationServerHelper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TaxonomySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorizationServerHelper authorizationServerHelper;

    @Then("^the response should contain no taxonomy$")
    public void checkTaxonomyResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(0));
    }

    @When("^I request POST taxonomies with (\\d*) for ID, (.*) for name and (.*) for ancestors")
    public void performPostOnTaxonomies(long id, String name, String ancestorKeys) throws Exception {
        List<String> newUrls = CommonStates.getUrls(ancestorKeys);
        String jsonContent = "{ " +
                "\"taxonomyId\": " + id + "," +
                "\"name\": \"" + name + "\"," +
                "\"ancestors\": " + objectMapper.writeValueAsString(newUrls) + "" +
                "}";

        CommonStates.setResultActions(mockMvc.perform(post("/taxonomies")
                .with(authorizationServerHelper.bearerToken("testOperator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent.getBytes())));
    }

}
