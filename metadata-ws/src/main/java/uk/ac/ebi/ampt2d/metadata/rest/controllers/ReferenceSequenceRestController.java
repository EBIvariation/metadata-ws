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
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.services.ReferenceSequenceService;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.resources.ReferenceSequenceResource;

import java.util.List;

@RestController
@Api(tags = "ReferenceSequence Entity")
@RequestMapping(path = "reference-sequences")
public class ReferenceSequenceRestController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private GenericResourceAssembler<ReferenceSequence, ReferenceSequenceResource> resourceAssembler;

    @Autowired
    private ReferenceSequenceService referenceSequenceService;


    @ApiOperation(value="Get a filtered list of reference sequences based on filtering criteria")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "name", dataType = "string", paramType = "query", example = "GRCh38"),
            @ApiImplicitParam(name = "patch", value = "patch number", dataType = "string", paramType = "query", example = "p2"),
            @ApiImplicitParam(name = "accessions", value = "accession", dataType = "string", paramType = "query", example = "GCA_000001405.3"),
            @ApiImplicitParam(name = "type", value = "Reference Sequence's type", dataType = "string",
                    paramType = "query", example = "ASSEMBLY", allowableValues = "ASSEMBLY,GENE,TRANSCRIPTOME")
    })
    @RequestMapping(method = RequestMethod.GET, path = "search", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ReferenceSequenceResource>> search(@QuerydslPredicate(root = ReferenceSequence.class) Predicate predicate) {
        List<ReferenceSequence> referenceSequences = (List<ReferenceSequence>) referenceSequenceRepository.findAll(predicate);

        Resources<ReferenceSequenceResource> resources = (Resources<ReferenceSequenceResource>) resourceAssembler.toResources(ReferenceSequence.class, referenceSequences);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the list of reference-sequences filtered by taxonomy id")
    @ApiParam(name = "id", value = "Taxonomy's id", type = "long", required = true, example = "9606")
    @RequestMapping(method = RequestMethod.GET, path = "search/taxonomy-id", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ReferenceSequenceResource>> findReferenceSequencesByTaxonomyId(long id) {
        List<ReferenceSequence> referenceSequences = referenceSequenceService.findReferenceSequencesByTaxonomyId(id);

        Resources<ReferenceSequenceResource> resources = (Resources<ReferenceSequenceResource>) resourceAssembler.toResources(ReferenceSequence.class, referenceSequences);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Get the list of reference-sequences filtered by taxonomy name")
    @ApiParam(name = "name", value = "Taxonomy's name", type = "string", required = true, example = "Homo sapiens")
    @RequestMapping(method = RequestMethod.GET, path = "search/taxonomy-name", produces = "application/json")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Resources<ReferenceSequenceResource>> findReferenceSequencesByTaxonomyName(String name) {
        List<ReferenceSequence> referenceSequences = referenceSequenceService.findReferenceSequencesByTaxonomyName(name);

        Resources<ReferenceSequenceResource> resources = (Resources<ReferenceSequenceResource>) resourceAssembler.toResources(ReferenceSequence.class, referenceSequences);

        return ResponseEntity.ok(resources);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(ReferenceSequenceRestController.class).slash("/search").withRel("reference-sequences"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/taxonomy-id").withRel("studies"));
        resource.add(ControllerLinkBuilder.linkTo(StudyRestController.class).slash("/search/taxonomy-name").withRel("studies"));
        return resource;
    }

}
