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
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.resources.AnalysisResource;

import java.util.List;

@RestController
@Api(tags = "Analysis Entity")
@RequestMapping(path = "analyses")
public class AnalysisRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private GenericResourceAssembler<Analysis, AnalysisResource> resourceAssembler;

    @ApiOperation(value = "Get a filtered list of analyses based on filtering criteria")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "Analysis's type", dataType = "string",
                    paramType = "query", example = "CASE_CONTROL",
                    allowableValues = "CASE_CONTROL,CONTROL_SET,CASE_SET,COLLECTION,TUMOR,MATCHED_NORMAL"),
            @ApiImplicitParam(name = "platform", value = "Analysis's Platform", dataType = "string",
                    paramType = "query", example = "Illumina")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<AnalysisResource>> search(@QuerydslPredicate(root = Analysis.class) Predicate predicate) {
        List<Analysis> analyses = (List<Analysis>) analysisRepository.findAll(predicate);

        Resources<AnalysisResource> resources = (Resources<AnalysisResource>) resourceAssembler.toResources(Analysis.class, analyses);

        return ResponseEntity.ok(resources);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(AnalysisRestController.class).slash("/search").withRel("analyses"));
        return resource;
    }
}
