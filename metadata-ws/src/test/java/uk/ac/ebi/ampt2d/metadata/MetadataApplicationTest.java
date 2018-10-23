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
<<<<<<< HEAD:metadata-ws/src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java
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
=======
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.*;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.*;
>>>>>>> Only multiple genes with single analysis:src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private TranscriptomeRepository transcriptomeRepository;

    @Autowired
    private GeneRepository geneRepository;

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
    private JacksonTester<Transcriptome> testTranscriptomeJson;

    @Autowired
    private JacksonTester<Gene> testGeneJson;

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
        assemblyRepository.deleteAll();
        transcriptomeRepository.deleteAll();
        geneRepository.deleteAll();
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
    public void postTranscriptome() throws Exception {
        String name = "GRCh37";
        String location = postTestTranscriptome(name, "p2", Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    private String postTestTranscriptome(String name, String patch, List<String> accessions) throws Exception {
        Transcriptome testTranscriptome = new Transcriptome(name, patch, accessions);

        MvcResult mvcResult = mockMvc.perform(post("/transcriptomes")
                .content(testTranscriptomeJson.write(testTranscriptome).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postGene() throws Exception {
        String name = "GRCh38";
        String location = postTestGene(name, "p3", Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    private String postTestGene(String name, String patch, List<String> accessions) throws Exception {
        Gene testGene = new Gene(name, patch, accessions);

        MvcResult mvcResult = mockMvc.perform(post("/genes")
                .content(testGeneJson.write(testGene).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postTaxonomy() throws Exception {
        String location = postTestTaxonomy();
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxonomyId").value(9606))
                .andExpect(jsonPath("$.name").value("Homo sapiens"));
    }

    @Test
    public void postTestTaxonomyWithIdLessThanOne() throws Exception {
        List<String> ancestors = new ArrayList<>();
        String jsonContent = "{ " +
                "\"taxonomyId\": 0," +
                "\"name\": \"Homo sapiens\"," +
                "\"ancestors\": " + testListJson.write(ancestors).getJson() + "" +
                "}";

        mockMvc.perform(post("/taxonomies")
                .content(jsonContent))
                .andExpect(status().is4xxClientError());
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

    @Test
    public void postStudy() throws Exception {
        String location = postTestStudy("EGAS0001", 1, "test_human_study");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAS0001"));
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

    @Test
    public void postAnalysis() throws Exception {
        String assemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String transcriptomeUrl = postTestTranscriptome("GRCh38", "p3",
                Arrays.asList("GCA_000001405.4", "GCF_000001405.15"));
        List<String> geneUrlList = new ArrayList<String>();
        String geneUrl = postTestGene("GRCh39", "p4",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        geneUrlList.add(geneUrl);
        geneUrl = postTestGene("GRCh40", "p5",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        geneUrlList.add(geneUrl);
        String studyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

        String location = postTestAnalysis("EGAA0001", assemblyUrl, transcriptomeUrl, geneUrlList, studyUrl, Analysis.Technology.GWAS,
                Analysis.Type.CASE_CONTROL, "Illumina");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAA0001"));
    }

    private String postTestAnalysis(String accession, String assemblyUrl, String transcriptomeUrl, List<String> geneUrlList, String studyUrl) throws Exception {
        return postTestAnalysis(accession, assemblyUrl, transcriptomeUrl, geneUrlList, studyUrl, Analysis.Technology.GWAS,
                Analysis.Type.CASE_CONTROL, "Illumina");
    }

    private String postTestAnalysis(String accession, String assemblyUrl, String transcriptomeUrl, List<String> geneUrlList,
                                    String studyUrl, Analysis.Technology technology, Analysis.Type type, String platform) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/analyses")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\": \"" + accession + "\",\"version\":  1 }," +
                        "\"name\": \"test_human_analysis\"," +
                        "\"description\": \"Nothing important\"," +
                        "\"study\": \"" + studyUrl + "\"," +
                        "\"assembly\": \"" + assemblyUrl + "\"," +
                        "\"transcriptome\": \"" + transcriptomeUrl + "\"," +
                        "\"genes\": " + testListJson.write(geneUrlList).getJson() + "," +
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
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAF0001"))
                .andExpect(jsonPath("$.accessionVersionId.version").value(1));
    }

    private String postTestFile(String accession, int version) throws Exception {
        File testFile = new File(new AccessionVersionId(accession, version), "asd123", "test_file",
                100, File.Type.TSV);

        MvcResult mvcResult = mockMvc.perform(post("/files")
                .content(testFileJson.write(testFile).getJson()))
                .andExpect(status().isCreated()).andReturn();

        return mvcResult.getResponse().getHeader("Location");
    }

    @Test
    public void postSample() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl1 = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl1);
        String taxonomyUrl2 = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl2);

        String location = postTestSample("EGAN0001", "testSample", taxonomyUrlList);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"))
                .andExpect(jsonPath("$..taxonomies.href").value(location + "/" + "taxonomies"));

        mockMvc.perform(get(location+ "/" + "taxonomies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(2))
                .andExpect(jsonPath("$..taxonomies[0]..taxonomy.href").value(taxonomyUrl1))
                .andExpect(jsonPath("$..taxonomies[1]..taxonomy.href").value(taxonomyUrl2));
    }

    @Test
    public void postSampleAndUpdate() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl1 = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl1);
        String taxonomyUrl2 = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl2);

        String location = postTestSample("EGAN0001", "testSample", taxonomyUrlList);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"))
                .andExpect(jsonPath("$..taxonomies.href").value(location + "/" + "taxonomies"));

        mockMvc.perform(get(location+ "/" + "taxonomies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(2))
                .andExpect(jsonPath("$..taxonomies[0]..taxonomy.href").value(taxonomyUrl1))
                .andExpect(jsonPath("$..taxonomies[1]..taxonomy.href").value(taxonomyUrl2));

        List<String> taxonomyUrlListNew = new ArrayList<String>();
        String taxonomyUrl3 = postTestTaxonomy(3, "Species3");
        taxonomyUrlListNew.add(taxonomyUrl3);

        mockMvc.perform(patch(location)
                .content("{ " +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlListNew).getJson() + "" +
                        "}"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(location+ "/" + "taxonomies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(1))
                .andExpect(jsonPath("$..taxonomies[0]..taxonomy.href").value(taxonomyUrl3));
    }

    @Test
    public void postSampleInvalidNoTaxonomies() throws Exception {
        mockMvc.perform(post("/samples")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\": \"" + "species1" + "\",\"version\": " + 1 + "}," +
                        "\"name\": \"" + "test sample" + "\"" +
                        "}"))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("exception").value("uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException"));
    }

    @Test
    public void postSampleInvalidBlankTaxonomies() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<>();
        mockMvc.perform(post("/samples")
                .content("{ " +
                        "\"accessionVersionId\":{ \"accession\": \"" + "species1" + "\",\"version\": " + 1 + "}," +
                        "\"name\": \"" + "test sample" + "\"," +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlList).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("exception").value("uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException"));
    }

    @Test
    public void deleteSampleTaxonomies() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl1 = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl1);
        String taxonomyUrl2 = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl2);

        String location = postTestSample("EGAN0001", "testSample", taxonomyUrlList);
        mockMvc.perform(get(location))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$..sample.href").value(location));

        String idStr = taxonomyUrl1.substring(taxonomyUrl1.lastIndexOf('/') + 1);
        mockMvc.perform(delete(location + "/taxonomies/" + idStr))
                .andExpect(status().is2xxSuccessful());
        idStr = taxonomyUrl2.substring(taxonomyUrl2.lastIndexOf('/') + 1);
        mockMvc.perform(delete(location + "/taxonomies/" + idStr))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("exception").value("uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException"));
    }

    @Test
    public void postSampleAndUpdateWithoutTaxonomies() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl1 = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl1);
        String taxonomyUrl2 = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl2);

        String location = postTestSample("EGAN0001", "testSample", taxonomyUrlList);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"))
                .andExpect(jsonPath("$..taxonomies.href").value(location + "/" + "taxonomies"));

        mockMvc.perform(get(location+ "/" + "taxonomies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(2))
                .andExpect(jsonPath("$..taxonomies[0]..taxonomy.href").value(taxonomyUrl1))
                .andExpect(jsonPath("$..taxonomies[1]..taxonomy.href").value(taxonomyUrl2));

        List<String> taxonomyUrlListInvalid = new ArrayList<String>();
        mockMvc.perform(patch(location)
                .content("{ " +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlListInvalid).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("exception").value("uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException"));
    }

    @Test
    public void postSampleAndUpdateInvalidTaxonomies() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl1 = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl1);
        String taxonomyUrl2 = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl2);

        String location = postTestSample("EGAN0001", "testSample", taxonomyUrlList);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"))
                .andExpect(jsonPath("$..taxonomies.href").value(location + "/" + "taxonomies"));

        mockMvc.perform(get(location+ "/" + "taxonomies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(2))
                .andExpect(jsonPath("$..taxonomies[0]..taxonomy.href").value(taxonomyUrl1))
                .andExpect(jsonPath("$..taxonomies[1]..taxonomy.href").value(taxonomyUrl2));

        List<String> taxonomyUrlListInvalid = new ArrayList<String>();
        taxonomyUrlListInvalid.add("http://nohost/taxonomies/9998");
        taxonomyUrlListInvalid.add("http://nohost/taxonomies/9999");
        mockMvc.perform(patch(location)
                .content("{ " +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlListInvalid).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("exception").value("uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException"));
    }

    @Test
    public void postSampleAndUpdatePartialInvalidTaxonomies() throws Exception {
        List<String> taxonomyUrlList = new ArrayList<String>();
        String taxonomyUrl1 = postTestTaxonomy(1, "Species1");
        taxonomyUrlList.add(taxonomyUrl1);
        String taxonomyUrl2 = postTestTaxonomy(2, "Species2");
        taxonomyUrlList.add(taxonomyUrl2);

        String location = postTestSample("EGAN0001", "testSample", taxonomyUrlList);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAN0001"))
                .andExpect(jsonPath("$.name").value("testSample"))
                .andExpect(jsonPath("$..taxonomies.href").value(location + "/" + "taxonomies"));

        mockMvc.perform(get(location+ "/" + "taxonomies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..taxonomies").isArray())
                .andExpect(jsonPath("$..taxonomies.length()").value(2))
                .andExpect(jsonPath("$..taxonomies[0]..taxonomy.href").value(taxonomyUrl1))
                .andExpect(jsonPath("$..taxonomies[1]..taxonomy.href").value(taxonomyUrl2));

        List<String> taxonomyUrlListMixed = new ArrayList<String>();
        taxonomyUrlListMixed.add(taxonomyUrl1);
        taxonomyUrlListMixed.add("http://nohost/taxonomies/9999");
        mockMvc.perform(patch(location)
                .content("{ " +
                        "\"taxonomies\": " + testListJson.write(taxonomyUrlListMixed).getJson() + "" +
                        "}"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("exception").value("uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException"));
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
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search?accessions=GCF_000001405.28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(1))
                .andExpect(jsonPath("$..assemblies[0]..assembly.href").value(grch38Url))
                .andExpect(jsonPath("$..assemblies[0].accessions").isArray())
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItems("GCF_000001405.28")));

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
                .andExpect(jsonPath("$..assemblies[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/assemblies/search?name=GRCh37&patch=p3&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..assemblies").isArray())
                .andExpect(jsonPath("$..assemblies.length()").value(0));
    }

    @Test
    public void findTranscriptomeByName() throws Exception {
        String grch37Url = postTestTranscriptome("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestTranscriptome("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch37Url))
                .andExpect(jsonPath("$..transcriptomes[0].name").value("GRCh37"));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch38Url))
                .andExpect(jsonPath("$..transcriptomes[0].name").value("GRCh38"));

        mockMvc.perform(get("/transcriptomes/search?name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(0));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh37&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch37Url))
                .andExpect(jsonPath("$..transcriptomes[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..transcriptomes[0].patch").value("p2"));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh38&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch38Url))
                .andExpect(jsonPath("$..transcriptomes[0].name").value("GRCh38"))
                .andExpect(jsonPath("$..transcriptomes[0].patch").value("p2"));

        mockMvc.perform(get("/transcriptomes/search?name=NCBI36&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(0));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh37&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(0));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh38&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(0));

        mockMvc.perform(get("/transcriptomes/search?accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch37Url))
                .andExpect(jsonPath("$..transcriptomes[0].accessions").isArray())
                .andExpect(jsonPath("$..transcriptomes[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/transcriptomes/search?accessions=GCF_000001405.28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch38Url))
                .andExpect(jsonPath("$..transcriptomes[0].accessions").isArray())
                .andExpect(jsonPath("$..transcriptomes[0].accessions[*]", hasItems("GCF_000001405.28")));

        mockMvc.perform(get("/transcriptomes/search?accessions=GCA_000001405.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(0));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh37&patch=p2&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(1))
                .andExpect(jsonPath("$..transcriptomes[0]..transcriptome.href").value(grch37Url))
                .andExpect(jsonPath("$..transcriptomes[0].accessions").isArray())
                .andExpect(jsonPath("$..transcriptomes[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..transcriptomes[0].patch").value("p2"))
                .andExpect(jsonPath("$..transcriptomes[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/transcriptomes/search?name=GRCh37&patch=p3&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..transcriptomes").isArray())
                .andExpect(jsonPath("$..transcriptomes.length()").value(0));
    }

    @Test
    public void findGeneByName() throws Exception {
        String grch37Url = postTestGene("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38Url = postTestGene("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));

        mockMvc.perform(get("/genes/search?name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch37Url))
                .andExpect(jsonPath("$..genes[0].name").value("GRCh37"));

        mockMvc.perform(get("/genes/search?name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch38Url))
                .andExpect(jsonPath("$..genes[0].name").value("GRCh38"));

        mockMvc.perform(get("/genes/search?name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(0));

        mockMvc.perform(get("/genes/search?name=GRCh37&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch37Url))
                .andExpect(jsonPath("$..genes[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..genes[0].patch").value("p2"));

        mockMvc.perform(get("/genes/search?name=GRCh38&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch38Url))
                .andExpect(jsonPath("$..genes[0].name").value("GRCh38"))
                .andExpect(jsonPath("$..genes[0].patch").value("p2"));

        mockMvc.perform(get("/genes/search?name=NCBI36&patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(0));

        mockMvc.perform(get("/genes/search?name=GRCh37&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(0));

        mockMvc.perform(get("/genes/search?name=GRCh38&patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(0));

        mockMvc.perform(get("/genes/search?accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch37Url))
                .andExpect(jsonPath("$..genes[0].accessions").isArray())
                .andExpect(jsonPath("$..genes[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/genes/search?accessions=GCF_000001405.28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch38Url))
                .andExpect(jsonPath("$..genes[0].accessions").isArray())
                .andExpect(jsonPath("$..genes[0].accessions[*]", hasItems("GCF_000001405.28")));

        mockMvc.perform(get("/genes/search?accessions=GCA_000001405.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(0));

        mockMvc.perform(get("/genes/search?name=GRCh37&patch=p2&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(1))
                .andExpect(jsonPath("$..genes[0]..gene.href").value(grch37Url))
                .andExpect(jsonPath("$..genes[0].accessions").isArray())
                .andExpect(jsonPath("$..genes[0].name").value("GRCh37"))
                .andExpect(jsonPath("$..genes[0].patch").value("p2"))
                .andExpect(jsonPath("$..genes[0].accessions[*]", hasItems("GCA_000001405.3")));

        mockMvc.perform(get("/genes/search?name=GRCh37&patch=p3&accessions=GCA_000001405.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..genes").isArray())
                .andExpect(jsonPath("$..genes.length()").value(0));
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
        String humanAssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String humanTranscriptomeUrl = postTestTranscriptome("GRCh38", "p3",
                Arrays.asList("GCA_000001405.4", "GCF_000001405.15"));
        List<String> humanGeneUrlList = new ArrayList<String>();
        String humanGeneUrl = postTestGene("GRCh39", "p4",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        humanGeneUrlList.add(humanGeneUrl);
        humanGeneUrl = postTestGene("GRCh40", "p5",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        humanGeneUrlList.add(humanGeneUrl);

        String humanStudyUrl = postTestStudy("EGAS0001", 1, "test_human_study");

<<<<<<< HEAD:metadata-ws/src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java
        return Arrays.asList(postTestAnalysis("EGAA0001", humanReferenceSequenceUrl, humanStudyUrl,
                Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina"),
                postTestAnalysis("EGAA0002", humanReferenceSequenceUrl, humanStudyUrl,
                        Analysis.Technology.ARRAY, Analysis.Type.TUMOR, "PacBio"));
=======
        return Arrays.asList(postTestAnalysis("EGAA0001", humanAssemblyUrl, humanTranscriptomeUrl, humanGeneUrlList,
                            humanStudyUrl, Analysis.Technology.GWAS, Analysis.Type.CASE_CONTROL, "Illumina"),
                postTestAnalysis("EGAA0002", humanAssemblyUrl, humanTranscriptomeUrl, humanGeneUrlList, humanStudyUrl,
                                Analysis.Technology.ARRAY, Analysis.Type.TUMOR, "PacBio"));
>>>>>>> Only multiple genes with single analysis:src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java
    }

    @Test
    public void clientErrorWhenSearchAnalysesWithInvalidType() throws Exception {
        mockMvc.perform(get("/analyses/search?type=unknown"))
                .andExpect(status().is4xxClientError());
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
    public void findStudies() throws Exception {
<<<<<<< HEAD:metadata-ws/src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java
        String taxonomyUrl = postTestTaxonomy();
        String grch37ReferenceSequenceUrl = postTestReferenceSequence("GRCh37", "p2",
=======
        String grch37AssemblyUrl = postTestAssembly("GRCh37", "p2",
>>>>>>> Only multiple genes with single analysis:src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String grch38AssemblyUrl = postTestAssembly("GRCh38", "p2",
                Arrays.asList("GCA_000001405.17", "GCF_000001405.28"));
<<<<<<< HEAD:metadata-ws/src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java
        String grch37StudyUrl = postTestStudy("EGAS0001", 1, "test_human_study", taxonomyUrl);
        String grch38StudyUrl = postTestStudy("EGAS0001", 2, "test_human_study", taxonomyUrl);
=======
        String grch39TranscriptomeUrl = postTestTranscriptome("GRCh39", "p3",
                Arrays.asList("GCA_000001405.4", "GCF_000001405.15"));
        String grch40TranscriptomeUrl = postTestTranscriptome("GRCh40", "p3",
                Arrays.asList("GCA_000001405.18", "GCF_000001405.29"));
        List<String> grch41GeneUrlList = new ArrayList<String>();
        String grch41GeneUrl = postTestGene("GRCh41", "p4",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        grch41GeneUrlList.add(grch41GeneUrl);
        grch41GeneUrl = postTestGene("GRCh41", "p4",
                Arrays.asList("GCA_000001405.6", "GCF_000001405.17"));
        grch41GeneUrlList.add(grch41GeneUrl);
        List<String> grch42GeneUrlList = new ArrayList<String>();
        String grch42GeneUrl = postTestGene("GRCh42", "p4",
                Arrays.asList("GCA_000001405.19", "GCF_000001405.30"));
        grch42GeneUrlList.add(grch42GeneUrl);
        grch42GeneUrl = postTestGene("GRCh42", "p4",
                Arrays.asList("GCA_000001405.20", "GCF_000001405.31"));
        grch42GeneUrlList.add(grch42GeneUrl);

        String grchStudyUrlv1 = postTestStudy("EGAS0001", 1, "test_human_study");
        String grchStudyUrlv2 = postTestStudy("EGAS0001", 2, "test_human_study");

        postTestAnalysis("EGAA0001", grch37AssemblyUrl, grch39TranscriptomeUrl, grch41GeneUrlList, grchStudyUrlv1);
        postTestAnalysis("EGAA0002", grch38AssemblyUrl, grch40TranscriptomeUrl, grch42GeneUrlList, grchStudyUrlv2);

        // Assembly related tests
        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));
>>>>>>> Only multiple genes with single analysis:src/test/java/uk/ac/ebi/ampt2d/metadata/MetadataApplicationTest.java

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37&analyses.assembly.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.assembly.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.assembly.name=NCBI36&analyses.assembly.patch=p2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh37&analyses.assembly.patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.assembly.name=GRCh38&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

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

        // Transcriptome related tests
        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh39"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh39&analyses.transcriptome.patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh40&analyses.transcriptome.patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=NCBI36&analyses.transcriptome.patch=p3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh39&analyses.transcriptome.patch=p4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh40&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh40&analyses.type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=GRCh40&analyses.type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.transcriptome.name=NCBI36&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        // Gene related tests
        mockMvc.perform(get("/studies?analyses.genes.name=GRCh41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.genes.name=NCBI36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh41&analyses.genes.patch=p4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh42&analyses.genes.patch=p4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.genes.name=NCBI36&analyses.genes.patch=p4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh41&analyses.genes.patch=p5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh42&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh42&analyses.type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.genes.name=GRCh42&analyses.type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.genes.name=NCBI36&analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        // Type related tests
        mockMvc.perform(get("/studies?analyses.type=CASE_CONTROL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0]..study.href").value(grchStudyUrlv1))
                .andExpect(jsonPath("$..studies[1]..study.href").value(grchStudyUrlv2));

        mockMvc.perform(get("/studies?analyses.type=TUMOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));

        mockMvc.perform(get("/studies?analyses.type=COLLECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void searchStudy() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        postTestStudy("EGAS0001", 1, "test human study based on GRCh37", taxonomyUrl);
        postTestStudy("EGAS0001", 2, "test human study based on GRCh37", taxonomyUrl);
        postTestStudy("EGAS0002", 1, "test human study based on GRCh38", taxonomyUrl);

        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAS0001"))
                .andExpect(jsonPath("$.accessionVersionId.version").value(2));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "human"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(3));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(3));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "grCh37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(2))
                .andExpect(jsonPath("$..studies[0].accessionVersionId.accession").value("EGAS0001"));
        mockMvc.perform(get("/studies/search/text").param("searchTerm", "GrCh39"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..studies.length()").value(0));
    }

    @Test
    public void searchStudyByAccession() throws Exception {
        String taxonomyUrl = postTestTaxonomy();
        String testStudy1 = postTestStudy("EGAS0001", 1, "test human study based on GRCh37", taxonomyUrl);
        String testStudy2 = postTestStudy("EGAS0001", 2, "test human study based on GRCh38", taxonomyUrl);
        String testStudy3 = postTestStudy("EGAS0002", 3, "test human study based on GRCh38", taxonomyUrl);

        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(testStudy2))
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAS0001"))
                .andExpect(jsonPath("$.accessionVersionId.version").value(2));
        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..study.href").value(testStudy3))
                .andExpect(jsonPath("$.accessionVersionId.accession").value("EGAS0002"))
                .andExpect(jsonPath("$.accessionVersionId.version").value(3));
        mockMvc.perform(get("/studies/search/accession").param("accession", "EGAS0003"))
                .andExpect(status().isNotFound());
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
        String testAssembly = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String testTranscriptome = postTestTranscriptome("GRCh38", "p3",
                Arrays.asList("GCA_000001405.4", "GCF_000001405.15"));
        List<String> testGeneList = new ArrayList<String>();
        String testGene = postTestGene("GRCh39", "p4",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        testGeneList.add(testGene);
        testGene = postTestGene("GRCh39", "p4",
                Arrays.asList("GCA_000001405.6", "GCF_000001405.17"));
        testGeneList.add(testGene);

        String testTaxonomy = postTestTaxonomy(9606, "Homo sapiens");
        String testStudy = postTestStudy("testhuman", 1, "test human study", testTaxonomy);
        String testAnalysis = postTestAnalysis("testhuman", testAssembly, testTranscriptome, testGeneList, testStudy);
        String testFile = postTestFile("testhuman", 1);
        String testSample = postTestSample("testhuman", "test human sample");
        String testWebResource = postTestWebResource();
        ZonedDateTime endTime = ZonedDateTime.now();

        checkLastModifiedDate(testAssembly, "assembly", startTime, endTime);
        checkLastModifiedDate(testTranscriptome, "transcriptome", startTime, endTime);
        checkLastModifiedDate(testGene, "gene", startTime, endTime);
        checkLastModifiedDate(testTaxonomy, "taxonomy", startTime, endTime);
        checkLastModifiedDate(testStudy, "study", startTime, endTime);
        checkLastModifiedDate(testAnalysis, "analysis", startTime, endTime);
        checkLastModifiedDate(testFile, "file", startTime, endTime);
        checkLastModifiedDate(testSample, "sample", startTime, endTime);
        checkLastModifiedDate(testWebResource, "webResource", startTime, endTime);

        startTime = ZonedDateTime.now();
        patchResource(testAssembly);
        patchResource(testTranscriptome);
        patchResource(testGene);
        patchResource(testTaxonomy);
        patchResource(testStudy);
        patchResource(testAnalysis);
        patchResource(testFile);
        patchResource(testSample);
        mockMvc.perform(patch(testWebResource)
                .content("{\"resourceUrl\": \"http://nothing.important.com\"}"))
                .andExpect(status().is2xxSuccessful());
        endTime = ZonedDateTime.now();

        checkLastModifiedDate(testAssembly, "assembly", startTime, endTime);
        checkLastModifiedDate(testTranscriptome, "transcriptome", startTime, endTime);
        checkLastModifiedDate(testGene, "gene", startTime, endTime);
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
        String humanAssemblyUrl = postTestAssembly("GRCh37", "p2",
                Arrays.asList("GCA_000001405.3", "GCF_000001405.14"));
        String humanTranscriptomeUrl = postTestTranscriptome("GRCh38", "p3",
                Arrays.asList("GCA_000001405.4", "GCF_000001405.15"));
        List<String> humanGeneUrlList = new ArrayList<String>();
        String humanGeneUrl = postTestGene("GRCh39", "p4",
                Arrays.asList("GCA_000001405.5", "GCF_000001405.16"));
        humanGeneUrlList.add(humanGeneUrl);
        humanGeneUrl = postTestGene("GRCh39", "p4",
                Arrays.asList("GCA_000001405.6", "GCF_000001405.17"));
        humanGeneUrlList.add(humanGeneUrl);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        String yesterdayReleasedStudyUrl = postTestStudy("1kg", 1, "1kg pilot", humanTaxonomyUrl, yesterday);
        String todayReleasedStudyUrl = postTestStudy("1kg", 2, "1kg phase 1", humanTaxonomyUrl, today);
        String tomorrowReleasedStudyUrl = postTestStudy("1kg", 3, "1kg phase 3", humanTaxonomyUrl, tomorrow);

        String yesterdayReleasedAnalysisUrl = postTestAnalysis("analysisReleasedYesterday", humanAssemblyUrl, humanTranscriptomeUrl, humanGeneUrlList, yesterdayReleasedStudyUrl);

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

    @Test
    public void findSampleByTaxonomyName() throws Exception {
        String homininesTaxonomyUrl = postTestTaxonomy(207598, "Homininae");
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        List<String> taxonomiesUrlList1 = Arrays.asList(homininesTaxonomyUrl, humanTaxonomyUrl);
        String bonoboTaxonomyUrl = postTestTaxonomy(9597, "Pan paniscus");
        String chimpanzeeTaxonomyUrl = postTestTaxonomy(9598, "Pan troglodytes");
        List<String> taxonomiesUrlList2 = Arrays.asList(bonoboTaxonomyUrl, chimpanzeeTaxonomyUrl);

        String sampleUrl1 = postTestSample("Species1", "Species collection1", taxonomiesUrlList1);
        String sampleUrl2 = postTestSample("Species2", "Species collection2", taxonomiesUrlList2);
        String sampleUrl3 = postTestSample("Species3", "Species collection3",
                Arrays.asList(homininesTaxonomyUrl, bonoboTaxonomyUrl));

        mockMvc.perform(get("/samples/search?taxonomies.name=Homininae"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(2))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl1))
                .andExpect(jsonPath("$..samples[1]..sample.href").value(sampleUrl3));

        mockMvc.perform(get("/samples/search?taxonomies.name=Homo sapiens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(1))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl1));

        mockMvc.perform(get("/samples/search?taxonomies.name=Pan paniscus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(2))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl2))
                .andExpect(jsonPath("$..samples[1]..sample.href").value(sampleUrl3));

        mockMvc.perform(get("/samples/search?taxonomies.name=Pan troglodytes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(1))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl2));

        mockMvc.perform(get("/samples/search?taxonomies.name=NonExisting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(0));
    }

    @Test
    public void findSampleByTaxonomyId() throws Exception {
        String homininesTaxonomyUrl = postTestTaxonomy(207598, "Homininae");
        String humanTaxonomyUrl = postTestTaxonomy(9606, "Homo sapiens");
        List<String> taxonomiesUrlList1 = Arrays.asList(homininesTaxonomyUrl, humanTaxonomyUrl);
        String bonoboTaxonomyUrl = postTestTaxonomy(9597, "Pan paniscus");
        String chimpanzeeTaxonomyUrl = postTestTaxonomy(9598, "Pan troglodytes");
        List<String> taxonomiesUrlList2 = Arrays.asList(bonoboTaxonomyUrl, chimpanzeeTaxonomyUrl);

        String sampleUrl1 = postTestSample("Species1", "Species collection1", taxonomiesUrlList1);
        String sampleUrl2 = postTestSample("Species2", "Species collection2", taxonomiesUrlList2);
        String sampleUrl3 = postTestSample("Species3", "Species collection3",
                Arrays.asList(homininesTaxonomyUrl, bonoboTaxonomyUrl));


        mockMvc.perform(get("/samples/search?taxonomies.taxonomyId=207598"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(2))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl1))
                .andExpect(jsonPath("$..samples[1]..sample.href").value(sampleUrl3));

        mockMvc.perform(get("/samples/search?taxonomies.taxonomyId=9606"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(1))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl1));

        mockMvc.perform(get("/samples/search?taxonomies.taxonomyId=9597"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(2))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl2))
                .andExpect(jsonPath("$..samples[1]..sample.href").value(sampleUrl3));

        mockMvc.perform(get("/samples/search?taxonomies.taxonomyId=9598"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(1))
                .andExpect(jsonPath("$..samples[0]..sample.href").value(sampleUrl2));

        mockMvc.perform(get("/samples/search?taxonomies.taxonomyId=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..samples").isArray())
                .andExpect(jsonPath("$..samples.length()").value(0));
    }
}
