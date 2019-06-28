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
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PublicationRepository;
import uk.ac.ebi.ampt2d.metadata.security.AuthorizationServerHelper;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CommonSteps {

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

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private AuthorizationServerHelper authorizationServerHelper;

    @Before
    public void cleanDatabases() {
        analysisRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        fileRepository.deleteAll();
        sampleRepository.deleteAll();
        studyRepository.deleteAll();
        taxonomyRepository.deleteAll();
        webResourceRepository.deleteAll();
        publicationRepository.deleteAll();
    }

    @Before
    public void cleanStates() {
        CommonStates.clear();
    }

    /* perform http request */

    @When("^I request GET ([\\S]*)$")
    public void performGetOnResourceUri(String resourceUri) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(resourceUri)
                .with(authorizationServerHelper.bearerToken("testuser")))
        );
    }

    @When("^I request ananymous GET ([\\S]*)$")
    public void performGetOnResourceUriNonSecure(String resourceUri) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(resourceUri)));
    }

    @When("^I request GET with value of (.*)$")
    public void performGetWithResourceUriKey(String resourceUriKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(CommonStates.getUrl(resourceUriKey))
                .with(authorizationServerHelper.bearerToken("testuser"))));
    }

    @When("^I request GET for (.*) of (.*)$")
    public void performGetForLinkedObjects(String className, String resourceUriKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get(CommonStates.getUrl(resourceUriKey) + "/" + className)
                .with(authorizationServerHelper.bearerToken("testuser"))));
    }

    @When("^I request POST (.*) with JSON payload:$")
    public void performPostOnResourceUriWithJsonData(String resourceUri, String jsonData) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(post(resourceUri)
                .with(authorizationServerHelper.bearerToken("testoperator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData.getBytes())));
    }

    @When("^I request authority set POST (.*) with JSON payload:$")
    public void performPostOnResourceUriWithJsonDataLowPrivileges(String resourceUri, String jsonData) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(post(resourceUri)
                .with(CommonStates.getRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData.getBytes())));
    }

    @When("^I request PUT (.*) with JSON payload:$")
    public void performPutOnResourceUriWithJsonData(String resourceUrlKey, String jsonData) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(put(CommonStates.getUrl(resourceUrlKey))
                .with(authorizationServerHelper.bearerToken("testoperator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData.getBytes())));
    }

    @When("^I request authority set PUT with value of (.*) having JSON payload:$")
    public void performPutOnResourceUriWithJsonDataUnauthorized(String resourceUrlKey, String jsonData) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(put(CommonStates.getUrl(resourceUrlKey))
                .with(CommonStates.getRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData.getBytes())));
    }

    @When("^I request PATCH (.*) with list (.*) of (.*)")
    public void performPatchOnResourceWithLinkedObjectList(String urlKey, String linkedObjectUrlKeys,
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
                .with(authorizationServerHelper.bearerToken("testoperator"))
                .content(jsonContent)));
    }

    @When("^I request PATCH (.*) with taxonomy (.*)")
    public void performPatchOnResourceWithLinkedObject(String urlKey, String linkedObjectUrlKey) throws Exception {
        String jsonContent = "{"
                + "\"taxonomy\": \"" + CommonStates.getUrl(linkedObjectUrlKey)
                + "\"}";
        CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey))
                .with(authorizationServerHelper.bearerToken("testoperator"))
                .content(jsonContent)));
    }

    @When("^I request PATCH (.*) with content (.*)")
    public void performPatchOnResourceWithContent(String urlKey, String content) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey))
                .with(authorizationServerHelper.bearerToken("testoperator"))
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
                .with(authorizationServerHelper.bearerToken("testoperator"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)));
    }

    @When("^I request DELETE for the (.*) of (.*) of the (.*)")
    public void performDeleteOnResourceWithLinkedObject(String className, String linkedObjectUrlKey,
                                                        String resourceUrlKey) throws Exception {
        String linkedObjectUrl = CommonStates.getUrl(linkedObjectUrlKey);
        String resourceUrl = CommonStates.getUrl(resourceUrlKey);
        if (linkedObjectUrl == null || resourceUrl == null) {
            return;
        }

        String idStr = linkedObjectUrl.substring(linkedObjectUrl.lastIndexOf('/') + 1);
        CommonStates.setResultActions(mockMvc.perform(delete(resourceUrl + "/" + className + "/" + idStr)
                .with(authorizationServerHelper.bearerToken("testoperator"))));
    }

    @When("^I request DELETE with value of (.*)$")
    public void performDeleteOnResourceUri(String resourceUrlKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(delete(CommonStates.getUrl(resourceUrlKey))
                .with(authorizationServerHelper.bearerToken("testoperator"))));
    }

    @When("^I request no authority DELETE with value of (.*)$")
    public void performDeleteOnResourceUriNonSecure(String resourceUrlKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(delete(CommonStates.getUrl(resourceUrlKey))));
    }

    @When("^I request authority set DELETE with value of (.*)$")
    public void performDeleteOnResourceUriSecureLowPrivileges(String resourceUrlKey) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(delete(CommonStates.getUrl(resourceUrlKey))
                .with(CommonStates.getRequestPostProcessor())));
    }

    @When("^I request search for the (.*) with the parameters: (.*)$")
    public void performSearchOnResourcesWithParameters(String className, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/" + className + "/search?" + parameters)
                .with(authorizationServerHelper.bearerToken("testuser"))));
    }

    @When("^I request elaborate find for the (.*) with the parameters: (.*)$")
    public void performFindOnResourcesWithParameters(String className, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/" + className + "?" + parameters)
                .with(authorizationServerHelper.bearerToken("testuser"))));
    }

    @When("^I request elaborate search for the (.*) base (.*) and with the parameters: (.*)$")
    public void performSearchOnResourcesWithBaseAndParameters(String className, String base, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/" + className + "/search/" + base + "?" + parameters)
                .with(authorizationServerHelper.bearerToken("testuser"))));
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

    /* check http response body */

    @Then("^the response JSON should be:$")
    public void checkResponseJsonMatch(String jsonString) throws Exception {
        CommonStates.getResultActions().andExpect(content().json(jsonString));
    }

    @Then("^the response should contain (\\d*) (.*)$")
    public void checkResponseListSize(int size, String className) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.." + className).isArray())
                .andExpect(jsonPath("$.." + className + ".length()").value(size));
    }

    @Then("^the response should contain no study$")
    public void checkStudyResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Then("^the response should contain one file$")
    public void checkFileResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..files").isArray())
                .andExpect(jsonPath("$..files.length()").value(1));
    }

    @Then("^the response should contain one sample$")
    public void checkSampleResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(1));
    }

    @Then("^the response should contain one taxonomy$")
    public void checkTaxonomyResponseSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(1));
    }

    @Then("^the response should contain field (.*) with value (.*)$")
    public void checkResponseJsonFieldValue(String field, String value) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$." + field).value(value));
    }

    @Then("^the response should contain error message (.*)$")
    public void checkResponseJsonMessage(String message) throws Exception {
        CommonStates.getResultActions().andReturn().getResponse().getContentAsString().contains(message);
    }

    @Then("^the response should contain field (.*) with a numeric value$")
    public void checkResponseJsonFieldValueNumber(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$." + field).isNumber());
    }

    @Then("^the response should contain field (.*) with null value$")
    public void checkResponseJsonFieldValueNull(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$." + field).value(nullValue()));
    }

    @Then("^the difference between (.*) and today should be (\\d*) day$")
    public void checkResponseJsonFieldValueDay(String field, int diff) throws Exception {
        LocalDate days = LocalDate.now().plusDays(diff);
        CommonStates.getResultActions().andExpect(jsonPath("$." + field).value(days.toString()));
    }

    @Then("^the response should contain field (.*) with a non-empty value$")
    public void checkResponseJsonFieldValueNotEmpty(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$." + field).isNotEmpty());
    }

    @And("^the href of the class (.*) should be (.*)$")
    public void checkResponseLinkedObjectHref(String className, String valueKey)
            throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.." + className + ".href")
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

        CommonStates.getResultActions().andExpect(jsonPath("$.." + className + "[*].." + field + ".href",
                containsInAnyOrder(urls)));
    }

    @And("^the (.*) field of (.*) (\\d*) should be (.*)$")
    public void checkResponseLinkedObjectFieldValue(String field, String className, int index, String fieldValue)
            throws Exception {
        CommonStates.getResultActions()
                .andExpect(jsonPath("$.." + className + "[" + index + "]." + field).value(fieldValue));
    }

    @And("^the (.*) field of (.*) (\\d*) should have item (.*)$")
    public void checkResponseLinkedObjectFieldItem(String field, String className, int index, String fieldValue)
            throws Exception {
        CommonStates.getResultActions()
                .andExpect(jsonPath("$.." + className + "[" + index + "]." + field).isArray())
                .andExpect(jsonPath("$.." + className + "[" + index + "]." + field + "[*]", hasItems(fieldValue)));
    }

    @Given("^current date as (.*)$")
    public void setTime(String timeKey) {
        CommonStates.setTime(timeKey, LocalDate.now());
    }

    @Then("^the (.*) should equal (.*)$")
    public void checkDateTimeBetween(String date, String today) throws Exception {
        JSONObject jsonObject = new JSONObject(CommonStates.getResultActions().andReturn().getResponse().getContentAsString());
        LocalDate lastModifiedDate = LocalDate.parse(jsonObject.getString(date));
        assert lastModifiedDate.isEqual(CommonStates.getTime(today));
    }

    @Given("^there is an URL (.*) with key (.*)$")
    public void setUrlWithKey(String url, String key) {
        CommonStates.setUrl(key, url);
    }

    @When("^I set authorization with testuser having default role$")
    public void setAuthorizationWithDefaultRole() {
        CommonStates.setRequestPostProcessor(authorizationServerHelper.bearerToken("testuser"));
    }

    @When("^I set authorization with testoperator having SERVICE_OPERATOR role$")
    public void setAuthorizationWithServiceOperatorRole() {
        CommonStates.setRequestPostProcessor(authorizationServerHelper.bearerToken("testoperator"));
    }

}
