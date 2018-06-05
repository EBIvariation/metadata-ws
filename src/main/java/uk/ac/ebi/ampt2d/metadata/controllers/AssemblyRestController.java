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
package uk.ac.ebi.ampt2d.metadata.controllers;

import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.assemblers.ResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Assembly;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AssemblyRepository;

import java.util.List;

@RestController
@Api(tags = "Assembly Entity")
@RequestMapping(path = "assemblies")
public class AssemblyRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private AssemblyRepository assemblyRepository;

    @Autowired
    private ResourceAssembler<Assembly> resourceAssembler;

    @ApiOperation(value="Get a filtered list of assemblies based on filtering criteria")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "name", dataType = "string", paramType = "query", example = "GRCh38"),
            @ApiImplicitParam(name = "patch", value = "patch number", dataType = "string", paramType = "query", example = "p2"),
            @ApiImplicitParam(name = "accessions", value = "accession", dataType = "string", paramType = "query", example = "GCA_000001405.3")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Resources<?>> search(@QuerydslPredicate(root = Assembly.class) Predicate predicate) {
        List<Assembly> assemblies = (List<Assembly>) assemblyRepository.findAll(predicate);

        Resources<?> resources = resourceAssembler.entitiesToResources(Assembly.class, assemblies);

        return ResponseEntity.ok(resources);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(AssemblyRestController.class).slash("/search").withRel("assemblies"));
        return resource;
    }

}
