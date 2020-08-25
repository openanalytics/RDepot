/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.config;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationFailureHandler;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import eu.openanalytics.rdepot.authenticator.KeycloakCustomBindAuthenticator;
import eu.openanalytics.rdepot.exception.AuthException;
import eu.openanalytics.rdepot.utils.CustomKeycloakAuthenticationFailureHandler;
import eu.openanalytics.rdepot.utils.CustomKeycloakSpringBootConfigResolver;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@ConditionalOnProperty(value = "app.authentication", havingValue = "keycloak")
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Resource
	private Environment environment;
		
	@Autowired
    private AdapterConfig cfg;
	
	@Autowired
	private KeycloakCustomBindAuthenticator authenticator;
	
	/**
	 * Registers the KeycloakAuthenticationProvider with the authentication manager.
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	/**
	 * Defines the session authentication strategy.
	 */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}
	
	@Bean
	public KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
		KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter = new KeycloakAuthenticationProcessingFilter(authenticationManagerBean());
		keycloakAuthenticationProcessingFilter.setAuthenticationFailureHandler(keycloakAuthenticationFailureHandler());
		return keycloakAuthenticationProcessingFilter;
	}
	
	@Bean
	public KeycloakAuthenticationFailureHandler keycloakAuthenticationFailureHandler() {
		return new CustomKeycloakAuthenticationFailureHandler();
	}

    /**
     * Define security constraints for the application resources.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);    
        
        http
            .authorizeRequests()
            	.antMatchers("/authfailed").anonymous()
            	.antMatchers("/manager/**").hasAuthority("user")
            	.antMatchers("/static/**").permitAll()
            .and()  	
	          .logout()
	          	.invalidateHttpSession(true)
	          	.addLogoutHandler(keycloakLogoutHandler());
//	          	.logoutUrl("/sso/logout").permitAll();
    }
    
    
	@Bean
	public KeycloakConfigResolver KeycloakConfigResolver(KeycloakSpringBootProperties properties) {
		return new CustomKeycloakSpringBootConfigResolver(environment, cfg);
	}
				
	protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
		return new KeycloakAuthenticationProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) super.authenticate(authentication);
				Set<GrantedAuthority> auth = new HashSet<>();
				
				try {
					auth = (Set<GrantedAuthority>) authenticator.authenticate(
						token.getName(),
						token.getAccount().getKeycloakSecurityContext().getIdToken()
						);
				} catch (AuthException e) {
					System.out.println("Failed: " + e.getMessage());
					throw e;
				}

				return new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), auth);
			}
		};
	}
}
