package uk.ac.ebi.ampt2d.metadata.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @When("^user request POST with (.*) for Uri (.*) for stringData (.*) for linkedObjectKey and (.*) for linkedObjectClassName")
    public void performPostOnResourceUriWithStringDataAndLink(String urlKey, String stringData, String linkedObjectUrlKeys, String linkedObjectClassName) throws Exception {
        List<String> newUrls = null;
        if (linkedObjectUrlKeys.isEmpty()) {
            newUrls = new ArrayList<>();
        } else if (!linkedObjectUrlKeys.equals("NONE")) {
            newUrls = Arrays.stream(linkedObjectUrlKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .collect(Collectors.toList());
        }

        String jsonContent = "{"
                + stringData
                + ", "
                + "\"" + linkedObjectClassName + "\":" + objectMapper.writeValueAsString(newUrls)
                + "}";

        CommonStates.setResultActions(mockMvc.perform(post(urlKey)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent.getBytes())));
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

    @When("^user request elaborate search for the (.*) base (.*) and with the parameters: (.*)$")
    public void performSearchOnResourcesWithBaseAndParameters(String className, String base, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters)));
    }

    @When("^user request exhaustive search for the (.*) base (.*) and with the parameters: (.*) and (.*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDay(String className, String base, String parameters, int day) throws Exception {
        if (day > 0) {
            CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters+LocalDate.now().plusDays(day))));
        } else {
            CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters+LocalDate.now().minusDays(Math.abs(day)))));
        }
    }

    @When("^user request exhaustive search with dates for the (.*) base (.*) and with the parameters: (.*) and (.*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDays(String className, String base, String parameters, int day) throws Exception {
        if (day > 0) {
            CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters+LocalDate.now().plusDays(day)+"&to="+parameters+LocalDate.now().plusDays(day))));
        } else {
            CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters+LocalDate.now().minusDays(Math.abs(day))+"&to="+LocalDate.now().minusDays(Math.abs(day)))));
        }
    }

    @When("^user request search for the (.*) with base (.*) and name (.*) value (.*)$")
    public void performSearchOnResourcesWithParameters(String className, String base, String name, String value) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base).param(name, value)));
    }

    @When("^user request elaborate find for the (.*) bases (.*) and with the parameters: (.*)$")
    public void performFindOnResourcesWithBaseAndParameters(String className, String bases, String parameters) throws Exception {
        String[] params = parameters.split("&");
        String[] base = bases.split(",");
        String query = base[0] + "." + params[0];
        for (int i = 1; i < params.length; i++) {
            query += "&" + base[i] + "." + params[i];
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

    @Then("^the result should contain object (.*) with items (.*)$")
    public void checkResponseVaryingListSize(String className, int size) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.."+className).isArray())
                .andExpect(jsonPath("$.."+className+".length()").value(size));
    }

    @Then("^the result should contain (.*) with value (.*)$")
    public void checkResponseJsonFieldValue(String field, String value) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(value));
    }

    @And("^the href of the (.*) of (.*) (\\d*) should be (.*)$")
    public void checkResponseLinkedObjectHref(String field, String className, int index, String valueKey)
            throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.."+className+"["+index+"].."+field+".href")
                .value(CommonStates.getUrl(valueKey)));
    }

    @And("^the href of the class (.*) should be (.*)$")
    public void checkResponseLinkedObjectHref(String className, String valueKey)
            throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.."+className+".href")
                .value(CommonStates.getUrl(valueKey)));
    }

    @And("^the href list of the (.*) of (.*) (\\d*) should be (.*)$")
    public void checkResponseLinkedObjectHrefList(String field, String className, int index, String valueKey)
            throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.."+className+"["+index+"].."+field+".href")
                .value(CommonStates.getUrl(valueKey)));
    }

    @And("^the href list of the (.*) of (.*) (.*) contained in (.*)$")
    public void checkResponseLinkedObjectHrefListLoop(String field, String className, int lenght, String valueKey)
            throws Exception {
        String[] urlList = valueKey.split(",");
        for (int index = 0; index < lenght; index++) {
            CommonStates.getResultActions().andExpect(jsonPath("$.."+className+"["+index+"].."+field+".href")
                    .value(CommonStates.getUrl(urlList[index])));
        }
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
}
