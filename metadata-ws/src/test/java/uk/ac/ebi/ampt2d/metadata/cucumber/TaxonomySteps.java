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
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TaxonomySteps {

    public static final String EXCLUDE_RANK_EXPRESSION = "@.rank!='class' && @.rank!='order' &&  @.rank!='genus'";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Then("^the response should contain no taxonomy$")
    public void checkTaxonomyResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(0));
    }

    @When("^I request POST taxonomy with (\\d*) for ID")
    public void performPostOnTaxonomiesWithTree(long id) throws Exception {

        StringBuilder jsonContent = new StringBuilder();
        jsonContent.append("{ " +
                "\"taxonomyId\": " + id + "}");

        CommonStates.setResultActions(mockMvc.perform(post("/taxonomies")
                .with(CommonStates.getRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent.toString().getBytes())));
    }

    @Then("^the response should contain only (\\d*) taxonomies excluding class , order and genus$")
    public void checkResponseListSize(int size) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies[?(" + EXCLUDE_RANK_EXPRESSION + ")]")
                        .value(iterableWithSize(size)));
    }

    @And("^the taxonomies contains items (.*)$")
    public void checkResponseLinkedObjectHref(String urlKeys)
            throws Exception {
        String[] urls = {};
        if (!urlKeys.equals("NONE")) {
            urls = Arrays.stream(urlKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .toArray(String[]::new);
        }

        CommonStates.getResultActions()
                .andExpect(jsonPath("$..taxonomies[?(" + EXCLUDE_RANK_EXPRESSION + ")]" +
                        "..taxonomy.href", containsInAnyOrder(urls)));
    }
}
