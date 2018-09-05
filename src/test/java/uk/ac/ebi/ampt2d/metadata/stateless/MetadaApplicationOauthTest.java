/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata.stateless;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AssemblyRepository;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureJsonTesters
@AutoConfigureMockMvc
public class MetadaApplicationOauthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssemblyRepository assemblyRepository;

    @Autowired
    private JacksonTester<Assembly> testAssemblyJson;

    @Autowired
    private OAuthHelper oAuthHelper;

    @Before
    public void cleanDatabases() throws Exception {
        assemblyRepository.deleteAll();
    }

    @Test
    public void postAssembly() throws Exception {
        String location = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));

        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GRCh37"));
    }

    private String postTestAssembly(String name, String patch, List<String> accessions) throws Exception {
        Assembly testAssembly = new Assembly(name, patch, accessions);

        MvcResult mvcResult = mockMvc.perform(post("/assemblies").with(oAuthHelper.bearerToken("test1"))
                .content(testAssemblyJson.write(testAssembly).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void testPostAssemblyWithoutBearerToken() throws Exception {
        String location = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));

        mockMvc.perform(get(location))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPostAssemblyWithoutProperAuthority() throws Exception {
        Assembly testAssembly = new Assembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));

        mockMvc.perform(post("/assemblies").with(oAuthHelper.bearerToken("test"))
                .content(testAssemblyJson.write(testAssembly).getJson()))
                .andExpect(status().isForbidden());

    }
}
