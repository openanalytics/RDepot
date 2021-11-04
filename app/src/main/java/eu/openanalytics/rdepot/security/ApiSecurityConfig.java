/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.security;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.repository.ApiTokenRepository;
import eu.openanalytics.rdepot.service.UserService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Order(1)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${api_token.secret}")
	private String SECRET;
	
	@Value("${app.authentication}")
	private String mode;
	
	@Autowired
	private ApiTokenRepository apiTokenRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ObjectMapper objectMapper;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.antMatcher("/api/**")
				.csrf().disable()
			.authorizeRequests()
				.anyRequest().hasAuthority("user")
			.and()
				.addFilter(new JWTAuthorizationFilter(authenticationManager(), apiTokenRepository, userService, SECRET, mode))
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)	
			.and()
	     		.exceptionHandling().accessDeniedPage("/api/accessdenied")
	     		.accessDeniedHandler(new RestAccessDeniedHandler(messageSource, locale, objectMapper))
	     		.authenticationEntryPoint(new RestAuthenticationEntryPoint(messageSource, locale, objectMapper));
			
	}
}
