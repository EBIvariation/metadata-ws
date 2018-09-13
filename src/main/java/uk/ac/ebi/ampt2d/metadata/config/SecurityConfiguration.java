package uk.ac.ebi.ampt2d.metadata.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

public class SecurityConfiguration {

    @ConditionalOnProperty(value = "security.enabled", havingValue = "true")
    @Configuration
    @EnableResourceServer
    static class EnableSecurity extends ResourceServerConfigurerAdapter {

        private static final String[] AUTH_WHITELIST = {
                // -- swagger ui
                "/v2/api-docs",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**",
                "/"
        };

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers(AUTH_WHITELIST).permitAll()
                    .anyRequest().authenticated();

        }
    }

    @ConditionalOnProperty(value = "security.enabled", havingValue = "false")
    @Configuration
    @EnableResourceServer
    static class DisableSecurity extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().permitAll();
        }
    }
}
