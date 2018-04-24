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
package uk.ac.ebi.ampt2d.metadata;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AssemblyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MetadataApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private AssemblyRepository assemblyRepository;

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
    public void cleanDatabases() throws Exception {
        analysisRepository.deleteAll();
        studyRepository.deleteAll();
        webResourceRepository.deleteAll();
        sampleRepository.deleteAll();
        fileRepository.deleteAll();
        taxonomyRepository.deleteAll();
        assemblyRepository.deleteAll();
    }

    @Test
    public void postAssembly() throws Exception {
        String location = postTestAssembly();
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GRCh37"));
    }

    private String postTestAssembly() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/assemblies")
                .content("{ " +
                        "\"name\": \"GRCh37\"," +
                        "\"patch\": \"p2\"," +
                        "\"accessions\": [\"GCA_000001405.3\", \"GCF_000001405.14\"]" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postTaxonomy() throws Exception {
        String location = postTestTaxonomy();
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("9606"))
                .andExpect(jsonPath("$.name").value("Homo sapiens"));
    }

    private String postTestTaxonomy() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/taxonomies")
                .content("{ \"id\": 9606, \"name\": \"Homo sapiens\"}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postStudy() throws Exception {
        String location = postTestStudy();

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test human study"));
    }

    private String postTestStudy() throws Exception {
        String taxonomyUrl = postTestTaxonomy();

        MvcResult mvcResult = mockMvc.perform(post("/studies")
                .content("{ " +
                        "\"name\": \"test human study\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postAnalysis() throws Exception {
        String assemblyUrl = postTestAssembly();
        String studyUrl = postTestStudy();

        MvcResult mvcResult = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"name\": \"test human analysis\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + studyUrl + "\"," +
                        "\"assembly\": \"" + assemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"string\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test human analysis"));
    }

    @Test
    public void postFile() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/files")
                .content("{" +
                        "\"hash\": \"asd123\"," +
                        "\"fileName\": \"testName\"," +
                        "\"fileSize\": 100," +
                        "\"type\": \"TSV\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("testName"));
    }

    @Test
    public void postSample() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/samples")
                .content("{" +
                        "\"name\":\"EBI_0000001\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("EBI_0000001"));
    }

    @Test
    public void postWebResource() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/webResources")
                .content("{" +
                        "\"type\":\"CENTER_WEB\"," +
                        "\"resourceUrl\":\"http:\\\\www.ebi.ac.uk\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CENTER_WEB"))
                .andExpect(jsonPath("$.resourceUrl").value("http:\\www.ebi.ac.uk"));
    }

}
