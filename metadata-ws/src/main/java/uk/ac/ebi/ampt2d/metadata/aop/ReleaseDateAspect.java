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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Auditable;
import uk.ac.ebi.ampt2d.metadata.security.CustomUsernamePasswordAuthenticationToken;
import uk.ac.ebi.ampt2d.metadata.security.EnableSecurityConfig;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An @Aspect for ensuring only published entities are returned through checking an entity's releaseDate field
 */
@Aspect
public class ReleaseDateAspect {

    /**
     * An @Around advice for repositories.*.findAll(..) method execution.
     * <p>
     * Check the release field of each entity and return only published entities.
     *
     * @param proceedingJoinPoint
     * @return the return object from join point method execution
     * @throws Throwable
     */

    @Around("execution(* uk.ac.ebi.ampt2d.metadata.persistence.repositories.*.findAll(..))")
    public Object filterOnReleaseDateAdviceFindAll(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Iterable<Object> results = (Iterable<Object>) proceedingJoinPoint.proceed();
        Iterator<Object> i = results.iterator();
        while (i.hasNext()) {
            Object result = i.next();
            if (getObjectUnlessBeforeReleaseDate(result) == null) {
                i.remove();
            }
        }
        return results;
    }

    /**
     * An @Around advice for CrudRepository.findOne(..) method execution
     * <p>
     * It takes the returned object from join point method execution and checks the returned object. If the returned
     * object is not a Auditable object, return as it is. If the returned object is an Auditable object, check its
     * release date. Return null if the release date is a date in the future.
     *
     * @param proceedingJoinPoint
     * @return null or object
     * @throws Throwable
     */
    @Around("execution(* org.springframework.data.repository.CrudRepository.findOne(java.io.Serializable))")
    public Object filterOnReleaseDateAdviceFindOne(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        return getObjectUnlessBeforeReleaseDate(result);
    }

    private Object getObjectUnlessBeforeReleaseDate(Object result) {
        if (!(result instanceof Auditable)) {
            return result;
        }

        LocalDate releaseDate = ((Auditable) result).getReleaseDate();
        if (releaseDate == null || !releaseDate.isAfter(LocalDate.now())) {
            return result;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new SecurityException("Authentication required");
        }

        //Allow admins to access before release date
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_" + EnableSecurityConfig.ROLE_SERVICE_OPERATOR))) {
            return result;
        }

        String commaSeparatedPermittedStudies = ((CustomUsernamePasswordAuthenticationToken)
                ((OAuth2Authentication) authentication).getUserAuthentication()).getStudies();
        List<String> permittedStudies;
        if (commaSeparatedPermittedStudies.isEmpty()) {
            return null;
        }
        permittedStudies = Arrays.asList(commaSeparatedPermittedStudies.split(","));
        List<String> studiesAssociatedToEntity =
                Arrays.asList(((Auditable) result).getStudyIds().split(","));

        if(CollectionUtils.containsAny(permittedStudies,studiesAssociatedToEntity)){
            return result;
        }

        return null;
    }

}
