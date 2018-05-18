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
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AssemblyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureJsonTesters
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

    @Autowired
    private JacksonTester<Assembly> testAssemblyJson;

    @Autowired
    private JacksonTester<File> testFileJson;

    @Autowired
    private JacksonTester<Sample> testSampleJson;

    @Autowired
    private JacksonTester<Taxonomy> testTaxonomyJson;

    @Autowired
    private JacksonTester<WebResource> testWebResourceJson;

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
        String location = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GRCh37"));
    }

    private String postTestAssembly(String name, String patch, List<String> accessions) throws Exception {
        Assembly testAssembly = new Assembly(name, patch, accessions);

        MvcResult mvcResult = mockMvc.perform(post("/assemblies")
                .content(testAssemblyJson.write(testAssembly).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postTaxonomy() throws Exception {
        String location = postTestTaxonomy();
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9606))
                .andExpect(jsonPath("$.name").value("Homo sapiens"));
    }

    private String postTestTaxonomy() throws Exception {
        Taxonomy testTaxonomy = new Taxonomy(9606, "Homo sapiens");

        MvcResult mvcResult = mockMvc.perform(post("/taxonomies")
                .content(testTaxonomyJson.write(testTaxonomy).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postStudy() throws Exception {
        String location = postTestStudy("test human study");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test human study"));
    }

    private String postTestStudy(String name) throws Exception {
        String taxonomyUrl = postTestTaxonomy();

        MvcResult mvcResult = mockMvc.perform(post("/studies")
                .content("{ " +
                        "\"name\": \"" + name + "\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postAnalysis() throws Exception {
        String assemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String studyUrl = postTestStudy("test human study");

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
        File testFile = new File("asd123", "testName", 100, File.Type.TSV);
        MvcResult mvcResult = mockMvc.perform(post("/files")
                .content(testFileJson.write(testFile).getJson()))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("testName"));
    }

    @Test
    public void postSample() throws Exception {
        Sample testSample = new Sample("EBI_0000001");
        MvcResult mvcResult = mockMvc.perform(post("/samples")
                .content(testSampleJson.write(testSample).getJson()))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("EBI_0000001"));
    }

    @Test
    public void postWebResource() throws Exception {
        WebResource testWebResource = new WebResource(WebResource.Type.CENTER_WEB, "http:\\www.ebi.ac.uk");

        MvcResult mvcResult = mockMvc.perform(post("/webResources")
                .content(testWebResourceJson.write(testWebResource).getJson()))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CENTER_WEB"))
                .andExpect(jsonPath("$.resourceUrl").value("http:\\www.ebi.ac.uk"));
    }

    @Test
    public void findAssemblyByName() throws Exception {
        String grch37Url = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/assemblies/search/findByName?name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"));

        mockMvc.perform(get("/assemblies/search/findByName?name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh38"));

        mockMvc.perform(get("/assemblies/search/findByName?name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

    }

    @Test
    public void findAssemblyByNameAndPatch() throws Exception {
        String grch37Url = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/assemblies/search/findByNameAndPatch?name=GRCh37&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"));

        mockMvc.perform(get("/assemblies/search/findByNameAndPatch?name=GRCh38&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh38"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"));

        mockMvc.perform(get("/assemblies/search/findByNameAndPatch?name=NCBI36&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search/findByNameAndPatch?name=GRCh37&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search/findByNameAndPatch?name=GRCh38&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));
    }

    @Test
    public void findAssemblyByAccessions() throws Exception {
        String grch37Url = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/assemblies/search/findByAccessions?accession=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search/findByAccessions?accession=GCF_000001405.28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCF_000001405.28")));

        mockMvc.perform(get("/assemblies/search/findByAccessions?accession=GCA_000001405.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));
    }

    @Test
    public void findStudyByAssemblyName() throws Exception {
        String grch37AssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38AssemblyUrl = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));
        String grch37StudyUrl = postTestStudy("test human study based on GRCh37");
        String grch38StudyUrl = postTestStudy("test human study based on GRCh38");

        mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"name\": \"test human analysis based on GRCh37\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + grch37StudyUrl + "\"," +
                        "\"assembly\": \"" + grch37AssemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"string\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"name\": \"test human analysis based on GRCh38\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + grch38StudyUrl + "\"," +
                        "\"assembly\": \"" + grch38AssemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"string\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        mockMvc.perform(get("/studies/search/findByAssemblyName?name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies/search/findByAssemblyName?name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies/search/findByAssemblyName?name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void findStudyByAssemblyNameAndPatch() throws Exception {
        String grch37AssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38AssemblyUrl = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));
        String grch37StudyUrl = postTestStudy("test human study based on GRCh37");
        String grch38StudyUrl = postTestStudy("test human study based on GRCh38");

        mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"name\": \"test human analysis based on GRCh37\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + grch37StudyUrl + "\"," +
                        "\"assembly\": \"" + grch37AssemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"string\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"name\": \"test human analysis based on GRCh38\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + grch38StudyUrl + "\"," +
                        "\"assembly\": \"" + grch38AssemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"string\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        mockMvc.perform(get("/studies/search/findByAssemblyNameAndPatch?name=GRCh37&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies/search/findByAssemblyNameAndPatch?name=GRCh38&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies/search/findByAssemblyNameAndPatch?name=NCBI36&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies/search/findByAssemblyNameAndPatch?name=GRCh37&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }


    @Test
    public void searchStudy() throws Exception {
        String grch37StudyUrl = postTestStudy("test human study based on GRCh37");
        String grch38StudyUrl = postTestStudy("test human study based on GRCh38");

        mockMvc.perform(get("/studies/search/").param("searchString","human"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/studies/search/").param("searchString","important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/studies/search/").param("searchString","GrCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*").isArray())
                .andExpect(jsonPath("$.length()").value(1));

    }

}
