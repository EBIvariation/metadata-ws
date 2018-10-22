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
package uk.ac.ebi.ampt2d.metadata.rest.controllers;

import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.services.StudyService;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.resources.StudyResource;

import java.time.LocalDate;
import java.util.List;

@RestController
@Api(tags = "Study Entity")
@RequestMapping(path = "studies")
public class StudyRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private StudyService studyService;

    @Autowired
    private GenericResourceAssembler<Study, StudyResource> resourceAssembler;

    @ApiOperation(value = "Get a filtered list of studies based on filtering criteria")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "analyses.referenceSequence.name", value = "Reference Sequence's name", dataType = "string",
                    paramType = "query", example = "grch37"),
            @ApiImplicitParam(name = "analyses.referenceSequence.patch", value = "Reference Sequence's patch number", dataType = "string",
                    paramType = "query", example = "p2"),
            @ApiImplicitParam(name = "analyses.type", value = "Analysis's type", dataType = "string",
                    paramType = "query", example = "CASE_CONTROL")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<StudyResource>> search(@QuerydslPredicate(root = Study.class) Predicate predicate) {
        List<Study> studies = studyService.findStudiesByPredicate(predicate);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the latest version of a study based on accession ")
    @ApiParam(name = "accession", value = "Study's accession", type = "string", required = true, example = "EGAS0001")
    @RequestMapping(method = RequestMethod.GET, path = "search/accession", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resource<Study>> findStudiesByAccession(String accession) {
        Study study = studyService.findStudyByAccession(accession);

        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        Resource<Study> resource = resourceAssembler.toResource(study);

        return ResponseEntity.ok(resource);
    }

    @ApiOperation(value = "Get the list of studies filtered by release date")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "from", value = "starting date", dataType = "string", format = "date",
                    paramType = "query", example = "2016-01-01"),
            @ApiImplicitParam(name = "to", value = "ending date", dataType = "string", format = "date",
                    paramType = "query", example = "2018-01-01")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search/release-date", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<StudyResource>> findStudiesByReleaseDate(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<Study> studies = studyService.findStudiesByReleaseDate(from, to);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the list of studies filtered by taxonomy id")
    @ApiParam(name = "id", value = "Taxonomy's id", type = "long", required = true, example = "9606")
    @RequestMapping(method = RequestMethod.GET, path = "search/taxonomy-id", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<StudyResource>> findStudiesByTaxonomyId(long id) {
        List<Study> studies = studyService.findStudiesByTaxonomyId(id);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the list of studies filtered by taxonomy name")
    @ApiParam(name = "name", value = "Taxonomy's name", type = "string", required = true, example = "Homo sapiens")
    @RequestMapping(method = RequestMethod.GET, path = "search/taxonomy-name", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<StudyResource>> findStudiesByTaxonomyName(String name) {
        List<Study> studies = studyService.findStudiesByTaxonomyName(name);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "studySearch")
    @ApiParam(name = "searchTerm", value = "search term", type = "string", required = true, example = "human")
    @RequestMapping(method = RequestMethod.GET, path = "search/text", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<StudyResource>> getStudies(String searchTerm) {
        List<Study> studies = studyService.findStudiesByTextSearch(searchTerm);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get a list of studies linked to a given study")
    @ApiParam(name = "id", value = "StudyId", type = "string", required = true)
    @RequestMapping(method = RequestMethod.GET, path = "{id}/linkedStudies", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Resources<StudyResource>> getLinkedStudies(@PathVariable("id") long id) {
        List<Study> studies = studyService.findLinkedStudies(id);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Update an existing study. For a study that has release date in the future or has been " +
            "deprecated, it is not possible to update that study object through PATCH /studies/{id} method, it will " +
            "result in NOT FOUND. This new method (PATCH /studies/{id}/patch) allows that study object to be found and" +
            " updated.")
    @ApiParam(name = "id", value = "StudyId", type = "string", required = true)
    @RequestMapping(method = RequestMethod.PATCH, path = "{id}/patch", produces = "application/json", consumes = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resource<Study>> patch(@PathVariable("id") long id, @RequestBody String json) {
        Study study = studyService.findOneStudyById(id);

        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        try {
            Study study1 = studyService.patch(study, json);

            Resource<Study> resource = resourceAssembler.toResource(study1);

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/{id}/patch").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/accession").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/release-date").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/text").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/taxonomy-id").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/taxonomy-name").withRel("studies"));
        return resource;
    }

}
