/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.base.security.backends.keycloak;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationFailureHandler;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.QueryParamPresenceRequestMatcher;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import eu.openanalytics.rdepot.base.security.authenticators.KeycloakCustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@ConditionalOnProperty(value = "app.authentication", havingValue = "keycloak")
@Order(2)
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(KeycloakSecurityConfig.class);
	
	public static final String DEFAULT_LOGIN_URL = "/sso/login";
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final  RequestMatcher customRequestMatcher =
			new AndRequestMatcher(
				new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**")),	
	            new OrRequestMatcher(
	                    new AntPathRequestMatcher(DEFAULT_LOGIN_URL),
	                    new RequestHeaderRequestMatcher(AUTHORIZATION_HEADER),
	                    new QueryParamPresenceRequestMatcher(OAuth2Constants.ACCESS_TOKEN)
	            	)		       
            );
	
	@Resource
	private Environment environment;
		
	@Autowired
    private AdapterConfig cfg;
	
	@Autowired
	private KeycloakCustomBindAuthenticator authenticator;

	@Override
	public void configure(WebSecurity web) throws Exception {
		web
	    	.ignoring()
	        .antMatchers("/static/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    super.configure(http);    
	        
	    http	    
	    	.authorizeRequests()
	          	.antMatchers("/authfailed").anonymous()
	           	.antMatchers("/manager/**").hasAuthority("user")
	           	.antMatchers("/static/**").permitAll()
	        .and()  	
	          	.addFilterAfter(keycloakAuthenticationProcessingFilter(), 
	          			BasicAuthenticationFilter.class)
	           	.logout()
	           		.invalidateHttpSession(true)
	           		.addLogoutHandler(keycloakLogoutHandler())
	         .and()
		     	.exceptionHandling().accessDeniedPage("/accessdenied");
	}
	    
	/**
	 * Defines the session authentication strategy.
	 */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}
		
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
			
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}
		
	@Bean
	public KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
		KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter = new KeycloakAuthenticationProcessingFilter(authenticationManagerBean(), customRequestMatcher);
		keycloakAuthenticationProcessingFilter.setAuthenticationFailureHandler(keycloakAuthenticationFailureHandler());
		return keycloakAuthenticationProcessingFilter;
	}		
	
	@Bean
	public KeycloakAuthenticationFailureHandler keycloakAuthenticationFailureHandler() {
		return new CustomKeycloakAuthenticationFailureHandler();
	}    
    
	@Bean
	@Primary
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
						token.getAccount().getKeycloakSecurityContext().getToken()
					);
				} catch (AuthException e) {
					logger.error("Authentication failed: " + e.getMessage());
					throw e;
				}

				return new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), auth);
			}
		};
	}
}
