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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@AutoConfigureMockMvc
public class StudySteps {

    @Autowired
    private MockMvc mockMvc;

    @When("user create a test study with (.*) for taxonomy$")
    public void createTestStudy(String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy("EGAS0001", 1, "test_human_study", false, LocalDate.now(), testTaxonomyKey));
    }

    @When("user create a test parameterized study with (.*) for accession, (.*) for version, (.*) for name, (.*) for deprecated, (.*) for releaseDay and (.*) for taxonomy$")
    public void createTestStudyParameterizedMore(String accession, int version, String name, boolean deprecated, int releaseDay, String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy(accession, version, name, deprecated, LocalDate.now().plusDays(releaseDay), testTaxonomyKey));
    }

    @When("^user request GET for the studies with query parameter (.*)")
    public void performGetOnResourcesQuery(String param) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies" + "?" + param)));
    }

    @When("^user request PATCH (.*) with patch and content (.*)")
    public void performPatchOnResourceWithContent(String urlKey, String content) throws Exception {
            CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey) + "/patch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)));
    }

    @When("^user request PATCH (.*) with patch and day (.*)")
    public void performPatchedPatchOnResourceWithDay(String urlKey, int day) throws Exception {
        String content = "{ \"releaseDate\" : \"";
        content += LocalDate.now().plusDays(day);
        content += "\" }";

        CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey) + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)));
    }

    @When("^user request elaborate search for the studies base (.*) and with the parameters: (.*)$")
    public void performSearchOnResourcesWithBaseAndParameters(String base, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base+"?"+parameters)));
    }

    @When("^user request elaborate search with day for the studies base (.*) and with the parameters: (.*) and (\\d*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDay(String base, String parameters, int day) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base+"?"+parameters+LocalDate.now().plusDays(day))));
    }

    @When("^user request elaborate search with date range for the studies base (.*) and with the parameters: (\\d*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDayRange(String base, int day) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base+"?"+"from="+LocalDate.now().plusDays(day)+"&to="+LocalDate.now().plusDays(day))));
    }

    @When("^user request search for the studies with base (.*) and name (.*) value (.*)$")
    public void performSearchOnResourcesWithParameters(String base, String name, String value) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base).param(name, value)));
    }

    @When("^user request search for studies with (.*)")
    public void performSearchOnResources(String param) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+param)));
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
