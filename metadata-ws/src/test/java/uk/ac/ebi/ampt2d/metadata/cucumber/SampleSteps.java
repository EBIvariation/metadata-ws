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
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Ignore
@AutoConfigureMockMvc
public class SampleSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I create a sample with (.*) for taxonomy")
    public void createTestSample(String testTaxonomyKeys) throws Exception {
        createTestSampleParameterized("EGAS0001", 1, "test_human_sample", testTaxonomyKeys);
    }

    @When("I create a parameterized sample with (.*) for accession, (.*) for version, (.*) for name and (.*) for taxonomy")
    public void createTestSampleParameterized(String accession, int version, String name, String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = CommonStates.getUrls(testTaxonomyKeys);
        CommonStates.setResultActions(postTestSample(accession, name, testTaxonomyList, version));
    }

    @When("I create a non-accessioned sample with (.*) for name and (.*) for taxonomy")
    public void createTestSampleParameterizedNoAccession(String name, String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = CommonStates.getUrls(testTaxonomyKeys);
        // no accession
        CommonStates.setResultActions(postTestSampleNoOrNullAccession(false, name, testTaxonomyList));
    }

    @When("I provide a null accession for a sample with (.*) for name and (.*) for taxonomy")
    public void createTestSampleParameterizedNullAccession(String name, String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = CommonStates.getUrls(testTaxonomyKeys);
        // null accession
        CommonStates.setResultActions(postTestSampleNoOrNullAccession(true, name, testTaxonomyList));
    }

    @Then("^the response should contain no sample$")
    public void checkStudyResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(0));
    }

    private ResultActions postTestSample(String accession, String name, List<String> testTaxonomyList, int version) throws Exception {
        String jsonContent = "{ " +
                "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\": " + version + "}," +
                "\"name\": \"" + name + "\"";
        if (testTaxonomyList != null) {
            jsonContent = jsonContent +
                    ",\"taxonomies\": " + objectMapper.writeValueAsString(testTaxonomyList);
        }
        jsonContent = jsonContent + "}";

        return mockMvc.perform(post("/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));
    }


    private ResultActions postTestSampleNoOrNullAccession(boolean accession, String name, List<String> testTaxonomyList) throws Exception {
        String jsonContent = "{ " +
                "\"name\": \"" + name + "\"";
        if (accession == true) {
            jsonContent = jsonContent +  ", \"accessionVersionId\":{ \"accession\":" + null + ",\"version\": " + 1 + "}" ;
        }
        if (testTaxonomyList != null) {
            jsonContent = jsonContent +
                    ", \"taxonomies\": " + objectMapper.writeValueAsString(testTaxonomyList);
        }
        jsonContent = jsonContent + "}";

        return mockMvc.perform(post("/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));
    }

}
