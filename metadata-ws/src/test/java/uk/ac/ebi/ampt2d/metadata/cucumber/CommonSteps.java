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
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CommonSteps {

    public static final String STUDY_NON_EXISTING_URL = "https://nohost//studies/999";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private WebResourceRepository webResourceRepository;

    @Before
    public void cleanDatabases() {
        analysisRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        fileRepository.deleteAll();
        sampleRepository.deleteAll();
        studyRepository.deleteAll();
        taxonomyRepository.deleteAll();
        webResourceRepository.deleteAll();
    }

    @Before
    public void cleanStates() {
        CommonStates.clear();
    }

    /* perform http request */

    @When("^user request GET ([\\S]*)$")
    public void performGetOnResourceUri(String resourceUri) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(resourceUri)));
    }

    @When("^user request GET with value of (.*)$")
    public void performGetWithResourceUriKey(String resourceUriKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(CommonStates.getUrl(resourceUriKey))));
    }

    @When("^user request GET for (.*) of (.*)$")
    public void performGetForLinkedObjects(String className, String resourceUriKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(CommonStates.getUrl(resourceUriKey)+"/"+className)));
    }

    @When("^user request POST (.*) with json data:$")
    public void performPostOnResourceUriWithJsonData(String resourceUri, String jsonData) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(post(resourceUri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData.getBytes())));
    }

    @When("^user request PATCH (.*) with list (.*) for (.*)")
    public void performPatchOnResourceWithLinkedObject(String urlKey, String linkedObjectUrlKeys,
                                                       String linkedObjectClassName) throws Exception {
        List<String> newUrls = null;
        if (!linkedObjectUrlKeys.equals("NONE")) {
            newUrls = Arrays.stream(linkedObjectUrlKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .collect(Collectors.toList());
        }
        String jsonContent = "{"
                + "\"" + linkedObjectClassName + "\":" + objectMapper.writeValueAsString(newUrls)
                + "}";

        CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey))
                .content(jsonContent)));
    }

    @When("^user request PATCH (.*) with content (.*)")
    public void performPatchOnResourceWithContent(String urlKey, String content) throws Exception {
            CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey))
                    .content(content)));
    }

    @When("^user request DELETE for the (.*) of (.*) of the (.*)")
    public void performDeleteOnResourceWithLinkedObject(String className, String linkedObjectUrlKey,
                                                        String resourceUrlKey) throws Exception {

        String linkedObjectUrl = CommonStates.getUrl(linkedObjectUrlKey);
        String resourceUrl = CommonStates.getUrl(resourceUrlKey);
        if (linkedObjectUrl == null || resourceUrl == null) {
            return;
        }

        String idStr = linkedObjectUrl.substring(linkedObjectUrl.lastIndexOf('/') + 1);
        CommonStates.setResultActions(mockMvc.perform(delete(resourceUrl + "/" + className + "/" + idStr)));
    }

    @When("^user request search for the (.*) with the parameters: (.*)$")
    public void performSearchOnResourcesWithParameters(String className, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search?"+parameters)));
    }

    @When("^user request elaborate find for the (.*) bases (.*) with the parameters: (.*) and (.*)$")
    public void performFindOnResourcesWithBaseAndParameters(String className, String bases, String parameters, String separator) throws Exception {
        String[] params = parameters.split("&");
        String[] base = bases.split(",");
        String query = base[0] + separator + params[0];
        for (int i = 1; i < params.length; i++) {
            query += "&" + base[i] + separator + params[i];
        }
        CommonStates.setResultActions(mockMvc.perform(get("/" + className + "?" + query)));
    }

    @And("^set the URL to (.*)$")
    public void setUrlTo(String resourceUriKey) {
        CommonStates.setUrl(resourceUriKey, CommonStates.getResultActions()
                .andReturn().getResponse().getHeader("Location"));
    }

    /* check http response code */

    @Then("^the response code should be (\\d*)$")
    public void checkResponseCode(int statusCode) throws Exception {
        CommonStates.getResultActions().andExpect(status().is(statusCode));
    }

    @Then("^the response code should be 2xx$")
    public void check2xxSuccessful() throws Exception {
        CommonStates.getResultActions().andExpect(status().is2xxSuccessful());
    }

    @Then("^the response code should be 4xx$")
    public void check4xxClientError() throws Exception {
        CommonStates.getResultActions().andExpect(status().is4xxClientError());
    }

    /* check http response header */

    @Then("^the (.*) header should be present with value of (.*)$")
    public void checkHeaderPresenceWithValue(String headerName, String valueKey) {
        String headerValue = CommonStates.getUrl(valueKey);
        assertEquals("Header not present with value: " + headerName + "=" + headerValue,
                headerValue,
                CommonStates.getResultActions().andReturn().getResponse().getHeaderValue(headerName));
    }

    @Then("^the (.*) header should be present with actual value (.*)$")
    public void checkHeaderPresenceWithDirectValue(String headerName, String headerValue) {
        assertEquals("Header not present with value: " + headerName + "=" + headerValue,
                headerValue,
                CommonStates.getResultActions().andReturn().getResponse().getHeaderValue(headerName));
    }

    @Then("^the (.*) header should contain (.*)$")
    public void checkHeaderContainsValue(String headerName, String headerValue) {
        String header = CommonStates.getResultActions().andReturn().getResponse().getHeaderValue(headerName).toString();
        assertTrue("Header not present with value: " + headerName + "=" + headerValue,
                header.contains(headerValue));
    }

    /* check http response body */

    @Then("^the result json should be:$")
    public void checkResponseJsonMatch(String jsonString) throws Exception {
        CommonStates.getResultActions().andExpect(content().json(jsonString));
    }

    @Then("^the result should contain (\\d*) (.*)$")
    public void checkResponseListSize(int size, String className) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.."+className).isArray())
                .andExpect(jsonPath("$.."+className+".length()").value(size));
    }

    @Then("^the result should have (.*) with value (.*)$")
    public void checkResponseJsonFieldValue(String field, String value) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(value));
    }

    @Then("^the result should have (.*) as number$")
    public void checkResponseJsonFieldValueNumber(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).isNumber());
    }

    @Then("^the result should have (.*) as null$")
    public void checkResponseJsonFieldValueNull(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(nullValue()));
    }

    @Then("^the result should have (.*) as false$")
    public void checkResponseJsonFieldValueFalse(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(false));
    }

    @Then("^the difference between (.*) and today should be (\\d*) day$")
    public void checkResponseJsonFieldValueDay(String field, int diff) throws Exception {
        LocalDate days = LocalDate.now().plusDays(diff);
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(days.toString()));
    }

    @Then("^the result should have (.*) existing$")
    public void checkResponseJsonFieldValueExist(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).exists());
    }

    @Then("^the result should have (.*) non empty$")
    public void checkResponseJsonFieldValueNotEmpty(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).isNotEmpty());
    }

    @Then("^the result should not contain (.*)$")
    public void checkResponseJsonNoField(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).doesNotExist());
    }

    @And("^the href of the class (.*) should be (.*)$")
    public void checkResponseLinkedObjectHref(String className, String valueKey)
            throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.."+className+".href")
                .value(CommonStates.getUrl(valueKey)));
    }

    @And("^the href of the (.*) of (.*) has items (.*)$")
    public void checkResponseLinkedObjectHref(String field, String className, String urlKeys)
            throws Exception {
        String[] urls = {};
        if (!urlKeys.equals("NONE")) {
            urls = Arrays.stream(urlKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .toArray(String[]::new);
        }

        CommonStates.getResultActions().andExpect(jsonPath("$.."+className+"[*].."+field+".href",
                containsInAnyOrder(urls)));
    }

    @And("^the (.*) field of (.*) (\\d*) should be (.*)$")
    public void checkResponseLinkedObjectFieldValue(String field, String className, int index, String fieldValue)
            throws Exception {
        CommonStates.getResultActions()
                .andExpect(jsonPath("$.."+className+"["+index+"]."+field).value(fieldValue));
    }

    @And("^the (.*) field of (.*) (\\d*) should have item (.*)$")
    public void checkResponseLinkedObjectFieldItem(String field, String className, int index, String fieldValue)
            throws Exception {
        CommonStates.getResultActions()
                .andExpect(jsonPath("$.."+className+"["+index+"]."+field).isArray())
                .andExpect(jsonPath("$.."+className+"["+index+"]."+field+"[*]", hasItems(fieldValue)));
    }

    @When("^user request OPTIONS with header (.*) value (.*)$")
    public void performOptionsWithData(String headerList, String valueList) throws Exception {
        String[] header = headerList.split(",");
        String[] value = valueList.split(",");

        CommonStates.setResultActions(mockMvc.perform(options("/")
                .header(header[0], value[0])
                .header(header[1], value[1])));
    }

    @When("^user request set start time")
    public void setStartTime() {
        CommonStates.setTime("START_TIME", ZonedDateTime.now());
    }

    @When("^user request set end time")
    public void setEndTime() {
        CommonStates.setTime("END_TIME", ZonedDateTime.now());
    }

    @Then("^the lastModifiedDate should be within times$")
    public void checkLastModifiedDate() throws Exception{
        JSONObject jsonObject = new JSONObject(CommonStates.getResultActions().andReturn().getResponse().getContentAsString());
        ZonedDateTime lastModifiedDate = ZonedDateTime.parse(jsonObject.getString("lastModifiedDate"));
        assert lastModifiedDate.isAfter(CommonStates.getTime("START_TIME"));
        assert lastModifiedDate.isBefore(CommonStates.getTime("END_TIME"));
    }

}
