package uk.ac.ebi.ampt2d.metadata.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Ignore
@AutoConfigureMockMvc
public class AnalysisSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @When("user create a test analysis with (.*) for study and (.*) for reference sequence")
    public void createTestAnalysis(String studyUrlKey, String referenceSequenceUrlKey) throws Exception {

        CommonStates.setResultActions(postTestAnalysis("EGAA0001", referenceSequenceUrlKey,
                CommonStates.getUrl(studyUrlKey), Analysis.Technology.EXOME_SEQUENCING,
                Analysis.Type.CASE_CONTROL, "Illumina"));
    }

    @When("user create a test analysis with (.*) for accession, (.*) for reference sequence, (.*) for study, (.*) for technology, (.*) for type and (.*) for platform")
    public void createTestAnalysis2(String accession, String referenceSequenceUrlKey, String studyUrlKey,
                                    Analysis.Technology technology, Analysis.Type type, String platform)
        throws Exception {

        CommonStates.setResultActions(
                postTestAnalysis(accession, referenceSequenceUrlKey, CommonStates.getUrl(studyUrlKey),
                        technology, type, platform)
        );
    }

    private ResultActions postTestAnalysis(String accession, String referenceSequenceUrlKey, String studyUrl,
                                           Analysis.Technology technology, Analysis.Type type, String platform)
            throws Exception {

        List<String> referenceSequenceUrlList = null;
        if (!referenceSequenceUrlKey.equals("NONE")) {
            referenceSequenceUrlList= Arrays.stream(referenceSequenceUrlKey.split(","))
                    .map(key -> CommonStates.getUrl(key))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        String jsonContent = "{ " +
                "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\":  1 }," +
                "\"name\": \"test_human_analysis\"," +
                "\"description\": \"Nothing important\"," +
                "\"study\": \"" + studyUrl + "\",";
        if (referenceSequenceUrlList != null) {
            jsonContent = jsonContent +
                    "\"referenceSequences\": " + objectMapper.writeValueAsString(referenceSequenceUrlList) + ",";
        }
        jsonContent = jsonContent +
                "\"technology\": \"" + technology + "\"," +
                "\"type\": \"" + type + "\"," +
                "\"platform\": \"" + platform + "\"" +
                "}";
        System.out.println(jsonContent);
        return mockMvc.perform(post("/analyses")
                .content(jsonContent));
    }
}
