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
package uk.ac.ebi.ampt2d.metadata.aop;

import com.querydsl.core.types.Predicate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QStudy;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;

import java.time.LocalDate;

/**
 * An @Aspect for ensuring only published studies are returned through checking a study's releaseDate field
 */
@Aspect
public class StudyReleaseDateAspect {

    /**
     * An @Around advice for StudyRepository.findAll(..) method execution.
     *
     * It takes the first argument of the join point method and adds a new Predicate for checking a study's
     * releaseDate field. It then calls the join point method with updated arguments.
     *
     * @param proceedingJoinPoint
     * @return the return object from join point method execution
     * @throws Throwable
     */
    @Around("execution(* uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository.findAll(..))")
    public Object filterOnReleaseDateAdviceFindAll(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        Predicate predicate = (Predicate) args[0];

        QStudy study = QStudy.study;
        Predicate dateRestrictedPredicate = study.releaseDate.between(null, LocalDate.now()).and(predicate);

        args[0] = dateRestrictedPredicate;

        return proceedingJoinPoint.proceed(args);
    }

    /**
     * An @Around advice for CrudRepository.findOne(..) method execution
     *
     * It takes the returned object from join point method execution and check the returned object. If the returned
     * object is not a Study object, return as it is. If the returned object is a Study object, check the study's
     * releaseDate field. Return null if the releaseDate is a date in the future.
     *
     * @param proceedingJoinPoint
     * @return null or object
     * @throws Throwable
     */
    @Around("execution(* org.springframework.data.repository.CrudRepository.findOne(java.io.Serializable))")
    public Object filterOnReleaseDateAdviceFindOne(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = proceedingJoinPoint.proceed();

        if ( result == null ||
                ( result.getClass() == Study.class && ((Study) result).getReleaseDate().isAfter(LocalDate.now()) ) ) {
            return null;
        }

        return result;
    }

}
