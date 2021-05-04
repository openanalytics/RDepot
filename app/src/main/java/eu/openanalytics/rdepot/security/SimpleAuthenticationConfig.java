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
package eu.openanalytics.rdepot.security;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import eu.openanalytics.rdepot.authenticator.SimpleCustomBindAuthenticator;
import eu.openanalytics.rdepot.security.simple.CustomAuthenticationProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(value = "app.authentication", havingValue = "simple")
@Order(2)
public class SimpleAuthenticationConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SimpleCustomBindAuthenticator authenticator;
	
	@Resource
	private Environment environment;


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/manager/**").hasAuthority("user")
				.antMatchers("/static/**").permitAll()
			.and()
				.formLogin()
	            .loginPage("/login")
	            .defaultSuccessUrl("/manager")
	            .failureUrl("/loginfailed")
	            .permitAll()
	         .and()
	         	.logout()
	         		.invalidateHttpSession(true)
			.and()
				.exceptionHandling().accessDeniedPage("/accessdenied");
		}
		
	@Override
	protected void configure(AuthenticationManagerBuilder builder) throws Exception {
	      builder.authenticationProvider(new CustomAuthenticationProvider(environment, authenticator));
	}
	
}
