package uk.ac.ebi.ampt2d.metadata.controller;

import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;

@RestController
@Api(tags = "Study Entity")
public class StudyRestController {

    @Autowired
    private StudyRepository studyRepository;

    @ApiOperation(value = "studySearch")
    @RequestMapping(method = RequestMethod.GET, path = "/studies/search/")
    public Iterable<Study> getStudies(@RequestParam("searchString") String searchString) {
        QStudy study = QStudy.study;
        Predicate predicate = study.name.containsIgnoreCase(searchString).
                or(study.description.containsIgnoreCase(searchString));
        return studyRepository.findAll(predicate);

    }
}
