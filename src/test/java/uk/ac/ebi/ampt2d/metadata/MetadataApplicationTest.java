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

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
@SpringBootTest(properties = "security.enabled=true")
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

    @Autowired
    private OAuthHelper oAuthHelper;

    @Before
    public void cleanDatabases() throws Exception {
        analysisRepository.deleteAll();
        assemblyRepository.deleteAll();
        fileRepository.deleteAll();
        sampleRepository.deleteAll();
        studyRepository.deleteAll();
        taxonomyRepository.deleteAll();
        webResourceRepository.deleteAll();
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

        MvcResult mvcResult = mockMvc.perform(post("/assemblies").with(oAuthHelper.bearerToken("test"))
                .content(testAssemblyJson.write(testAssembly).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postTaxonomy() throws Exception {
        String location = postTestTaxonomy();
        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
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

        MvcResult mvcResult = mockMvc.perform(post("/taxonomies").with(oAuthHelper.bearerToken("test"))
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postStudy() throws Exception {
        String location = postTestStudy("EGAS0001", 1, "test_human_study");

        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAS0001"));
    }

    private String postTestStudy(String accession, int version, String name) throws Exception {
        String taxonomyUrl = postTestTaxonomy();

        return postTestStudy(accession, version, name, taxonomyUrl);
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl) throws Exception {
        return postTestStudy(accession, version, name, taxonomyUrl, false, LocalDate.now());
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, boolean deprecated) throws Exception {
        return postTestStudy(accession, version, name, taxonomyUrl, deprecated, LocalDate.now());
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, LocalDate releaseDate) throws Exception {
        return postTestStudy(accession, version, name, taxonomyUrl, false, releaseDate);
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, boolean deprecated, LocalDate releaseDate) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/studies").with(oAuthHelper.bearerToken("test"))
                .content("{ " +
                        "\"id\":{ \"accession\": \"" + accession + "\",\"version\": " + version + "}," +
                        "\"name\": \"" + name + "\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"deprecated\": \"" + deprecated + "\"," +
                        "\"releaseDate\": \"" + releaseDate + "\"," +
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

        String location = postTestAnalysis("EGAA0001", assemblyUrl, studyUrl, Analysis.Technology.GWAS,
                Analysis.Type.CASE_CONTROL, "Illumina");

        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAA0001"));
    }

    private String postTestAnalysis(String accession, String assemblyUrl, String studyUrl) throws Exception {
        return postTestAnalysis(accession, assemblyUrl, studyUrl, Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina");

    }

    private String postTestAnalysis(String accession, String assemblyUrl, String studyUrl, Analysis.Technology
            technology, Analysis.Type type, String platform) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/analyses").with(oAuthHelper.bearerToken("test"))
                .content("{ " +
                        "\"id\":{ \"accession\": \"" + accession + "\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + studyUrl + "\"," +
                        "\"assembly\": \"" + assemblyUrl + "\"," +
                        "\"technology\": \"" + technology + "\"," +
                        "\"type\": \"" + type + "\"," +
                        "\"platform\": \"" + platform + "\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postFile() throws Exception {
        String location = postTestFile("EGAF0001", 1);

        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAF0001"))
                .andExpect(jsonPath("$.id.version").value(1));
    }

    private String postTestFile(String accession, int version) throws Exception {
        File testFile = new File(new AccessionVersionEntityId(accession, version), "asd123", "test_file",
                100, File.Type.TSV);

        MvcResult mvcResult = mockMvc.perform(post("/files").with(oAuthHelper.bearerToken("test"))
                .content(testFileJson.write(testFile).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postSample() throws Exception {
        String location = postTestSample("EGAN0001", "testSample");

        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"));
    }

    private String postTestSample(String accession, String name) throws Exception {
        Sample testSample = new Sample(new AccessionVersionEntityId(accession, 1), name);
        MvcResult mvcResult = mockMvc.perform(post("/samples").with(oAuthHelper.bearerToken("test"))
                .content(testSampleJson.write(testSample).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postWebResource() throws Exception {
        String location = postTestWebResource();

        mockMvc.perform(get(location).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CENTER_WEB"))
                .andExpect(jsonPath("$.resourceUrl").value("http:\\www.ebi.ac.uk"));
    }

    private String postTestWebResource() throws Exception {
        WebResource testWebResource = new WebResource(WebResource.Type.CENTER_WEB, "http:\\www.ebi.ac.uk");

        MvcResult mvcResult = mockMvc.perform(post("/webResources").with(oAuthHelper.bearerToken("test"))
                .content(testWebResourceJson.write(testWebResource).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void findAssemblyByName() throws Exception {
        String grch37Url = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/assemblies/search?name=GRCh37").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"));

        mockMvc.perform(get("/assemblies/search?name=GRCh38").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh38"));

        mockMvc.perform(get("/assemblies/search?name=NCBI36").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"));

        mockMvc.perform(get("/assemblies/search?name=GRCh38&patch=p2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh38"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"));

        mockMvc.perform(get("/assemblies/search?name=NCBI36&patch=p2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p3").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh38&patch=p3").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?accessions=GCA_000001405.3").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search?accessions=GCF_000001405.28").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCF_000001405.28")));

        mockMvc.perform(get("/assemblies/search?accessions=GCA_000001405.2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p2&accessions=GCA_000001405.3").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch37Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..assemblies[0].patch").value("p2"))
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItem("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p3&accessions=GCA_000001405.3").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));
    }

    @Test
    public void findAnalyses() throws Exception {
        List<String> testAnalysisUrls = postTestAnalyses();
        String testAnalysisOneUrl = testAnalysisUrls.get(0);
        String testAnalysisTwoUrl = testAnalysisUrls.get(1);

        mockMvc.perform(get("/analyses/search?type=CASE_CONTROL").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisOneUrl));

        mockMvc.perform(get("/analyses/search?type=TUMOR").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisTwoUrl));

        mockMvc.perform(get("/analyses/search?type=COLLECTION").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/analyses/search?platform=PacBio").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisTwoUrl));

        mockMvc.perform(get("/analyses/search?platform=illumina").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisOneUrl));

        mockMvc.perform(get("/analyses/search?platform=nextSeq").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/analyses/search?platform=pacbio&type=TUMOR").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1));
    }

    @Test
    public void findAnalysisByTechnology() throws Exception {
        List<String> testAnalysisUrls = postTestAnalyses();

        mockMvc.perform(get("/analyses/search?technology=UNKNOWN").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/analyses/search?technology=CURATION").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/analyses/search?technology=GWAS").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisUrls.get(0)));

        mockMvc.perform(get("/analyses/search?technology=ARRAY&type=TUMOR").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisUrls.get(1)));
    }

    private List<String> postTestAnalyses() throws Exception {
        String humanAssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String humanStudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        return Arrays.asList(postTestAnalysis("EGAA0001", humanAssemblyUrl, humanStudyUrl,
                Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina"),
                postTestAnalysis("EGAA0002", humanAssemblyUrl, humanStudyUrl,
                        Analysis.Technology.ARRAY, Analysis.Type.TUMOR, "PacBio"));
    }

    @Test
    public void clientErrorWhenSearchAnalysesWithInvalidType() throws Exception {
        mockMvc.perform(get("/analyses/search?type=unknown").with(oAuthHelper.bearerToken("test")))
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

        postTestAnalysis("EGAA0001", grch37AssemblyUrl, grch37StudyUrl);
        postTestAnalysis("EGAA0002", grch38AssemblyUrl, grch38StudyUrl);

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37&analyses.assembly.patch=p2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.assembly.patch=p2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36&analyses.assembly.patch=p2").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37&analyses.assembly.patch=p3").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.type=CASE_CONTROL").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.type=TUMOR").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.type=COLLECTION").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=CASE_CONTROL").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=TUMOR").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=COLLECTION").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36&analyses.type=CASE_CONTROL").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void searchStudy() throws Exception {
        postTestStudy("EGAS0001", 1, "test human study based on GRCh37");
        postTestStudy("EGAS0002", 1, "test human study based on GRCh38");

        mockMvc.perform(get("/studies/search/text").with(oAuthHelper.bearerToken("test")).param("searchTerm", "human"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(2));
        mockMvc.perform(get("/studies/search/text").with(oAuthHelper.bearerToken("test")).param("searchTerm", "important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(2));
        mockMvc.perform(get("/studies/search/text").with(oAuthHelper.bearerToken("test")).param("searchTerm", "grCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0].id.accession").value("EGAS0001"));
        mockMvc.perform(get("/studies/search/text").with(oAuthHelper.bearerToken("test")).param("searchTerm", "GrCh39"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void searchStudyByAccession() throws Exception {
        String testStudy1 = postTestStudy("EGAS0001", 1, "test human study based on GRCh37");
        String testStudy2 = postTestStudy("EGAS0001", 2, "test human study based on GRCh38");
        String testStudy3 = postTestStudy("EGAS0002", 3, "test human study based on GRCh38");

        mockMvc.perform(get("/studies/search/accession").with(oAuthHelper.bearerToken("test")).param("accession", "EGAS0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(testStudy2))
                .andExpect(jsonPath("$.id.accession").value("EGAS0001"))
                .andExpect(jsonPath("$.id.version").value(2));
        mockMvc.perform(get("/studies/search/accession").with(oAuthHelper.bearerToken("test")).param("accession", "EGAS0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(testStudy3))
                .andExpect(jsonPath("$.id.accession").value("EGAS0002"))
                .andExpect(jsonPath("$.id.version").value(3));
        mockMvc.perform(get("/studies/search/accession").with(oAuthHelper.bearerToken("test")).param("accession", "EGAS0003"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAccessionValidation() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        mockMvc.perform(post("/studies").with(oAuthHelper.bearerToken("test"))
                .content("{ " +
                        "\"id\":{ \"accession\": \"EGAS0001\",\"version\":  0 }," +
                        "\"name\": \" study1\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"releaseDate\": \"" + LocalDate.now() + "\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("id.version"))
                .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 1"));
        mockMvc.perform(post("/studies").with(oAuthHelper.bearerToken("test"))
                .content("{ " +
                        "\"id\":{ \"version\":  1 }," +
                        "\"name\": \" study1\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"center\": \"EBI\"," +
                        "\"releaseDate\": \"" + LocalDate.now() + "\"," +
                        "\"taxonomy\": \"" + taxonomyUrl + "\"" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("id.accession"))
                .andExpect(jsonPath("$.errors[0].message").value("may not be null"));
        postTestStudy("EGAS0001", 1, "test_study");
        mockMvc.perform(get("/studies/EGAS0001").with(oAuthHelper.bearerToken("test"))).andExpect(status().is4xxClientError()).andExpect(jsonPath("$" +
                ".message").value("Please provide an ID in the form accession.version"));
        mockMvc.perform(get("/studies/EGAS0001.S1").with(oAuthHelper.bearerToken("test"))).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Please provide an ID in the form accession.version"));
        mockMvc.perform(get("/studies/EGAS0001.1").with(oAuthHelper.bearerToken("test"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAS0001"));
        mockMvc.perform(get("/studies/EGAS0001.2").with(oAuthHelper.bearerToken("test"))).andExpect(status().isNotFound());
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

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9606").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9596").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=207598").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(3))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[2]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=0").with(oAuthHelper.bearerToken("test")))
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

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homo sapiens").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Pan").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homininae").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(3))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(bonoboStudyUrl))
                .andExpect(jsonPath("$..studies[2]..study.href").value(chimpanzeeStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=None").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void deprecateStudy() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String studyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, false);

        mockMvc.perform(get(studyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(studyUrl));

        mockMvc.perform(patch(studyUrl).with(oAuthHelper.bearerToken("test"))
                .content("{\"deprecated\": \"true\"}"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(studyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getStudyDoesNotIncludeDeprecatedField() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String studyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, false);

        mockMvc.perform(get(studyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(studyUrl))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.deprecated").doesNotExist());
    }

    @Test
    public void findUndeprecatedStudiesOnly() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String deprecatedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, true);
        String undeprecatedStudyUrl = postTestStudy("1kg", 2, "1kg phase 1", humanTaxonomyUrl, false);

        mockMvc.perform(get("/studies").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get(undeprecatedStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get(undeprecatedStudyUrl + "/analyses").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/studies/search?taxonomy.id=9606").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/accession?accession=1kg").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("1kg"))
                .andExpect(jsonPath("$.id.version").value(2))
                .andExpect(jsonPath("$..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9606").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homo sapiens").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/text?searchTerm=1kg").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));
    }

    @Test
    public void notFoundWhenFindDeprecatedStudies() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String deprecatedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, true);

        mockMvc.perform(get(deprecatedStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(deprecatedStudyUrl + "/analyses").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deprecatedStudyCouldBeUndeprecated() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, false);

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));

        mockMvc.perform(patch(humanStudyUrl + "/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"deprecated\" : \"" + true + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch(humanStudyUrl + "/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"deprecated\" : \"" + false + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));
    }

    @Test
    public void browsableIsAPropertyOfStudy() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl);

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.browsable").value(false));

        mockMvc.perform(get("/studies/search?browsable=true").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(patch(humanStudyUrl).with(oAuthHelper.bearerToken("test"))
                .content("{\"browsable\" : true }"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/studies/search?browsable=true").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl));
    }

    private ResultMatcher isReleaseDateEqualTo(LocalDate localDate) {
        return new ResultMatcher() {
            @Override
            public void match(MvcResult mvcResult) throws Exception {
                JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());
                LocalDate releaseDate = LocalDate.parse(jsonObject.getString("releaseDate"));
                assert releaseDate.equals(localDate);
            }
        };
    }

    private ResultMatcher isLastModifiedDateBetween(ZonedDateTime start, ZonedDateTime end) {
        return new ResultMatcher() {
            @Override
            public void match(MvcResult mvcResult) throws Exception {
                JSONObject jsonObject = new JSONObject(mvcResult.getResponse().getContentAsString());
                ZonedDateTime lastModifiedDate = ZonedDateTime.parse(jsonObject.getString("lastModifiedDate"));
                assert lastModifiedDate.isAfter(start);
                assert lastModifiedDate.isBefore(end);
            }
        };
    }

    private void checkLastModifiedDate(String url, String type, ZonedDateTime startTime, ZonedDateTime endTime)
            throws Exception {
        mockMvc.perform(get(url).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.." + type + ".href").value(url))
                .andExpect(jsonPath("$.lastModifiedDate").isNotEmpty())
                .andExpect(isLastModifiedDateBetween(startTime, endTime));
    }

    private void patchResource(String url) throws Exception {
        mockMvc.perform(patch(url).with(oAuthHelper.bearerToken("test"))
                .content("{\"name\": \"nothing important\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void metadataObjectsAreAuditable() throws Exception {
        ZonedDateTime startTime = ZonedDateTime.now();
        String testAssembly = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String testTaxonomy = postTestTaxonomy(9606, "Homo sapiens");
        String testStudy = postTestStudy("testhuman", 1, "test human study", testTaxonomy);
        String testAnalysis = postTestAnalysis("testhuman", testAssembly, testStudy);
        String testFile = postTestFile("testhuman", 1);
        String testSample = postTestSample("testhuman", "test human sample");
        String testWebResource = postTestWebResource();
        ZonedDateTime endTime = ZonedDateTime.now();

        checkLastModifiedDate(testAssembly, "assembly", startTime, endTime);
        checkLastModifiedDate(testTaxonomy, "taxonomy", startTime, endTime);
        checkLastModifiedDate(testStudy, "study", startTime, endTime);
        checkLastModifiedDate(testAnalysis, "analysis", startTime, endTime);
        checkLastModifiedDate(testFile, "file", startTime, endTime);
        checkLastModifiedDate(testSample, "sample", startTime, endTime);
        checkLastModifiedDate(testWebResource, "webResource", startTime, endTime);

        startTime = ZonedDateTime.now();
        patchResource(testAssembly);
        patchResource(testTaxonomy);
        patchResource(testStudy);
        patchResource(testAnalysis);
        patchResource(testFile);
        patchResource(testSample);
        mockMvc.perform(patch(testWebResource).with(oAuthHelper.bearerToken("test"))
                .content("{\"resourceUrl\": \"http://nothing.important.com\"}"))
                .andExpect(status().is2xxSuccessful());
        endTime = ZonedDateTime.now();

        checkLastModifiedDate(testAssembly, "assembly", startTime, endTime);
        checkLastModifiedDate(testTaxonomy, "taxonomy", startTime, endTime);
        checkLastModifiedDate(testStudy, "study", startTime, endTime);
        checkLastModifiedDate(testAnalysis, "analysis", startTime, endTime);
        checkLastModifiedDate(testFile, "file", startTime, endTime);
        checkLastModifiedDate(testSample, "sample", startTime, endTime);
        checkLastModifiedDate(testWebResource, "webResource", startTime, endTime);
    }

    public void findStudyByReleaseDate() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        String releasedYesterday = postTestStudy("releasedYesterday", 1, "nothing important", humanTaxonomyUrl, yesterday);
        String releasedToday = postTestStudy("releasedToday", 1, "nothing important", humanTaxonomyUrl, today);
        String releasedTomorrow = postTestStudy("releasedTomorrow", 1, "nothing important", humanTaxonomyUrl, tomorrow);

        mockMvc.perform(get("/studies/search/release-date?to=" + today).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(releasedYesterday))
                .andExpect(jsonPath("$..studies[1]..study.href").value(releasedToday));

        mockMvc.perform(get("/studies/search/release-date?from=" + today).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(releasedToday));

        mockMvc.perform(get("/studies/search/release-date?from=" + today + "&to=" + today).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(releasedToday));
    }

    @Test
    public void clientErrorWhenSearchStudiesByReleaseDateWithInvalidInput() throws Exception {
        mockMvc.perform(get("/studies/search/release-date").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.exception").value("java.lang.IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value("Either from or to needs to be non-null"));

        mockMvc.perform(get("/studies/search/release-date?from=" + "wrong-format-date").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.exception").value("java.lang.IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value("Please provide a date in the form yyyy-mm-dd"));
    }

    @Test
    public void findPublicStudiesOnly() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanAssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        String yesterdayReleasedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, yesterday);
        String todayReleasedStudyUrl = postTestStudy("1kg", 2, "1kg phase 1", humanTaxonomyUrl, today);
        String tomorrowReleasedStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl, tomorrow);

        String yesterdayReleasedAnalysisUrl = postTestAnalysis("analysisReleasedYesterday", humanAssemblyUrl, yesterdayReleasedStudyUrl);

        mockMvc.perform(get("/studies").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get(yesterdayReleasedStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(yesterdayReleasedStudyUrl));

        mockMvc.perform(get(yesterdayReleasedStudyUrl + "/analyses").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(yesterdayReleasedAnalysisUrl));

        mockMvc.perform(get(todayReleasedStudyUrl + "/analyses").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/studies/search?taxonomy.id=9606").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/accession?accession=1kg").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("1kg"))
                .andExpect(jsonPath("$.id.version").value(2))
                .andExpect(jsonPath("$..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/release-date?from=" + LocalDate.now()).with(oAuthHelper.bearerToken
                ("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9606").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homo sapiens").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/text?searchTerm=1kg").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

    }

    @Test
    public void notFoundWhenFindYetToPublishedStudies() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String tomorrowReleasedStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl, LocalDate.now().plusDays(1));

        mockMvc.perform(get(tomorrowReleasedStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(tomorrowReleasedStudyUrl + "/analyses").with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void studyReleaseDateCouldBeChanged() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl, today);

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(today));

        mockMvc.perform(patch(humanStudyUrl + "/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + tomorrow + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(tomorrow));

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch(humanStudyUrl + "/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + today + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(today));

        mockMvc.perform(get(humanStudyUrl).with(oAuthHelper.bearerToken("test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(today));
    }

    @Test
    public void notFoundWhenPatchAnUnexistingStudy() throws Exception {
        mockMvc.perform(patch("studies/unexist.1/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + LocalDate.now() + "\" }"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void badRequestWhenPatchAStudyWithInvalidRequestBody() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl);

        mockMvc.perform(patch(humanStudyUrl + "/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + 2001 + "\" }"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch(humanStudyUrl + "/patch").with(oAuthHelper.bearerToken("test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void withOutOAuthToken() throws Exception {
        // Any url other than root and swagger is Secured
        mockMvc.perform(get("/taxonomies")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/studies")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/")).andExpect(status().isOk()); // Root is not secured
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk()); // Swagger is not secured

    }

}
