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
        CommonStates.setResultActions(postTestSample("EGAS0001", "test_human_sample", testTaxonomyList));
    }

    @When("user create a test parameterized sample with (.*) for accession, (.*) for name and (.*) for taxonomy")
    public void createTestSampleParameterized(String accession, String name, String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = null;
        if (!testTaxonomyKeys.equals("NONE")) {
            testTaxonomyList = Arrays.stream(testTaxonomyKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        CommonStates.setResultActions(postTestSample(accession, name, testTaxonomyList));
    }

    private ResultActions postTestSample(String accession, String name, List<String> testTaxonomyList) throws Exception {
        String jsonContent = "{ " +
                "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\": " + 1 + "}," +
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
}
