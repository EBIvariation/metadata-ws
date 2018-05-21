package uk.ac.ebi.ampt2d.metadata.controller;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

@RestController
public class StudyRestController {

    @Autowired
    private StudyRepository studyRepository;

    @RequestMapping(method = RequestMethod.GET, path = "/studies/search/")
    public Iterable<Study> getStudies(@RequestParam("searchString") String searchString) {
        QStudy study = QStudy.study;
        Predicate predicate = study.name.containsIgnoreCase(searchString).
                or(study.description.containsIgnoreCase(searchString));
        return studyRepository.findAll(predicate);

    }
}
