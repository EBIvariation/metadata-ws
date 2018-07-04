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

@Aspect
public class StudyDeprecationAspect {

    @Around("execution(* uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository.findAll(..))")
    public Object filterOnDeprecationAdviceFindAll(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        Predicate predicate = (Predicate) args[0];

        QStudy study = QStudy.study;
        Predicate deprecationCheckedPredicate = study.deprecated.isFalse().and(predicate);

        args[0] = deprecationCheckedPredicate;

        return proceedingJoinPoint.proceed(args);
    }

    @Around("execution(* uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository.findOne(..))")
    public Object filterOnDeprecationAdviceFindOne(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object study = proceedingJoinPoint.proceed();

        if ( study != null && ((Study) study).isDeprecated() ) {
            return null;
        }

        return study;
    }

}
