package uk.ac.ebi.ampt2d.metadata.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@AutoConfigureMockMvc
public class StudySteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @When("user create a test study with (.*) for taxonomy$")
    public void createTestStudy(String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy("EGAS0001", 1, "test_human_study", false, LocalDate.now(), testTaxonomyKey));
    }

    @When("user create a test parameterized study with (.*) for accession, (.*) for version, (.*) for name, (.*) for deprecated, (.*) for releaseDay and (.*) for taxonomy$")
    public void createTestStudyParameterizedMore(String accession, int version, String name, boolean deprecated, int releaseDay, String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy(accession, version, name, deprecated, LocalDate.now().plusDays(releaseDay), testTaxonomyKey));
    }

    @When("^user request GET for the (.*)")
    public void performGetOnResources(String className) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/" + className)));
    }

    @When("^user request GET for (.*) with query param (.*)")
    public void performGetOnResourcesQuery(String className, String param) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/" + className + "?" + param)));
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

    @When("^user request elaborate search for the (.*) base (.*) and with the parameters: (.*)$")
    public void performSearchOnResourcesWithBaseAndParameters(String className, String base, String parameters) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters)));
    }

    @When("^user request elaborate search with day for the (.*) base (.*) and with the parameters: (.*) and (\\d*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDay(String className, String base, String parameters, int day) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+parameters+LocalDate.now().plusDays(day))));
    }

    @When("^user request elaborate search with date range for the (.*) base (.*) and with the parameters: (\\d*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDayRange(String className, String base, int day) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base+"?"+"from="+LocalDate.now().plusDays(day)+"&to="+LocalDate.now().plusDays(day))));
    }

    @When("^user request search for the (.*) with base (.*) and name (.*) value (.*)$")
    public void performSearchOnResourcesWithParameters(String className, String base, String name, String value) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/"+className+"/search/"+base).param(name, value)));
    }

    @When("^user request search for the (.*) with param (.*)")
    public void performSearchOnResources(String className, String param) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/" + className+"/search/"+param)));
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
