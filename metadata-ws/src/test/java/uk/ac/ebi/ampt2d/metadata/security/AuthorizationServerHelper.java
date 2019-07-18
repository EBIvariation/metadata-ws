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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@EnableAuthorizationServer
public class AuthorizationServerHelper extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthorizationServerTokenServices tokenservice;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ClientDetailsService clientDetailsService;

    public RequestPostProcessor bearerToken(final String clientid) {
        return mockRequest -> {
            OAuth2AccessToken token = createAccessToken(clientid);
            mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
            return mockRequest;
        };
    }

    private OAuth2AccessToken createAccessToken(final String clientId) {
        ClientDetails client = clientDetailsService.loadClientByClientId(clientId);
        Map<String, Object> clientNameMap = client.getAdditionalInformation();

        OAuth2Authentication auth = jwtAccessTokenCustomizer(mapper).extractAuthentication(clientNameMap);
        OAuth2AccessToken auth2AccessToken = tokenservice.createAccessToken(auth);
        return auth2AccessToken;
    }

    @Override
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
        Map<String, Object> testuserMap = getClientInfo("testuser", "DEFAULT");
        Map<String, Object> testoperatorMap = getClientInfo("testoperator", EnableSecurityConfig.ROLE_SERVICE_OPERATOR);
        clients.inMemory()
                .withClient("testoperator")
                .authorities("ROLE_" + EnableSecurityConfig.ROLE_SERVICE_OPERATOR)
                .additionalInformation(testoperatorMap)
                .and()
                .withClient("testuser")
                .additionalInformation(testuserMap);
    }

    @Bean
    public JwtAccessTokenCustomizer jwtAccessTokenCustomizer(ObjectMapper mapper) {
        return new JwtAccessTokenCustomizer(mapper);
    }

    private static Map<String, Object> getClientInfo(String clientId, String role) {
        Map<String, Object> clientNameMap = new LinkedHashMap<>();
        clientNameMap.put("jti", "7ac94e65-119e-471e-abb9-4e1fb5cc79d2");
        clientNameMap.put("exp", 1561536742L);
        clientNameMap.put("nbf", 0);
        clientNameMap.put("iat", 1561536442);
        clientNameMap.put("iss", "http://localhost:8085/ampt2d/auth/realms/securemetadata");
        clientNameMap.put("aud", "secure-client");
        clientNameMap.put("sub", "77dfbb1a-f998-4477-8276-f7a3ef5893b0");
        clientNameMap.put("typ", "Bearer");
        clientNameMap.put("azp", "secure-client");
        clientNameMap.put("auth_time", 1561536442);
        clientNameMap.put("session_state", "4a75616f-810c-40ed-beb7-875867b0accc");
        clientNameMap.put("acr", "1");
        clientNameMap.put("allowed-origins", new ArrayList<>());

        ArrayList<String> rolesValueRealmList = new ArrayList<>(Arrays.asList("offline_access", "uma_authorization"));
        Map<String, Object> rolesMap = new LinkedHashMap<>();
        rolesMap.put("roles", rolesValueRealmList);
        clientNameMap.put("realm_access", rolesMap);

        ArrayList<String> rolesValueList = new ArrayList<>(Arrays.asList("manage_account", "manage_account-links", "view-profile"));
        Map<String, Object> accountMap = new LinkedHashMap<>();
        Map<String, Object> resourceAccessMap = new LinkedHashMap<>();
        accountMap.put("roles", rolesValueList);
        resourceAccessMap.put("account", accountMap);
        if (clientId.equals("testoperator") && role.equals(EnableSecurityConfig.ROLE_SERVICE_OPERATOR)) {
            ArrayList<String> rolesValueOpList = new ArrayList<>(Arrays.asList(EnableSecurityConfig.ROLE_SERVICE_OPERATOR));
            Map<String, Object> secureClientMap = new LinkedHashMap<>();
            secureClientMap.put("roles", rolesValueOpList);
            resourceAccessMap.put("secure-client", secureClientMap);
        }
        clientNameMap.put("resource_access", resourceAccessMap);

        clientNameMap.put("scope", "openid profile email");
        clientNameMap.put("email_verified", false);
        clientNameMap.put("user_name", clientId);
        clientNameMap.put("preferred_username", clientId);
        clientNameMap.put("client_id", clientId);
        return clientNameMap;
    }

}
