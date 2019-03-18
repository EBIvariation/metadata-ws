package uk.ac.ebi.ampt2d.metadata.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Ignore
@AutoConfigureMockMvc
public class SampleSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @When("user create a test sample with (.*) for taxonomy")
    public void createTestSample(String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = null;
        if (!testTaxonomyKeys.equals("NONE")) {
            testTaxonomyList = Arrays.stream(testTaxonomyKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        CommonStates.setResultActions(postTestSample("EGAS0001", "test_human_sample", testTaxonomyList, 1));
    }

    @When("user create a test parameterized sample with (.*) for accession, (.*) for version, (.*) for name and (.*) for taxonomy")
    public void createTestSampleParameterized(String accession, int version, String name, String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = null;
        if (!testTaxonomyKeys.equals("NONE")) {
            testTaxonomyList = Arrays.stream(testTaxonomyKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        CommonStates.setResultActions(postTestSample(accession, name, testTaxonomyList, version));
    }

    @When("user create a test sample no or null accession (.*), (.*) for name and (.*) for taxonomy")
    public void createTestSampleParameterizedNoAccession(boolean accession, String name, String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = null;
        if (!testTaxonomyKeys.equals("NONE")) {
            testTaxonomyList = Arrays.stream(testTaxonomyKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        if (accession == false) {
            // no accession
            CommonStates.setResultActions(postTestSampleNoOrNullAccession(false, name, testTaxonomyList));
        } else {
            // null accession
            CommonStates.setResultActions(postTestSampleNoOrNullAccession(true, name, testTaxonomyList));
        }
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
