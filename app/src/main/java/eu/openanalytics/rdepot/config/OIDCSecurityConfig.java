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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import eu.openanalytics.rdepot.authenticator.OIDCCustomBindAuthenticator;
import eu.openanalytics.rdepot.controller.ErrorController;
import eu.openanalytics.rdepot.exception.AuthException;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@ConditionalOnProperty(value = "app.authentication", havingValue = "openid")
public class OIDCSecurityConfig extends WebSecurityConfigurerAdapter {
	
	public static final String NAME = "openid";

	private static final String REG_ID = "rdepot";
	private static final String ENV_TOKEN_NAME = "RDEPOT_OIDC_ACCESS_TOKEN";
	
	@Resource
	private Environment environment;
	
	@Autowired
	private OIDCCustomBindAuthenticator authenticator;
	
	@Autowired
	private ErrorController errorController;
	
	private OAuth2AuthorizedClientService authorizedClientService;
	
	private ClientRegistrationRepository clientRegistrationRepo;
	
	private ClientRegistration client;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		clientRegistrationRepo = createClientRepo();
		authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepo);
		
		http
			.authorizeRequests()
				.antMatchers("/authfailed").permitAll()
				.antMatchers("/manager/**").hasAuthority("user")
				.antMatchers("/static/**").permitAll()	
			.and()
				.logout()
				.invalidateHttpSession(true)
				.logoutSuccessHandler(oidcLogoutSuccessHandler())
			.and()
				.oauth2Login()	
					.clientRegistrationRepository(clientRegistrationRepo)
					.authorizedClientService(authorizedClientService)					
					.defaultSuccessUrl("/manager")
					.failureHandler(errorController)
					.userInfoEndpoint()
						.oidcUserService(createOidcUserService());
	}
		
	protected ClientRegistrationRepository createClientRepo() {
		Set<String> scopes = new HashSet<>();
		scopes.add("openid");
		scopes.add("email");
		scopes.add("profile");
		
		for (int i=0;;i++) {
			String scope = environment.getProperty(String.format("app.openid.scopes[%d]", i));
			if (scope == null) break;
			else scopes.add(scope);
		}
				
		Map<String, Object> configurationMetadata = new HashMap<>();
		configurationMetadata.put("end_session_endpoint", environment.getProperty("app.openid.logout-url"));
		
		client = ClientRegistration.withRegistrationId(REG_ID)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.clientName(REG_ID)
				.redirectUriTemplate(environment.getProperty("app.openid.baseUrl") 
									+ "/login/oauth2/code/"
									+ environment.getProperty("app.openid.registrationId"))
				.scope(scopes.toArray(new String[scopes.size()]))
				.userNameAttributeName(environment.getProperty("app.openid.username-attribute", "nickname"))
				.authorizationUri(environment.getProperty("app.openid.auth-url"))
				.tokenUri(environment.getProperty("app.openid.token-url"))
				.jwkSetUri(environment.getProperty("app.openid.jwks-url"))
				.clientId(environment.getProperty("app.openid.client-id"))
				.clientSecret(environment.getProperty("app.openid.client-secret"))
				.providerConfigurationMetadata(configurationMetadata)		
				.build();
		
		return new InMemoryClientRegistrationRepository(Collections.singletonList(client));
	}
	
	protected OidcUserService createOidcUserService() {
		return new OidcUserService() {
			@SuppressWarnings("unchecked")
			@Override
			public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
				OidcUser user = super.loadUser(userRequest);
				String nameAttributeKey = environment.getProperty("app.openid.username-attribute", "nickname");
				
				Set<GrantedAuthority> auth = new HashSet<>();
				try {
					auth = (Set<GrantedAuthority>) authenticator.authenticate(user.getName(), userRequest.getIdToken());
				} catch (AuthException e) {
					throw e;
				}
				
				return new DefaultOidcUser(auth, user.getIdToken(), user.getUserInfo(), nameAttributeKey);
			}
		};
	}
	
	private LogoutSuccessHandler oidcLogoutSuccessHandler() {
	    OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
	      new OidcClientInitiatedLogoutSuccessHandler(
	        this.clientRegistrationRepo);
	 
	    oidcLogoutSuccessHandler.setPostLogoutRedirectUri(environment.getProperty("app.openid.baseUrl"));
	 
	    return oidcLogoutSuccessHandler;
	}
}
