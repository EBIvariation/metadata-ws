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

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.ampt2d.metadata.security.AuthorizationServerHelper;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

public class MiscSteps {

    @Autowired
    private MockMvc mockMvc;

    @When("^I request OPTIONS / with GET for Access-Control-Request-Method header and http://www.evil-url.com for Origin header$")
    public void performOptionsWithData() throws Exception {
        CommonStates.setResultActions(mockMvc.perform(options("/")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://www.evil-url.com")));
    }

    @Then("^the (.*) header should contain (.*)$")
    public void checkHeaderContainsValue(String headerName, String headerValue) {
        String header = CommonStates.getResultActions().andReturn().getResponse().getHeaderValue(headerName).toString();
        assertTrue("Header not present with value: " + headerName + "=" + headerValue,
                header.contains(headerValue));
    }

}
