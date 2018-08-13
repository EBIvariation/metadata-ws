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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionEntityId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.LinkedStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.services.LinkedStudyService;
import uk.ac.ebi.ampt2d.metadata.rest.assemblers.GenericResourceAssembler;
import uk.ac.ebi.ampt2d.metadata.rest.resources.StudyResource;

import java.util.Arrays;
import java.util.List;

@RestController
@Api(tags = "Study Entity")
public class LinkedStudyRestController {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private LinkedStudyService linkedStudyService;

    @Autowired
    private GenericResourceAssembler<Study, StudyResource> resourceAssembler;

    @ApiOperation(value = "Get a list of studies that are linked to a study")
    @RequestMapping(method = RequestMethod.GET, path = "studies/{id}/linkedStudies", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Resources<StudyResource>> getLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id) {
        Study study = studyRepository.findOne(id);
        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        List<Study> studies = linkedStudyService.findLinkedStudiesById(id);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Create links (relationship) between a study and a list of studies")
    @RequestMapping(method = RequestMethod.POST, path = "studies/{id}/linkedStudies", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<Resources<StudyResource>> postLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id,
                                            @RequestBody AccessionVersionEntityId[] linkedStudies) {
        Study study = studyRepository.findOne(id);
        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        List<Study> studyList = linkedStudyService.findLinkedStudiesById(id);
        if ( !studyList.isEmpty() ) {
            return ResponseEntity.badRequest().build();
        }

        List<LinkedStudy> result = linkedStudyService.save(id, Arrays.asList(linkedStudies));

        List<Study> studies = linkedStudyService.findLinkedStudiesById(id);

        Resources<StudyResource> resources = (Resources<StudyResource>) resourceAssembler.toResources(Study.class, studies);

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Replace the list of studies a study previously links to")
    @RequestMapping(method = RequestMethod.PUT, path = "studies/{id}/linkedStudies", consumes = "application/json")
    @ResponseBody
    public ResponseEntity putLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id,
                                            @RequestBody AccessionVersionEntityId[] linkedStudies) {
        Study study = studyRepository.findOne(id);
        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        linkedStudyService.delete(id);

        List<LinkedStudy> result = linkedStudyService.save(id, Arrays.asList(linkedStudies));

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Add a new list of studies that are linked to a study")
    @RequestMapping(method = RequestMethod.PATCH, path = "studies/{id}/linkedStudies", consumes = "application/json")
    @ResponseBody
    public ResponseEntity patchLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id,
                                           @RequestBody AccessionVersionEntityId[] linkedStudies) {
        Study study = studyRepository.findOne(id);
        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        List<LinkedStudy> result = linkedStudyService.save(id, Arrays.asList(linkedStudies));

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Delete all links (relationship) between a study and its linked studies")
    @RequestMapping(method = RequestMethod.DELETE, path = "studies/{id}/linkedStudies")
    @ResponseBody
    public ResponseEntity deleteLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id) {
        Study study = studyRepository.findOne(id);
        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        linkedStudyService.delete(id);

        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Get the linked study of a study")
    @RequestMapping(method = RequestMethod.GET, path = "studies/{id}/linkedStudies/{linkedStudyId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Resource<Study>> getLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id,
                                                            @PathVariable(name = "linkedStudyId") AccessionVersionEntityId linkedStudyId) {
        if ( studyRepository.findOne(id) == null ) {
            return ResponseEntity.notFound().build();
        }

        LinkedStudy linkedStudy = linkedStudyService.findOne(id, linkedStudyId);

        if ( linkedStudy == null ) {
            return ResponseEntity.notFound().build();
        }

        Study study = studyRepository.findOne(linkedStudyId);

        Resource<Study> resource = resourceAssembler.toResource(study);

        return ResponseEntity.ok(resource);
    }

    @ApiOperation(value = "Delete a linked study of a study")
    @RequestMapping(method = RequestMethod.DELETE, path = "studies/{id}/linkedStudies/{linkedStudyId}")
    @ResponseBody
    public ResponseEntity<Resource<Study>> deleteLinkedStudies(@PathVariable(name = "id") AccessionVersionEntityId id,
                                                               @PathVariable(name = "linkedStudyId") AccessionVersionEntityId linkedStudyId) {
        Study study = studyRepository.findOne(id);
        if ( study == null ) {
            return ResponseEntity.notFound().build();
        }

        LinkedStudy linkedStudy = linkedStudyService.findOne(id, linkedStudyId);

        if ( linkedStudy == null ) {
            return ResponseEntity.notFound().build();
        }

        linkedStudyService.delete(id, linkedStudyId);

        return ResponseEntity.ok().build();
    }

}
