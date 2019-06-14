/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata.security;

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.SecurityUser;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SecurityUserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MetadataAuthoritiesExtractor implements AuthoritiesExtractor {

    private SecurityUserRepository securityUserRepository;

    public MetadataAuthoritiesExtractor(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    @Override
    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
        String preferredUsername = (String) map.get("preferred_username");
        SecurityUser securityUser = securityUserRepository.findByPreferredUsername(preferredUsername);
        if (securityUser == null) {
            securityUser = new SecurityUser(preferredUsername, SecurityUser.Role.ROLE_USER);
            securityUserRepository.save(securityUser);
            return Arrays.asList(new SimpleGrantedAuthority(securityUser.getRole().name()));
        }
        return Arrays.asList(new SimpleGrantedAuthority(securityUser.getRole().name()));
    }
}
