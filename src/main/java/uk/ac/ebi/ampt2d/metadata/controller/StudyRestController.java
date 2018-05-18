package uk.ac.ebi.ampt2d.metadata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

import java.util.List;

@RestController
public class StudyRestController {

    @Autowired
    private StudyRepository studyRepository;

    @RequestMapping(method = RequestMethod.GET, path = "/studies/search/")
    public List<Study> getStudies(@RequestParam("searchString") String searchString) {
        return studyRepository.findAll(Example.of(new Study(searchString, searchString), ExampleMatcher.matchingAny()
                .withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)));
    }
}
