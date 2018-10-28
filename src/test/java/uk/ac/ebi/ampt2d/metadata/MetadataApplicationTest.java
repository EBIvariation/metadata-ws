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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.DbAccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ContactRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.CrossReferenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.DacRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.DuoRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PolicyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    private ContactRepository contactRepository;

    @Autowired
    private CrossReferenceRepository crossReferenceRepository;

    @Autowired
    private DacRepository dacRepository;

    @Autowired
    private DuoRepository duoRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private WebResourceRepository webResourceRepository;

    @Autowired
    private JacksonTester<DbAccessionVersionId> testDbAccessionVersionIdJson;

    @Autowired
    private JacksonTester<ReferenceSequence> testReferenceSequenceJson;

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
        policyRepository.deleteAll();
        duoRepository.deleteAll();
        crossReferenceRepository.deleteAll();
        dacRepository.deleteAll();
        contactRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        fileRepository.deleteAll();
        sampleRepository.deleteAll();
        studyRepository.deleteAll();
        taxonomyRepository.deleteAll();
        webResourceRepository.deleteAll();
    }

    @Test
    public void postReferenceSequence() throws Exception {
        postReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"), ReferenceSequence.Type.ASSEMBLY);

        postReferenceSequence("GRCh38", "p3",
                Arrays.asList("GCA_000001406.3", "GCF_000001406.14"), ReferenceSequence.Type.GENE);

        postReferenceSequence("GRCh39", "p4",
                Arrays.asList("GCA_000001407.3", "GCF_000001407.14"), ReferenceSequence.Type.TRANSCRIPTOME);
    }

    private void postReferenceSequence(String name, String patch, List accessions, ReferenceSequence.Type type) throws Exception {
        String location = postTestReferenceSequence(name, patch, accessions, type);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    private String postTestReferenceSequence(String name, String patch, List<String> accessions) throws Exception {
        return postTestReferenceSequence(name, patch, accessions, ReferenceSequence.Type.ASSEMBLY);
    }

    private String postTestReferenceSequence(String name, String patch, List<String> accessions, ReferenceSequence.Type type) throws Exception {
        ReferenceSequence testReferenceSequence = new ReferenceSequence(name, patch, accessions, type);

        MvcResult mvcResult = mockMvc.perform(post("/reference-sequences")
                .content(testReferenceSequenceJson.write(testReferenceSequence).getJson()))
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
        if ( ancestors == null ) {
            ancestors = Arrays.asList();
        }

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
        return postTestStudy(accession, version, name, taxonomyUrl, false, LocalDate.now());
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, boolean deprecated) throws Exception {
        return postTestStudy(accession, version, name, taxonomyUrl, deprecated, LocalDate.now());
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, LocalDate releaseDate) throws Exception {
        return postTestStudy(accession, version, name, taxonomyUrl, false, releaseDate);
    }

    private String postTestStudy(String accession, int version, String name, String taxonomyUrl, boolean deprecated, LocalDate releaseDate) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/studies")
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
        String referenceSequenceUrl = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String studyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        String location = postTestAnalysis("EGAA0001", referenceSequenceUrl, studyUrl, Analysis.Technology.GWAS,
                Analysis.Type.CASE_CONTROL, "Illumina");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAA0001"));
    }

    private String postTestAnalysis(String accession, String referenceSequenceUrl, String studyUrl) throws Exception {
        return postTestAnalysis(accession, referenceSequenceUrl, studyUrl, Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina");

    }

    private String postTestAnalysis(String accession, String referenceSequenceUrl, String studyUrl, Analysis.Technology
            technology, Analysis.Type type, String platform) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"id\":{ \"accession\": \"" + accession + "\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + studyUrl + "\"," +
                        "\"referenceSequence\": \"" + referenceSequenceUrl + "\"," +
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

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAF0001"))
                .andExpect(jsonPath("$.id.version").value(1));
    }

    private String postTestFile(String accession, int version) throws Exception {
        File testFile = new File(new AccessionVersionEntityId(accession, version), "asd123", "test_file",
                100, File.Type.TSV);

        MvcResult mvcResult = mockMvc.perform(post("/files")
                .content(testFileJson.write(testFile).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postSample() throws Exception {
        String location = postTestSample("EGAN0001", "testSample");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"));
    }

    private String postTestSample(String accession, String name) throws Exception {
        Sample testSample = new Sample(new AccessionVersionEntityId(accession, 1), name);
        MvcResult mvcResult = mockMvc.perform(post("/samples")
                .content(testSampleJson.write(testSample).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postWebResource() throws Exception {
        String location = postTestWebResource();

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CENTER_WEB"))
                .andExpect(jsonPath("$.resourceUrl").value("http:\\www.ebi.ac.uk"));
    }

    private String postTestWebResource() throws Exception {
        WebResource testWebResource = new WebResource(WebResource.Type.CENTER_WEB, "http:\\www.ebi.ac.uk");

        MvcResult mvcResult = mockMvc.perform(post("/webResources")
                .content(testWebResourceJson.write(testWebResource).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void validateWebResourceURL() throws Exception {
        // Valid URLs
        // HTTP protocol
        String url = "http://api.plos.org/search?q=title:%22Drosophila%22%20and%20body:%22RNA%22&fl=id,abstract";
        postWebResourceValidURL(url);

        // HTTPS protocol
        url = "https://localhost:8090/swagger-ui.html#/WebResource_Entity/saveWebResourceUsingPOST";
        postWebResourceValidURL(url);

        // FTP protocol
        url = "ftp://ftp.sra.ebi.ac.uk/meta/xsd/sra_1_5/SRA.study.xsd";
        postWebResourceValidURL(url);

        // Complex URL
        url = "http://MVSXX.COMPANY.COM:04445/CICSPLEXSM//JSMITH/VIEW/OURLOCTRAN?A_TRANID=P*&O_TRANID=NE";
        postWebResourceValidURL(url);

        // Invalid URLs
        // Blank URL
        url = "";
        postWebResourceInvalidURL(url);

        // Wrong protocol
        url = "htttps://www.ebi.ac.uk";
        postWebResourceInvalidURL(url);

        // Without protocol
        url = "www.google.com";
        postWebResourceInvalidURL(url);

        // Invalid characters
        url = "http://www.space address.org";
        postWebResourceInvalidURL(url);

        // File resource
        url = "//fileserver/code/src/main/app.java";
        postWebResourceInvalidURL(url);
    }

    private void postWebResourceValidURL(String url) throws Exception {
        WebResource testWebResource = new WebResource(WebResource.Type.CENTER_WEB, url);

        MvcResult mvcResult = mockMvc.perform(post("/webResources")
                .content(testWebResourceJson.write(testWebResource).getJson()))
                .andExpect(status().isCreated()).andReturn();

        mockMvc.perform(get(mvcResult.getResponse().getHeader("Location")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CENTER_WEB"))
                .andExpect(jsonPath("$.resourceUrl").value(url));
    }

    private void postWebResourceInvalidURL(String url) throws Exception {
        WebResource testWebResource = new WebResource(WebResource.Type.CENTER_WEB, url);

        mockMvc.perform(post("/webResources")
                .content(testWebResourceJson.write(testWebResource).getJson()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void findReferenceSequenceByName() throws Exception {
        String grch37Url = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestReferenceSequence("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch37Url))
                .andExpect(jsonPath("$..reference-sequences[0].name").value("GRCh37"));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch38Url))
                .andExpect(jsonPath("$..reference-sequences[0].name").value("GRCh38"));

        mockMvc.perform(get("/reference-sequences/search?name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh37&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch37Url))
                .andExpect(jsonPath("$..reference-sequences[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..reference-sequences[0].patch").value("p2"));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh38&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch38Url))
                .andExpect(jsonPath("$..reference-sequences[0].name").value("GRCh38"))
                .andExpect(jsonPath("$..reference-sequences[0].patch").value("p2"));

        mockMvc.perform(get("/reference-sequences/search?name=NCBI36&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh37&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh38&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));

        mockMvc.perform(get("/reference-sequences/search?accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch37Url))
                .andExpect(jsonPath("$..reference-sequences[0].accessions").isArray())
                .andExpect(jsonPath("$..reference-sequences[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/reference-sequences/search?accessions=GCF_000001405.28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch38Url))
                .andExpect(jsonPath("$..reference-sequences[0].accessions").isArray())
                .andExpect(jsonPath("$..reference-sequences[0].accessions[*]", hasItems("GCF_000001405.28")));

        mockMvc.perform(get("/reference-sequences/search?accessions=GCA_000001405.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh37&patch=p2&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch37Url))
                .andExpect(jsonPath("$..reference-sequences[0].accessions").isArray())
                .andExpect(jsonPath("$..reference-sequences[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..reference-sequences[0].patch").value("p2"))
                .andExpect(jsonPath("$..reference-sequences[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/reference-sequences/search?name=GRCh37&patch=p3&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));
    }

    @Test
    public void findReferenceSequenceByType() throws Exception {
        String grch37Url = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"), ReferenceSequence.Type.ASSEMBLY);
        String grch38Url = postTestReferenceSequence("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"), ReferenceSequence.Type.ASSEMBLY);
        String grch39Url = postTestReferenceSequence("GRCh39", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"), ReferenceSequence.Type.GENE);

        mockMvc.perform(get("/reference-sequences/search?type=" + ReferenceSequence.Type.ASSEMBLY.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(2))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch37Url))
                .andExpect(jsonPath("$..reference-sequences[1]..referenceSequence.href").value(grch38Url))
                .andExpect(jsonPath("$..reference-sequences[0].type").value(ReferenceSequence.Type.ASSEMBLY.toString()))
                .andExpect(jsonPath("$..reference-sequences[1].type").value(ReferenceSequence.Type.ASSEMBLY.toString()));

        mockMvc.perform(get("/reference-sequences/search?type=" + ReferenceSequence.Type.GENE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(1))
                .andExpect(jsonPath("$..reference-sequences[0]..referenceSequence.href").value(grch39Url))
                .andExpect(jsonPath("$..reference-sequences[0].type").value(ReferenceSequence.Type.GENE.toString()));

        mockMvc.perform(get("/reference-sequences/search?type=" + ReferenceSequence.Type.TRANSCRIPTOME.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..reference-sequences").isArray())
                .andExpect(jsonPath("$..reference-sequences.length()").value(0));
    }

    @Test
    public void clientErrorWhenSearchReferenceSequenceWithInvalidType() throws Exception {
        mockMvc.perform(get("/reference-sequences/search?type=UNKNOWN"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void findAnalyses() throws Exception {
        List<String> testAnalysisUrls = postTestAnalyses();
        String testAnalysisOneUrl = testAnalysisUrls.get(0);
        String testAnalysisTwoUrl = testAnalysisUrls.get(1);

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
    public void findAnalysisByTechnology() throws Exception {
        List<String> testAnalysisUrls = postTestAnalyses();

        mockMvc.perform(get("/analyses/search?technology=UNKNOWN"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/analyses/search?technology=CURATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/analyses/search?technology=GWAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisUrls.get(0)));

        mockMvc.perform(get("/analyses/search?technology=ARRAY&type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(testAnalysisUrls.get(1)));
    }

    private List<String> postTestAnalyses() throws Exception {
        String humanReferenceSequenceUrl = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String humanStudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        return Arrays.asList(postTestAnalysis("EGAA0001", humanReferenceSequenceUrl, humanStudyUrl,
                            Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina"),
                postTestAnalysis("EGAA0002", humanReferenceSequenceUrl, humanStudyUrl,
                                Analysis.Technology.ARRAY, Analysis.Type.TUMOR, "PacBio"));
    }

    @Test
    public void clientErrorWhenSearchAnalysesWithInvalidType() throws Exception {
        mockMvc.perform(get("/analyses/search?type=unknown"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void findStudies() throws Exception {
        String grch37ReferenceSequenceUrl = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38ReferenceSequenceUrl = postTestReferenceSequence("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));
        String grch37StudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");
        String grch38StudyUrl = postTestStudy("EGAS0001", 2, "test_human_study");

        postTestAnalysis("EGAA0001", grch37ReferenceSequenceUrl, grch37StudyUrl);
        postTestAnalysis("EGAA0002", grch38ReferenceSequenceUrl, grch38StudyUrl);

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh37&analyses.referenceSequence.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch37StudyUrl));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh38&analyses.referenceSequence.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=NCBI36&analyses.referenceSequence.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh37&analyses.referenceSequence.patch=p3"))
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

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh38&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grch38StudyUrl));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh38&analyses.type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=GRCh38&analyses.type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.referenceSequence.name=NCBI36&analyses.type=CASE_CONTROL"))
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
        String testStudy1 = postTestStudy("EGAS0001", 1, "test human study based on GRCh37");
        String testStudy2 = postTestStudy("EGAS0001", 2, "test human study based on GRCh38");
        String testStudy3 = postTestStudy("EGAS0002", 3, "test human study based on GRCh38");

        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(testStudy2))
                .andExpect(jsonPath("$.id.accession").value("EGAS0001"))
                .andExpect(jsonPath("$.id.version").value(2));
        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(testStudy3))
                .andExpect(jsonPath("$.id.accession").value("EGAS0002"))
                .andExpect(jsonPath("$.id.version").value(3));
        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0003"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAccessionValidation() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        mockMvc.perform(post("/studies")
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
        mockMvc.perform(post("/studies")
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
    public void getStudyDoesNotIncludeDeprecatedField() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String studyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, false);

        mockMvc.perform(get(studyUrl))
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
                .andExpect(jsonPath("$.id.accession").value("1kg"))
                .andExpect(jsonPath("$.id.version").value(2))
                .andExpect(jsonPath("$..study.href").value(undeprecatedStudyUrl));

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

    @Test
    public void deprecatedStudyCouldBeUndeprecated() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, false);

        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));

        mockMvc.perform(patch(humanStudyUrl + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"deprecated\" : \"" + true + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));

        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch(humanStudyUrl + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"deprecated\" : \"" + false + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));

        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(humanStudyUrl));
    }

    @Test
    public void browsableIsAPropertyOfStudy() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl);

        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.browsable").value(false));

        mockMvc.perform(get("/studies/search?browsable=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(patch(humanStudyUrl)
                .content("{\"browsable\" : true }"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/studies/search?browsable=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrl));
    }

    private ResultMatcher isReleaseDateEqualTo(LocalDate localDate) {
        return new ResultMatcher() {
            @Override
            public void match(MvcResult mvcResult) throws Exception {
                JSONObject jsonObject =  new JSONObject(mvcResult.getResponse().getContentAsString());
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
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.." + type + ".href").value(url))
                .andExpect(jsonPath("$.lastModifiedDate").isNotEmpty())
                .andExpect(isLastModifiedDateBetween(startTime, endTime));
    }

    private void patchResource(String url) throws Exception {
        mockMvc.perform(patch(url)
                .content("{\"name\": \"nothing important\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void metadataObjectsAreAuditable() throws Exception {
        ZonedDateTime startTime = ZonedDateTime.now();
        String testReferenceSequence = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String testTaxonomy = postTestTaxonomy(9606, "Homo sapiens");
        String testStudy = postTestStudy("testhuman", 1, "test human study", testTaxonomy);
        String testAnalysis = postTestAnalysis("testhuman", testReferenceSequence, testStudy);
        String testFile = postTestFile("testhuman", 1);
        String testSample = postTestSample("testhuman", "test human sample");
        String testWebResource = postTestWebResource();
        ZonedDateTime endTime = ZonedDateTime.now();

        checkLastModifiedDate(testReferenceSequence, "referenceSequence", startTime, endTime);
        checkLastModifiedDate(testTaxonomy, "taxonomy", startTime, endTime);
        checkLastModifiedDate(testStudy, "study", startTime, endTime);
        checkLastModifiedDate(testAnalysis, "analysis", startTime, endTime);
        checkLastModifiedDate(testFile, "file", startTime, endTime);
        checkLastModifiedDate(testSample, "sample", startTime, endTime);
        checkLastModifiedDate(testWebResource, "webResource", startTime, endTime);

        startTime = ZonedDateTime.now();
        patchResource(testReferenceSequence);
        patchResource(testTaxonomy);
        patchResource(testStudy);
        patchResource(testAnalysis);
        patchResource(testFile);
        patchResource(testSample);
        mockMvc.perform(patch(testWebResource)
                .content("{\"resourceUrl\": \"http://nothing.important.com\"}"))
                .andExpect(status().is2xxSuccessful());
        endTime = ZonedDateTime.now();

        checkLastModifiedDate(testReferenceSequence, "referenceSequence", startTime, endTime);
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

        mockMvc.perform(get("/studies/search/release-date?to=" + today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(releasedYesterday))
                .andExpect(jsonPath("$..studies[1]..study.href").value(releasedToday));

        mockMvc.perform(get("/studies/search/release-date?from=" + today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(releasedToday));

        mockMvc.perform(get("/studies/search/release-date?from=" + today + "&to=" + today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(releasedToday));
    }

    @Test
    public void clientErrorWhenSearchStudiesByReleaseDateWithInvalidInput() throws Exception {
        mockMvc.perform(get("/studies/search/release-date"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.exception").value("java.lang.IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value("Either from or to needs to be non-null"));

        mockMvc.perform(get("/studies/search/release-date?from=" + "wrong-format-date"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.exception").value("java.lang.IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value("Please provide a date in the form yyyy-mm-dd"));
    }

    @Test
    public void findPublicStudiesOnly() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanReferenceSequenceUrl = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        String yesterdayReleasedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, yesterday);
        String todayReleasedStudyUrl = postTestStudy("1kg", 2, "1kg phase 1", humanTaxonomyUrl, today);
        String tomorrowReleasedStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl, tomorrow);

        String yesterdayReleasedAnalysisUrl = postTestAnalysis("analysisReleasedYesterday", humanReferenceSequenceUrl, yesterdayReleasedStudyUrl);

        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get(yesterdayReleasedStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(yesterdayReleasedStudyUrl));

        mockMvc.perform(get(yesterdayReleasedStudyUrl + "/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(1))
                .andExpect(jsonPath("$..analyses[0]..analysis.href").value(yesterdayReleasedAnalysisUrl));

        mockMvc.perform(get(todayReleasedStudyUrl + "/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..analyses").isArray())
                .andExpect(jsonPath("$..analyses.length()").value(0));

        mockMvc.perform(get("/studies/search?taxonomy.id=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/accession?accession=1kg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.accession").value("1kg"))
                .andExpect(jsonPath("$.id.version").value(2))
                .andExpect(jsonPath("$..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/release-date?from=" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-id?id=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/taxonomy-name?name=Homo sapiens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/text?searchTerm=1kg"))
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

        mockMvc.perform(get(tomorrowReleasedStudyUrl))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(tomorrowReleasedStudyUrl + "/analyses"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void studyReleaseDateCouldBeChanged() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl, today);


        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(today));

        mockMvc.perform(patch(humanStudyUrl + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + tomorrow + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(tomorrow));

        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch(humanStudyUrl + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + today + "\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(today));

        mockMvc.perform(get(humanStudyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").exists())
                .andExpect(isReleaseDateEqualTo(today));
    }

    @Test
    public void notFoundWhenPatchAnUnexistingStudy() throws Exception {
        mockMvc.perform(patch("studies/unexist.1/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + LocalDate.now() + "\" }"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void badRequestWhenPatchAStudyWithInvalidRequestBody() throws Exception {
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        String humanStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl);

        mockMvc.perform(patch(humanStudyUrl + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"releaseDate\" : \"" + 2001 + "\" }"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch(humanStudyUrl + "/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void searchStudyByPagingAndSorting() throws Exception {
        String humanStudyUrlB = postTestStudy("EGAS0001", 1, "test human B");
        String humanStudyUrlA = postTestStudy("EGAS0002", 1, "test human A");

        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));
        mockMvc.perform(get("/studies?size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrlB));
        mockMvc.perform(get("/studies?size=1&sort=name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(humanStudyUrlA));
        mockMvc.perform(get("/studies?page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void findLinkedStudies() throws Exception {
        String testTaxonomy = postTestTaxonomy(9606, "Homo sapiens");
        String testStudy1 = postTestStudy("testhuman", 1, "test human study", testTaxonomy);
        String testStudy2 = postTestStudy("testhuman", 2, "test human study", testTaxonomy);
        String testStudy3 = postTestStudy("testhuman", 3, "test human study", testTaxonomy);
        String testStudy4 = postTestStudy("testhuman", 4, "test human study", testTaxonomy);

        mockMvc.perform(patch(testStudy1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"childStudies\":" +
                        testListJson.write(Arrays.asList(testStudy2, testStudy3)).getJson() +
                        "}"))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(get(testStudy1 + "/linkedStudies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[*]..study.href", hasItems(testStudy2, testStudy3)));
        mockMvc.perform(get(testStudy2 + "/linkedStudies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[*]..study.href", hasItems(testStudy1, testStudy3)));
        mockMvc.perform(get(testStudy3 + "/linkedStudies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[*]..study.href", hasItems(testStudy1, testStudy2)));
        mockMvc.perform(get(testStudy4 + "/linkedStudies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        String nonexistent = testStudy1.replace("testhuman", "testmouse");
        mockMvc.perform(patch(testStudy1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"childStudies\":" +
                        testListJson.write(Arrays.asList(nonexistent)).getJson() +
                        "}"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(testStudy1 + "/linkedStudies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void testCORS() throws Exception {
        mockMvc.perform(options("/")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://www.evil-url.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://www.evil-url.com"))
                .andExpect(header().string("Allow", containsString("GET")));
    }

    @Test
    public void postContact() throws Exception {
        String location = postTestContact("test@test.com");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void clientErrorWhenPostContactWithDuplicatedEmails() throws Exception {
        String email = "test@test.com";
        String jsonContent = "{ " +
                "\"email\": \"" + email + "\"," +
                "\"title\": \"" + "Dr." + "\"," +
                "\"firstName\": \"" + "First" + "\"," +
                "\"surname\": \"" + "Last" + "\"" +
                "}";

        postTestContact(email);
        mockMvc.perform(post("/contacts")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());
    }

    private String postTestContact(String email) throws Exception {
        String jsonContent = "{ " +
                "\"email\": \"" + email + "\"," +
                "\"title\": \"" + "Dr." + "\"," +
                "\"firstName\": \"" + "First" + "\"," +
                "\"surname\": \"" + "Last" + "\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/contacts")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postDac() throws Exception {
        String contact = postTestContact("test@test.com");
        String location = postTestDac("testdac", 1, contact);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionEntityId.accession").value("testdac"));

        mockMvc.perform(get(location + "/mainContact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..contact.href").value(contact));
    }

    @Test
    public void clientErrorWhenPostDacWithDuplicatedAccessions() throws Exception {
        String accession = "testdac";
        int version = 1;
        String contact = postTestContact("test@test.com");
        String location = postTestDac(accession, version, contact);

        String jsonContent = "{ " +
                "\"accessionVersionEntityId\":{ \"accession\": \"" + accession + "\",\"version\": " + version + "}," +
                "\"title\": \"" + "title" + "\"," +
                "\"center\": \"" + "center" + "\"," +
                "\"mainContact\": \"" + contact + "\"" +
                "}";

        mockMvc.perform(post("/dacs")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());
    }

    private String postTestDac(String accession, int version, String mainContact) throws Exception {
        String jsonContent = "{ " +
                "\"accessionVersionEntityId\":{ \"accession\": \"" + accession + "\",\"version\": " + version + "}," +
                "\"title\": \"" + "title" + "\"," +
                "\"center\": \"" + "center" + "\"," +
                "\"mainContact\": \"" + mainContact + "\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/dacs")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void findDacByAccession() throws Exception {
        String contact = postTestContact("test@test.com");
        String testDac1 = postTestDac("testDac1", 1, contact);
        String testDac2 = postTestDac("testDac1", 2, contact);
        String testDac3 = postTestDac("testDac3", 1, contact);

        mockMvc.perform(get("/dacs/search/accession").param("accession", "testDac1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..dacs").isArray())
                .andExpect(jsonPath("$..dacs.length()").value(1))
                .andExpect(jsonPath("$..dacs[0]..dac.href").value(testDac2))
                .andExpect(jsonPath("$..dacs[0].accessionVersionEntityId.accession").value("testDac1"))
                .andExpect(jsonPath("$..dacs[0].accessionVersionEntityId.version").value(2));
        mockMvc.perform(get("/dacs/search/accession").param("accession", "testDac3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..dacs").isArray())
                .andExpect(jsonPath("$..dacs.length()").value(1))
                .andExpect(jsonPath("$..dacs[0]..dac.href").value(testDac3))
                .andExpect(jsonPath("$..dacs[0].accessionVersionEntityId.accession").value("testDac3"))
                .andExpect(jsonPath("$..dacs[0].accessionVersionEntityId.version").value(1));
        mockMvc.perform(get("/dacs/search/accession").param("accession", "unexisted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..dacs").isArray())
                .andExpect(jsonPath("$..dacs.length()").value(0));
    }

    @Test
    public void postCrossReference() throws Exception {
        String location = postTestCrossReference("DUO", "0000005");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dbAccessionVersionId.db").value("DUO"))
                .andExpect(jsonPath("$.dbAccessionVersionId.accession").value("0000005"));
    }

    @Test
    public void clientErrorWhenPostSameCrossReference() throws Exception {
        String db = "DUO";
        String accession = "0000005";
        postTestCrossReference(db, accession);

        DbAccessionVersionId dbAccessionVersionId = new DbAccessionVersionId(db, accession);
        String jsonContent = "{ " +
                "\"dbAccessionVersionId\": " + testDbAccessionVersionIdJson.write(dbAccessionVersionId).getJson() + "," +
                "\"label\": \"" + "label" + "\"," +
                "\"url\": \"" + "url" + "\"" +
                "}";

        mockMvc.perform(post("/crossReferences")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());
    }

    private String postTestCrossReference(String db, String accession) throws Exception {
        DbAccessionVersionId dbAccessionVersionId = new DbAccessionVersionId(db, accession);
        String jsonContent = "{ " +
                "\"dbAccessionVersionId\": " + testDbAccessionVersionIdJson.write(dbAccessionVersionId).getJson() + "," +
                "\"label\": \"" + "label" + "\"," +
                "\"url\": \"" + "url" + "\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/crossReferences")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postDuo() throws Exception {
        String crUrl = postTestCrossReference("DUO", "0000007");
        String duoUrl = postTestDuo(crUrl, null);

        mockMvc.perform(get(duoUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..self.href").value(duoUrl));

        mockMvc.perform(get(duoUrl + "/condition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..self.href").value(crUrl));

        String modifierUrl = postTestCrossReference("DUO", "0000008");
        String duoWithModifierUrl = postTestDuo(crUrl, Arrays.asList(modifierUrl));

        mockMvc.perform(get(duoWithModifierUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..self.href").value(duoWithModifierUrl));

        mockMvc.perform(get(duoWithModifierUrl + "/condition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..self.href").value(crUrl));

        mockMvc.perform(get(duoWithModifierUrl + "/modifiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..crossReferences").isArray())
                .andExpect(jsonPath("$..crossReferences.length()").value(1))
                .andExpect(jsonPath("$..crossReferences[0]..crossReference.href").value(modifierUrl));
    }

    @Test
    public void clientErrorWhenPostDuosWithDuplicatedConditions() throws Exception {
        List<String> modifiers = new ArrayList<>();
        String crUrl = postTestCrossReference("DUO", "0000007");
        String jsonContent = "{ " +
                "\"condition\": \"" + crUrl + "\"," +
                "\"modifiers\": " + testListJson.write(modifiers).getJson() + "" +
                "}";

        mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());

        String modifierOneUrl = postTestCrossReference("DS", "0001");
        modifiers.add(modifierOneUrl);
        jsonContent = "{ " +
                "\"condition\": \"" + crUrl + "\"," +
                "\"modifiers\": " + testListJson.write(modifiers).getJson() + "" +
                "}";

        mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());

        String modifierTwoUrl = postTestCrossReference("DS", "0002");
        modifiers.add(modifierTwoUrl);
        jsonContent = "{ " +
                "\"condition\": \"" + crUrl + "\"," +
                "\"modifiers\": " + testListJson.write(modifiers).getJson() + "" +
                "}";

        mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());
    }

    private String postTestDuo(String conditionUrl, List<String> modifierUrls) throws Exception {
        if ( modifierUrls == null ) {
            modifierUrls = Arrays.asList();
        }

        String jsonContent = "{ " +
                "\"condition\": \"" + conditionUrl + "\"," +
                "\"modifiers\": " + testListJson.write(modifierUrls).getJson() + "" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/duos")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postPolicy() throws Exception {
        String contact = postTestContact("test@test.com");
        String dacUrl = postTestDac("testDac1", 1, contact);
        String policyUrl = postTestPolicy("testPolicy1", 1, dacUrl, null);

        mockMvc.perform(get(policyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionEntityId.accession").value("testPolicy1"))
                .andExpect(jsonPath("$..self.href").value(policyUrl));

        mockMvc.perform(get(policyUrl + "/dac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionEntityId.accession").value("testDac1"))
                .andExpect(jsonPath("$..self.href").value(dacUrl));

        mockMvc.perform(get(policyUrl + "/dataUseConditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..duos").isArray())
                .andExpect(jsonPath("$..duos.length()").value(0));

        String crUrl = postTestCrossReference("DUO", "0000007");
        String duoUrl = postTestDuo(crUrl, null);
        policyUrl = postTestPolicy("testPolicy2", 1, dacUrl, Arrays.asList(duoUrl));

        mockMvc.perform(get(policyUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionEntityId.accession").value("testPolicy2"))
                .andExpect(jsonPath("$..self.href").value(policyUrl));

        mockMvc.perform(get(policyUrl + "/dac"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionEntityId.accession").value("testDac1"))
                .andExpect(jsonPath("$..self.href").value(dacUrl));

        mockMvc.perform(get(policyUrl + "/dataUseConditions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..duos").isArray())
                .andExpect(jsonPath("$..duos.length()").value(1))
                .andExpect(jsonPath("$..duos[0]..self.href").value(duoUrl));
    }

    @Test
    public void clientErrorWhenPostPolicyWithDuplicatedAccessions() throws Exception {
        String accession = "testpolicy";
        int version = 1;
        String contact = postTestContact("test@test.com");
        String dac = postTestDac("testdac", 1, contact);
        List<String> duos = new ArrayList<>();

        postTestPolicy(accession, version, dac, duos);

        String jsonContent = "{ " +
                "\"accessionVersionEntityId\":{ \"accession\": \"" + accession + "\",\"version\":" + version + "}," +
                "\"title\": \"" + "title" + "\"," +
                "\"center\": \"" + "center" + "\"," +
                "\"content\": \"" + "content" + "\"," +
                "\"dac\": \"" + dac + "\"," +
                "\"dataUseConditions\": " + testListJson.write(duos).getJson() +
                "}";

        mockMvc.perform(post("/policies")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());
    }

    private String postTestPolicy(String accession, int version, String dacUrl, List<String> duoUrls) throws Exception {
        if ( duoUrls == null ) {
            duoUrls = Arrays.asList();
        }

        String jsonContent = "{ " +
                "\"accessionVersionEntityId\":{ \"accession\": \"" + accession + "\",\"version\":" + version + "}," +
                "\"title\": \"" + "title" + "\"," +
                "\"center\": \"" + "center" + "\"," +
                "\"content\": \"" + "content" + "\"," +
                "\"dac\": \"" + dacUrl + "\"," +
                "\"dataUseConditions\": " + testListJson.write(duoUrls).getJson() +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/policies")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void findPolicyByAccession() throws Exception {
        String contact = postTestContact("test@test.com");
        String dacUrl = postTestDac("testDac1", 1, contact);
        String testPolicy1 = postTestPolicy("testPolicy1", 1, dacUrl, null);
        String testPolicy2 = postTestPolicy("testPolicy1", 2, dacUrl, null);
        String testPolicy3 = postTestPolicy("testPolicy3", 1, dacUrl, null);

        mockMvc.perform(get("/policies/search/accession").param("accession", "testPolicy1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..policies").isArray())
                .andExpect(jsonPath("$..policies.length()").value(1))
                .andExpect(jsonPath("$..policies[0]..policy.href").value(testPolicy2))
                .andExpect(jsonPath("$..policies[0].accessionVersionEntityId.accession").value("testPolicy1"))
                .andExpect(jsonPath("$..policies[0].accessionVersionEntityId.version").value(2));
        mockMvc.perform(get("/policies/search/accession").param("accession", "testPolicy3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..policies").isArray())
                .andExpect(jsonPath("$..policies.length()").value(1))
                .andExpect(jsonPath("$..policies[0]..policy.href").value(testPolicy3))
                .andExpect(jsonPath("$..policies[0].accessionVersionEntityId.accession").value("testPolicy3"))
                .andExpect(jsonPath("$..policies[0].accessionVersionEntityId.version").value(1));
        mockMvc.perform(get("/policies/search/accession").param("accession", "unexisted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..policies").isArray())
                .andExpect(jsonPath("$..policies.length()").value(0));
    }

}
