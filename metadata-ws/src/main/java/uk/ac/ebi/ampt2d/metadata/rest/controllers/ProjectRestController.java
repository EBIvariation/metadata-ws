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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Project;
import uk.ac.ebi.ampt2d.metadata.persistence.services.ProjectService;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.resources.ProjectResource;

import java.time.LocalDate;
import java.util.List;

@RestController
@Api(tags = "Project Entity")
@RequestMapping(path = "projects")
public class ProjectRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private GenericResourceAssembler<Project, ProjectResource> resourceAssembler;

    @ApiOperation(value = "Get a filtered list of projects based on filtering criteria")
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
    public ResponseEntity<Resources<ProjectResource>> search(@QuerydslPredicate(root = Project.class) Predicate predicate) {
        List<Project> projects = projectService.findProjectsByPredicate(predicate);

        Resources<ProjectResource> resources = (Resources<ProjectResource>) resourceAssembler.toResources(Project.class, projects);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the latest version of a project based on accession ")
    @ApiParam(name = "accession", value = "Project's accession", type = "string", required = true, example = "EGAS0001")
    @RequestMapping(method = RequestMethod.GET, path = "search/accession", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resource<Project>> findProjectsByAccession(String accession) {
        Project project = projectService.findProjectByAccession(accession);

        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        Resource<Project> resource = resourceAssembler.toResource(project);

        return ResponseEntity.ok(resource);
    }

    @ApiOperation(value = "Get the list of projects filtered by release date")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "from", value = "starting date", dataType = "string", format = "date",
                    paramType = "query", example = "2016-01-01"),
            @ApiImplicitParam(name = "to", value = "ending date", dataType = "string", format = "date",
                    paramType = "query", example = "2018-01-01")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search/release-date", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ProjectResource>> findProjectsByReleaseDate(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<Project> projects = projectService.findProjectsByReleaseDate(from, to);

        Resources<ProjectResource> resources = (Resources<ProjectResource>) resourceAssembler.toResources(Project.class, projects);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the list of projects filtered by taxonomy id")
    @ApiParam(name = "id", value = "Taxonomy's id", type = "long", required = true, example = "9606")
    @RequestMapping(method = RequestMethod.GET, path = "search/taxonomy-id", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ProjectResource>> findProjectsByTaxonomyId(long id) {
        List<Project> projects = projectService.findProjectsByTaxonomyId(id);

        Resources<ProjectResource> resources = (Resources<ProjectResource>) resourceAssembler.toResources(Project.class, projects);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the list of projects filtered by taxonomy name")
    @ApiParam(name = "name", value = "Taxonomy's name", type = "string", required = true, example = "Homo sapiens")
    @RequestMapping(method = RequestMethod.GET, path = "search/taxonomy-name", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ProjectResource>> findProjectsByTaxonomyName(String name) {
        List<Project> projects = projectService.findProjectsByTaxonomyName(name);

        Resources<ProjectResource> resources = (Resources<ProjectResource>) resourceAssembler.toResources(Project.class, projects);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "projectSearch")
    @ApiParam(name = "searchTerm", value = "search term", type = "string", required = true, example = "human")
    @RequestMapping(method = RequestMethod.GET, path = "search/text", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ProjectResource>> getProjects(String searchTerm) {
        List<Project> projects = projectService.findProjectsByTextSearch(searchTerm);

        Resources<ProjectResource> resources = (Resources<ProjectResource>) resourceAssembler.toResources(Project.class, projects);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get a list of projects linked to a given project")
    @ApiParam(name = "id", value = "Project's id", type = "long", required = true)
    @RequestMapping(method = RequestMethod.GET, path = "{id}/linkedProjects", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Resources<ProjectResource>> getLinkedProjects(@PathVariable("id") long id) {
        List<Project> projects = projectService.findLinkedProjects(id);

        Resources<ProjectResource> resources = (Resources<ProjectResource>) resourceAssembler.toResources(Project.class, projects);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Update an existing project. For a project that has been deprecated, it is not possible " +
            "to update that project object through PATCH /projects/{id} method, it will " +
            "result in NOT FOUND. This new method (PATCH /projects/{id}/patch) allows that project object to be found and" +
            " updated.")
    @ApiParam(name = "id", value = "Project's id", type = "long", required = true)
    @RequestMapping(method = RequestMethod.PATCH, path = "{id}/patch", produces = "application/json", consumes = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resource<Project>> patch(@PathVariable("id") long id, @RequestBody String json) {
        Project project = projectService.findOneProjectById(id);

        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Project project1 = projectService.patch(project, json);

            Resource<Project> resource = resourceAssembler.toResource(project1);

            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/{id}/patch").withRel("projects"));
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/search").withRel("projects"));
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/search/accession").withRel("projects"));
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/search/release-date").withRel("projects"));
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/search/text").withRel("projects"));
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/search/taxonomy-id").withRel("projects"));
        resource.add(ControllerLinkBuilder.linkTo(ProjectRestController.class).slash("/search/taxonomy-name").withRel("projects"));
        return resource;
    }

}
