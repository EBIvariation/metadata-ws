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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.resources.SampleResource;

import java.util.List;

@RestController
@Api(tags = "Sample Entity")
@RequestMapping(path = "samples")
public class SampleRestController implements ResourceProcessor<RepositoryLinksResource> {
    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private GenericResourceAssembler<Sample, SampleResource> resourceAssembler;

    @ApiOperation(value = "Get a filtered list of samples based on taxonomy name")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taxonomies.name", value = "Sample's taxonomy", dataType = "string",
                    paramType = "query", example = "Homo Sapiens")
    })

    @RequestMapping(method = RequestMethod.GET, path = "search", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<SampleResource>> search(@QuerydslPredicate(root = Sample.class) Predicate predicate) {
        List<Sample> samples = (List<Sample>) sampleRepository.findAll(predicate);

        Resources<SampleResource> resources = (Resources<SampleResource>) resourceAssembler.toResources(Sample.class, samples);

        return ResponseEntity.ok(resources);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(SampleRestController.class).slash("/search").withRel("samples"));
        return resource;
    }
}
