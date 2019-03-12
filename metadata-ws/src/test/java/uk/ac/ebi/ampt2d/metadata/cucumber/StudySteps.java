package uk.ac.ebi.ampt2d.metadata.cucumber;

import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@AutoConfigureMockMvc
public class StudySteps {

    @Autowired
    private MockMvc mockMvc;

    @When("user create a test study with (.*) for taxonomy$")
    public void createTestStudy(String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy("EGAS0001", 1, "test_human_study", LocalDate.now(), testTaxonomyKey));
    }

    @When("user create a test parameterized study with (.*) for accession, (.*) for version, (.*) for name and (.*) for taxonomy$")
    public void createTestStudyParameterized(String accession, int version, String name, String testTaxonomyKey) throws Exception {
        CommonStates.setResultActions(postTestStudy(accession, version, name, LocalDate.now(), testTaxonomyKey));
    }

    @When("user create a test parameterized study with (.*) for accession, (.*) for version, (.*) for name (.*) for releaseDay and (.*) for taxonomy$")
    public void createTestStudyParameterized(String accession, int version, String name, int releaseDay, String testTaxonomyKey) throws Exception {
        if (releaseDay > 0) {
            CommonStates.setResultActions(postTestStudy(accession, version, name, LocalDate.now().plusDays(releaseDay), testTaxonomyKey));
        } else {
            CommonStates.setResultActions(postTestStudy(accession, version, name, LocalDate.now().minusDays(Math.abs(releaseDay)), testTaxonomyKey));
        }
    }

    private ResultActions postTestStudy(String accession, int version, String name, LocalDate releaseDate, String testTaxonomyKey) throws Exception {
        String jsonContent = "{" +
                "      \"accessionVersionId\": {" +
                "       \"accession\": \"" + accession +  "\"," +
                "        \"version\": " + version +
                "        }," +
                "      \"name\": \"" + name + "\"," +
                "      \"description\": \"Nothing important\"," +
                "      \"center\": \"EBI\"," +
                "      \"deprecated\": false," +
                "      \"releaseDate\": \"" + releaseDate + "\"," +
                "      \"taxonomy\": \"" + CommonStates.getUrl(testTaxonomyKey) + "\"" +
                "    }";

        return mockMvc.perform(post("/studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated());
    }
}
