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
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@AutoConfigureMockMvc
public class StudySteps {

    @Autowired
    private MockMvc mockMvc;

    @When("I create a study with (.*) for taxonomy$")
    public void createTestStudy(String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy("EGAS0001", 1, "test_human_study", false, LocalDate.now(), testTaxonomyKey));
    }

    @When("^I request POST /studies with JSON-like payload:$")
    public void postTestStudy(String jsonLikeData) throws Exception {
        String[] values = jsonLikeData.split(",");
        String json = "{";
        for (String value : values) {
            if (value.contains("releaseDate")) {
                json += "\"releaseDate\": \"";
                if (jsonLikeData.contains("today")) {
                    json += LocalDate.now();
                } else if (jsonLikeData.contains("yesterday")) {
                    json += LocalDate.now().plusDays(-1);
                } else if (jsonLikeData.contains("tomorrow")) {
                    json += LocalDate.now().plusDays(+1);
                }
                json += "\",";
                continue;
            } else if (value.contains("taxonomy")) {
                String taxonomyKey = value.substring(value.indexOf(":") + 1);
                taxonomyKey = taxonomyKey.replace("\"", "").trim();
                String taxonomyUrl = CommonStates.getUrl(taxonomyKey);
                json += "\"taxonomy\": \"" + taxonomyUrl + "\",";
                continue;
            }
            json += value + ",";
        }
        json += "\"description\": \"Nothing important\"," +
                "\"center\": \"EBI\"}";

        CommonStates.setResultActions(mockMvc.perform(post("/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.getBytes())));
    }

    @When("^I request GET for the studies with query parameter (.*)")
    public void performGetOnResourcesQuery(String param) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies" + "?" + param)));
    }

    @When("^I request PATCH (.*) with patch and content (.*)")
    public void performPatchOnResourceWithContent(String urlKey, String content) throws Exception {
            CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey) + "/patch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)));
    }

    @When("^I request PATCH (.*) with patch and day (.*)")
    public void performPatchedPatchOnResourceWithDay(String urlKey, String day) throws Exception {
        int intDay = 0;
        if (day.equals("today")) {
            intDay = 0;
        } else if (day.equals("yesterday")) {
            intDay = -1;
        } else if (day.equals("tomorrow")) {
            intDay = 1;
        }
        String content = "{ \"releaseDate\" : \"";
        content += LocalDate.now().plusDays(intDay);
        content += "\" }";

        CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey) + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)));
    }

    @When("^I request search studies having release (.*) today")
    public void performSearchOnResourcesWithBaseAndParametersAndDay(String parameter) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/release-date?"+parameter+"="+LocalDate.now())));
    }

    @When("^I request elaborate search with date range for the studies base (.*) and with the parameters: (\\d*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDayRange(String base, int day) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base+"?"+"from="+LocalDate.now().plusDays(day)+"&to="+LocalDate.now().plusDays(day))));
    }

    @When("^I request search for the studies with base (.*) and name (.*) value (.*)$")
    public void performSearchOnResourcesWithParameters(String base, String name, String value) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base).param(name, value)));
    }

    @When("^I request search for studies that have been released")
    public void performSearchOnResources() throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/release-date")));
    }

    @Then("^the response should contain one study$")
    public void checkResponseListSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1));
    }

    @Then("^the response should contain field releaseDate$")
    public void checkResponseJsonFieldValueExist() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.releaseDate").exists());
    }

    @Then("^the response should contain field (.*) with a false boolean value$")
    public void checkResponseJsonFieldValueFalse(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(false));
    }

    @Then("^the response should not contain field (.*)$")
    public void checkResponseJsonNoField(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).doesNotExist());
    }

    private ResultActions postTestStudy(String accession, int version, String name, boolean deprecated, LocalDate releaseDate, String testTaxonomyKey) throws Exception {
        String jsonContent = "{" +
                "      \"accessionVersionId\": {" +
                "       \"accession\": \"" + accession +  "\"," +
                "        \"version\": " + version +
                "        }," +
                "      \"name\": \"" + name + "\"," +
                "      \"description\": \"Nothing important\"," +
                "      \"center\": \"EBI\"," +
                "      \"deprecated\": \"" + deprecated + "\"," +
                "      \"releaseDate\": \"" + releaseDate + "\"," +
                "      \"taxonomy\": \"" + CommonStates.getUrl(testTaxonomyKey) + "\"" +
                "    }";

        return mockMvc.perform(post("/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated());
    }
}
