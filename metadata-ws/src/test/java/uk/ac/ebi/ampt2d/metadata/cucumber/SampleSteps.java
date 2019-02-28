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

    @When("user create a test sample with (.*) for taxonomy$")
    public void createTestSample(String testTaxonomyKeys) throws Exception {
        List<String> testTaxonomyList = null;
        if (!testTaxonomyKeys.equals("NONE")) {
            testTaxonomyList = Arrays.stream(testTaxonomyKeys.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        CommonStates.setResultActions(postTestSample(testTaxonomyList));
    }

    private ResultActions postTestSample(List<String> testTaxonomyList) throws Exception {
        String jsonContent = "{ " +
                "\"accessionVersionId\":{ \"accession\": \"EGAS0001\",\"version\": 1}," +
                "\"name\": \"test_human_sample\"";
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
