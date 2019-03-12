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
import org.junit.Ignore;
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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.File;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.FileRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
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
    private ReferenceSequenceRepository referenceSequenceRepository;

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
        referenceSequenceRepository.deleteAll();
        fileRepository.deleteAll();
        sampleRepository.deleteAll();
        studyRepository.deleteAll();
        taxonomyRepository.deleteAll();
        webResourceRepository.deleteAll();
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

    private String postTestTaxonomy() throws Exception {
        return postTestTaxonomy(9606, "Homo sapiens");
    }

    private String postTestTaxonomy(long id, String name) throws Exception {
        return postTestTaxonomy(id, name, new ArrayList<>());
    }

    private String postTestTaxonomy(long id, String name, List<String> ancestors) throws Exception {
        String jsonContent = "{ " +
                "\"taxonomyId\": " + Long.toString(id) + "," +
                "\"name\": \"" + name + "\"," +
                "\"ancestors\": " + testListJson.write(ancestors).getJson() + "" +
                "}";

        MvcResult mvcResult = mockMvc.perform(post("/taxonomies")
                .content(jsonContent))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
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
                        "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\": " + version + "}," +
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


    private String postTestAnalysis(String accession, List<String> referenceSequenceList, String studyUrl) throws Exception {
        return postTestAnalysis(accession, referenceSequenceList, studyUrl, Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina");

    }

    private String postTestAnalysis(String accession, List<String> referenceSequenceList, String studyUrl, Analysis.Technology
            technology, Analysis.Type type, String platform) throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + studyUrl + "\"," +
                        "\"referenceSequences\": " + testListJson.write(referenceSequenceList).getJson() + "," +
                        "\"technology\": \"" + technology + "\"," +
                        "\"type\": \"" + type + "\"," +
                        "\"platform\": \"" + platform + "\"" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    private String postTestFile(String accession, int version) throws Exception {
        File testFile = new File(new AccessionVersionId(accession, version), "asd123", "test_file",
                100, File.Type.TSV);

        MvcResult mvcResult = mockMvc.perform(post("/files")
                .content(testFileJson.write(testFile).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    private String postTestSample(String accession, String name) throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl);
        taxonomyUrl = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl);

        return postTestSample(accession, name, taxonomyUrlList);
    }

    private String postTestSample(String accession, String name, List<String> taxonomyUrlList) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/samples")
        .content("{ " +
                "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\": " + 1 + "}," +
                "\"name\": \"" + name + "\"," +
                "\"taxonomies\": " + testListJson.write(taxonomyUrlList).getJson() + "" +
                "}"))
        .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    private String postTestWebResource() throws Exception {
        WebResource testWebResource = new WebResource(WebResource.Type.CENTER_WEB, "http:\\www.ebi.ac.uk");

        MvcResult mvcResult = mockMvc.perform(post("/webResources")
                .content(testWebResourceJson.write(testWebResource).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
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

    private List<String> postTestAnalyses() throws Exception {
        String humanReferenceSequenceUrl = postTestReferenceSequence("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String humanStudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        List<String> referenceSequenceList = new ArrayList<>();
        referenceSequenceList.add(humanReferenceSequenceUrl);
        return Arrays.asList(postTestAnalysis("EGAA0001", referenceSequenceList, humanStudyUrl,
                Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina"),
                postTestAnalysis("EGAA0002", referenceSequenceList, humanStudyUrl,
                        Analysis.Technology.ARRAY, Analysis.Type.TUMOR, "PacBio"));
    }

    @Test
    public void testAccessionVersionIdPost() throws Exception {
        //AccessionVersionId can be null but in case of provided values,accession and version should be valid.
        String homininesTaxonomyUrl = postTestTaxonomy(207598, "Homininae");
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        List<String> taxonomyUrlList = Arrays.asList(homininesTaxonomyUrl, humanTaxonomyUrl);

        // no accession
        MvcResult mvcResult = mockMvc.perform(post("/samples")
                .content("{ " +
                        "\"name\": \"" + "Sample1" + "\"," +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlList).getJson() + "" +
                        "}"))
                .andExpect(status().isCreated()).andReturn();
        String testSampleUrl = mvcResult.getResponse().getHeader("Location");
        mockMvc.perform(get(testSampleUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.accessionVersionId").value(nullValue()));

        // null accession
        mockMvc.perform(post("/samples")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\":" + null + ",\"version\": " + 1 + "}," +
                        "\"name\": \"" + "Sample1" + "\"," +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlList).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("accessionVersionId.accession"))
                .andExpect(jsonPath("$.errors[0].message").value("may not be null"));

        // blank accession
        mockMvc.perform(post("/samples")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\": \"" + "\",\"version\": " + 1 + "}," +
                        "\"name\": \"" + "Sample1" + "\"," +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlList).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("accessionVersionId.accession"))
                .andExpect(jsonPath("$.errors[0].message").value("size must be between 1 and 255"));

        // 0 version
        mockMvc.perform(post("/samples")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\": \"" + "EGAN0001" + "\",\"version\": " + 0 + "}," +
                        "\"name\": \"" + "Sample1" + "\"," +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlList).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors[0].property").value("accessionVersionId.version"))
                .andExpect(jsonPath("$.errors[0].message").value("must be greater than or equal to 1"));
    }

    @Test
    public void testAccessionValidation() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        postTestStudy("EGAS0001", 1, "test_study", taxonomyUrl);
        mockMvc.perform(get("/studies?accessionVersionId=EGAS0001")).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$" + ".message")
                        .value("Please provide an ID in the form accession.version"));
        mockMvc.perform(get("/studies?accessionVersionId=EGAS0001.S1")).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message")
                        .value("Please provide an ID in the form accession.version"));
        mockMvc.perform(get("/studies?accessionVersionId=EGAS0001.1")).andExpect(status().isOk())
                .andExpect(jsonPath("$..studies[0].accessionVersionId.accession").value("EGAS0001"));
        mockMvc.perform(get("/studies?accessionVersionId=EGAS0001.2")).andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void testSearchByAccessionVersionId() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        postTestStudy("EGAS0001", 1, "test_study", taxonomyUrl);
        postTestFile("EGAF0001", 1);
        postTestSample("EGAN0001", "Sample1");
        mockMvc.perform(get("/studies?accessionVersionId=EGAS0001.1")).andExpect(status().isOk())
                .andExpect(jsonPath("$..studies[0].accessionVersionId.accession").value("EGAS0001"));
        mockMvc.perform(get("/files?accessionVersionId=EGAF0001.1")).andExpect(status().isOk())
                .andExpect(jsonPath("$..files[0].accessionVersionId.accession").value("EGAF0001"));
        mockMvc.perform(get("/samples?accessionVersionId=EGAN0001.1")).andExpect(status().isOk())
                .andExpect(jsonPath("$..samples[0].accessionVersionId.accession").value("EGAN0001"));
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

        mockMvc.perform(get("/studies/search?taxonomy.taxonomyId=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(undeprecatedStudyUrl));

        mockMvc.perform(get("/studies/search/accession?accession=1kg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("1kg"))
                .andExpect(jsonPath("$.accessionVersionId.version").value(2))
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
        List<String> referenceSequenceList = new ArrayList<>();
        referenceSequenceList.add(testReferenceSequence);
        String testAnalysis = postTestAnalysis("testhuman", referenceSequenceList, testStudy);
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

        List<String> referenceSequenceList = new ArrayList<>();
        referenceSequenceList.add(humanReferenceSequenceUrl);
        String yesterdayReleasedAnalysisUrl = postTestAnalysis("analysisReleasedYesterday", referenceSequenceList, yesterdayReleasedStudyUrl);

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

        mockMvc.perform(get("/studies/search?taxonomy.taxonomyId=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(yesterdayReleasedStudyUrl))
                .andExpect(jsonPath("$..studies[1]..study.href").value(todayReleasedStudyUrl));

        mockMvc.perform(get("/studies/search/accession?accession=1kg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("1kg"))
                .andExpect(jsonPath("$.accessionVersionId.version").value(2))
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
        String taxonomyUrl = postTestTaxonomy();
        String humanStudyUrlB = postTestStudy("EGAS0001", 1, "test human B", taxonomyUrl);
        String humanStudyUrlA = postTestStudy("EGAS0002", 1, "test human A", taxonomyUrl);

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

}
