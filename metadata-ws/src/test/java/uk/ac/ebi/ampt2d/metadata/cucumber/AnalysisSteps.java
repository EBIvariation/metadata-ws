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
import cucumber.api.java.en.When;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;

import java.util.List;

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
        createTestAnalysisParam("EGAA0001", referenceSequenceUrlKey, studyUrlKey, Analysis.Technology.EXOME_SEQUENCING,
                Analysis.Type.CASE_CONTROL, "Illumina");
    }

    @When("user create a test analysis with (.*) for accession, (.*) for reference sequence, (.*) for study, (.*) for technology, (.*) for type and (.*) for platform")
    public void createTestAnalysisParam(String accession, String referenceSequenceUrlKey, String studyUrlKey,
                                        Analysis.Technology technology, Analysis.Type type, String platform)
            throws Exception {

        CommonStates.setResultActions(
                postTestAnalysis(accession, referenceSequenceUrlKey, studyUrlKey,
                        technology, type, platform)
        );
    }

    private ResultActions postTestAnalysis(String accession, String referenceSequenceUrlKey, String studyUrlKey,
                                           Analysis.Technology technology, Analysis.Type type, String platform)
            throws Exception {

        List<String> referenceSequenceUrlList = CommonStates.getUrls(referenceSequenceUrlKey);
        String jsonContent = "{ " +
                "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\":  1 }," +
                "\"name\": \"test_human_analysis\"," +
                "\"description\": \"Nothing important\"," +
                "\"study\": \"" + CommonStates.getUrl(studyUrlKey) + "\",";
        if (referenceSequenceUrlList != null) {
            jsonContent = jsonContent +
                    "\"referenceSequences\": " + objectMapper.writeValueAsString(referenceSequenceUrlList) + ",";
        }
        jsonContent = jsonContent +
                "\"technology\": \"" + technology + "\"," +
                "\"type\": \"" + type + "\"," +
                "\"platform\": \"" + platform + "\"" +
                "}";
        return mockMvc.perform(post("/analyses")
                .content(jsonContent));
    }
}