/**
 * R Depot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
package eu.openanalytics.rdepot.test.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.security.RestAccessDeniedHandler;
import eu.openanalytics.rdepot.base.security.RestAuthenticationEntryPoint;

/**
 * Simple security configuration for API unit tests.
 */
@Configuration
@ComponentScan({
	"eu.openanalytics.rdepot.r.api.v2.controllers", 
	"eu.openanalytics.rdepot.base.api.v2.controllers",
	"eu.openanalytics.rdepot.r.api.v2", 
	"eu.openanalytics.rdepot.base.api.v2",
	"eu.openanalytics.rdepot.base.validation"})
@EnableWebSecurity
@EnableAutoConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass=true)
//@EnableGlobalMethodSecurity(proxyTargetClass=true)
@EnableAsync
public class ApiTestConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/api/v2/**")
		.csrf().disable().authorizeRequests().anyRequest().hasAuthority("user")
		.and()
		.exceptionHandling()
		.accessDeniedHandler(new RestAccessDeniedHandler(messageSource(), Locale.ENGLISH, new ObjectMapper()))
		.authenticationEntryPoint(new RestAuthenticationEntryPoint(messageSource(), Locale.ENGLISH, new ObjectMapper()));
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder builder) {
		builder.authenticationProvider(new TestAuthenticationProvider());
	}
	
	@Bean
	public StaticMessageResolver staticMessageResolver() {
		return new StaticMessageResolver(messageSource());
	}
	
	@Bean
	MessageSource messageSource() 
	{
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasenames("i18n/messages-r","i18n/messages-common");
		source.setUseCodeAsDefaultMessage(true);
		return source;
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jsonConverter());
	}
	
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jsonConverter());
	}
	
	@Bean
	public MappingJackson2HttpMessageConverter jsonConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
		mediaTypes.add(MediaType.valueOf("application/json-patch+json"));
		converter.setSupportedMediaTypes(mediaTypes);
		converter.setObjectMapper(objectMapper());
		return converter;
	}

	@Override
	public MessageCodesResolver getMessageCodesResolver() {
		return new DefaultMessageCodesResolver() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6517730864672588910L;

			@Override
    		public String[] resolveMessageCodes(String errorCode, String objectName) {
    			return new String[]{errorCode};
    		}
    	};
	}
	
	@Bean
    public ObjectMapper objectMapper() {
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.registerModule(new JSR353Module());
    	objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
//    	objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    	return objectMapper;
    }
	
	@Bean
	public TimeZone timeZone() {
		TimeZone defaultTimeZone =  TimeZone.getTimeZone("UTC");
		TimeZone.setDefault(defaultTimeZone);
		return defaultTimeZone;
	}
}
