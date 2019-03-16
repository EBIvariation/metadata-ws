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

}
