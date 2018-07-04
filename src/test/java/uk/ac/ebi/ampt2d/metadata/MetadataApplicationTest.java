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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AssemblyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private JacksonTester<List> testListJson;

    @Autowired
    private JacksonTester<Sample> testSampleJson;

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
        return postTestTaxonomy(9606, "Homo sapiens");
    }

    private String postTestTaxonomy(long id, String name) throws Exception {
        return postTestTaxonomy(id, name, new ArrayList<>());
    }

    private String postTestTaxonomy(long id, String name, List<String> ancestors) throws Exception {
        String jsonContent = "{ " +
                "\"id\": " + Long.toString(id) + "," +
                "\"name\": \"" + name + "\"," +
                "\"ancestors\": " + testListJson.write(ancestors).getJson() + "" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/taxonomies")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postStudy() throws Exception {
        String location = postTestStudy("EGAS0001", 1, "test_human_study");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAS0001"));
    }

    private String postTestStudy(String accession, int version, String name) throws Exception {
        String taxonomyUrl = postTestTaxonomy();

        return postTestStudy(accession, version, name, taxonomyUrl);
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl) throws Exception {
        return postTestStudy(accession, version, name, taxonomyUrl, false);
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, boolean deprecated) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/studies")
                .content("{ " +
                        "\"id\":{ \"accession\": \"" + accession + "\",\"version\": " + version + "}," +
                        "\"name\": \"" + name + "\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"deprecated\": \"" + deprecated + "\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postAnalysis() throws Exception {
        String assemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String studyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        MvcResult mvcResult = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"id\":{ \"accession\": \"EGAA0001\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis\"," +
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
                .andExpect(jsonPath("$.id.accession").value("EGAA0001"));
    }

    @Test
    public void postFile() throws Exception {
        File testFile = new File(new AccessionVersionEntityId("EGAF0001", 1), "asd123", "test_file", 100,
                File.Type.TSV);
        MvcResult mvcResult = mockMvc.perform(post("/files")
                .content(testFileJson.write(testFile).getJson()))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAF0001"))
                .andExpect(jsonPath("$.id.version").value(1));
    }

    @Test
    public void postSample() throws Exception {
        Sample testSample = new Sample(new AccessionVersionEntityId("EGAN0001", 1), "testSample");
        MvcResult mvcResult = mockMvc.perform(post("/samples")
                .content(testSampleJson.write(testSample).getJson()))
                .andExpect(status().isCreated()).andReturn();

        String location = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"));
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

        mockMvc.perform(get("/assemblies/search?name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"));

        mockMvc.perform(get("/assemblies/search?name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh38"));

        mockMvc.perform(get("/assemblies/search?name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"));

        mockMvc.perform(get("/assemblies/search?name=GRCh38&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh38"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"));

        mockMvc.perform(get("/assemblies/search?name=NCBI36&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh38&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search?accessions=GCF_000001405.28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCF_000001405.28")));

        mockMvc.perform(get("/assemblies/search?accessions=GCA_000001405.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p2&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"))
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p3&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));
    }

    @Test
    public void findAnalyses() throws Exception {
        String humanAssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String humanStudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        String testAnalysisOneUrl = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"id\":{ \"accession\": \"EGAA0001\",\"version\":  1 }," +
                        "\"name\": \"test_analysis_one\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + humanStudyUrl + "\"," +
                        "\"assembly\": \"" + humanAssemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"Illumina\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn()
                .getResponse().getHeader("Location");

        String testAnalysisTwoUrl = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"id\":{ \"accession\": \"EGAA0002\",\"version\":  1 }," +
                        "\"name\": \"test_analysis_two\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + humanStudyUrl + "\"," +
                        "\"assembly\": \"" + humanAssemblyUrl + "\"," +
                        "\"technology\": \"ARRAY\"," +
                        "\"type\": \"TUMOR\"," +
                        "\"platform\": \"PacBio\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn()
                .getResponse().getHeader("Location");

        mockMvc.perform(get("/analyses/search?type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisOneUrl));

        mockMvc.perform(get("/analyses/search?type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisTwoUrl));

        mockMvc.perform(get("/analyses/search?type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/analyses/search?platform=PacBio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisTwoUrl));

        mockMvc.perform(get("/analyses/search?platform=illumina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisOneUrl));

        mockMvc.perform(get("/analyses/search?platform=nextSeq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/analyses/search?platform=pacbio&type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1));
    }

    @Test
    public void clientErrorWhenSearchAnalysesWithInvalidType() throws Exception {
        mockMvc.perform(get("/analyses/search?type=unknown"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void findStudies() throws Exception {
        String grch37AssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38AssemblyUrl = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));
        String grch37StudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");
        String grch38StudyUrl = postTestStudy("EGAS0001", 2, "test_human_study");

        mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"id\":{ \"accession\": \"EGAA0001\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis_based_on_GRCh37\"," +
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
                        "\"id\":{ \"accession\": \"EGAA0002\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis_based_on_GRCh38\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + grch38StudyUrl + "\"," +
                        "\"assembly\": \"" + grch38AssemblyUrl + "\"," +
                        "\"technology\": \"GWAS\"," +
                        "\"type\": \"CASE_CONTROL\"," +
                        "\"platform\": \"string\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37&analyses.assembly.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.assembly.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36&analyses.assembly.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37&analyses.assembly.patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void searchStudy() throws Exception {
        postTestStudy("EGAS0001", 1, "test human study based on GRCh37");
        postTestStudy("EGAS0002", 1, "test human study based on GRCh38");

        mockMvc.perform(get("/studies/search/text").param("searchTerm", "human"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(2));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(2));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "grCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0].id.accession").value("EGAS0001"));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "GrCh39"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void searchStudyByAccession() throws Exception {
        postTestStudy("EGAS0001", 1, "test human study based on GRCh37");
        postTestStudy("EGAS0001", 2, "test human study based on GRCh38");
        postTestStudy("EGAS0002", 3, "test human study based on GRCh38");

        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0].id.accession").value("EGAS0001"))
                .andExpect(jsonPath("$..studies[0].id.version").value(2));
        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0].id.accession").value("EGAS0002"))
                .andExpect(jsonPath("$..studies[0].id.version").value(3));
        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void testAccesionValidation() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        mockMvc.perform(post("/studies")
                .content("{ " +
                        "\"id\":{ \"accession\": \"EGAS0001\",\"version\":  0 }," +
                        "\"name\": \" study1\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("id.version"))
                .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 1"));
        mockMvc.perform(post("/studies")
                .content("{ " +
                        "\"id\":{ \"version\":  1 }," +
                        "\"name\": \" study1\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("id.accession"))
                .andExpect(jsonPath("$.errors[0].message").value("may not be null"));
        postTestStudy("EGAS0001", 1, "test_study");
        mockMvc.perform(get("/studies/EGAS0001")).andExpect(status().is4xxClientError()).andExpect(jsonPath("$" +
                ".message").value("Please provide an ID in the form accession.version"));
        mockMvc.perform(get("/studies/EGAS0001.S1")).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Please provide an ID in the form accession.version"));
        mockMvc.perform(get("/studies/EGAS0001.1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAS0001"));
        mockMvc.perform(get("/studies/EGAS0001.2")).andExpect(status().isNotFound());
    }

    @Test
    public void findStudyByTaxonomyId() throws Exception {
        String homininesTaxonomyUrl = postTestTaxonomy(207598, "Homininae");
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens",
                Arrays.asList(homininesTaxonomyUrl));
        String panTaxonomyUrl = postTestTaxonomy(9596, "Pan",
                Arrays.asList(homininesTaxonomyUrl));
        String bonoboTaxonomyUrl = postTestTaxonomy(9597, "Pan paniscus",
                Arrays.asList(homininesTaxonomyUrl, panTaxonomyUrl));
        String chimpanzeeTaxonomyUrl = postTestTaxonomy(9598, "Pan troglodytes",
                Arrays.asList(homininesTaxonomyUrl, panTaxonomyUrl));

        String humanStudyUrl = postTestStudy("testhuman", 1, "test human study", humanTaxonomyUrl);
        String bonoboStudyUrl = postTestStudy("testbonobo", 1, "test bonobo study", bonoboTaxonomyUrl);
        String chimpanzeeStudyUrl = postTestStudy("testchimpanzee", 1, "test chimpanzee study", chimpanzeeTaxonomyUrl);

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9596"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=207598"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(3))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[2]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void findStudyByTaxonomyName() throws Exception {
        String homininesTaxonomyUrl = postTestTaxonomy(207598, "Homininae");
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens",
                Arrays.asList(homininesTaxonomyUrl));
        String panTaxonomyUrl = postTestTaxonomy(9596, "Pan",
                Arrays.asList(homininesTaxonomyUrl));
        String bonoboTaxonomyUrl = postTestTaxonomy(9597, "Pan paniscus",
                Arrays.asList(homininesTaxonomyUrl, panTaxonomyUrl));
        String chimpanzeeTaxonomyUrl = postTestTaxonomy(9598, "Pan troglodytes",
                Arrays.asList(homininesTaxonomyUrl, panTaxonomyUrl));

        String humanStudyUrl = postTestStudy("testhuman", 1, "test human study", humanTaxonomyUrl);
        String bonoboStudyUrl = postTestStudy("testbonobo", 1, "test bonobo study", bonoboTaxonomyUrl);
        String chimpanzeeStudyUrl = postTestStudy("testchimpanzee", 1, "test chimpanzee study", chimpanzeeTaxonomyUrl);

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homo sapiens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Pan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homininae"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(3))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[2]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=None"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void deprecateStudy() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String studyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, false);

        mockMvc.perform(get(studyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(studyUrl));

        mockMvc.perform(patch(studyUrl)
                .content("{\"deprecated\": \"true\"}"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(studyUrl))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findUndeprecatedStudiesOnly() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String deprecatedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, true);
        String undeprecatedStudyUrl = postTestStudy("1kg", 2, "1kg phase 1", humanTaxonomyUrl, false);

        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get(undeprecatedStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get(undeprecatedStudyUrl + "/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/studies/search?taxonomy.id=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/accession?accession=1kg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homo sapiens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/text?searchTerm=1kg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));
    }

    @Test
    public void notFoundWhenFindDeprecatedStudies() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String deprecatedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, true);

        mockMvc.perform(get(deprecatedStudyUrl))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(deprecatedStudyUrl + "/analyses"))
                .andExpect(status().isNotFound());
    }

}
