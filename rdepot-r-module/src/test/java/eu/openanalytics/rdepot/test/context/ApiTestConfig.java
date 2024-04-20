/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.test.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2ReadingController;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidatorImpl;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.security.RestAccessDeniedHandler;
import eu.openanalytics.rdepot.base.security.RestAuthenticationEntryPoint;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.r.api.v2.controllers.RPackageController;
import eu.openanalytics.rdepot.r.api.v2.controllers.RRepositoryController;
import eu.openanalytics.rdepot.r.api.v2.controllers.RSubmissionController;
import eu.openanalytics.rdepot.r.technology.RLanguage;

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
@EnableMethodSecurity
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
public class ApiTestConfig implements WebMvcConfigurer {

	private static final String[] EXTENSIONS = {"r", "common"};
	private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> 
	packageControllerClasses = Map.of(
				RLanguage.instance, RPackageController.class
	);
	
	private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>>
		repositoryControllerClasses = Map.of(
				RLanguage.instance, RRepositoryController.class
	);
	
	private static final Map<Technology, Class<? extends ApiV2ReadingController<?,?>>> 
		submissionControllerClasses = Map.of(
				RLanguage.instance, RSubmissionController.class
	);

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
	        .csrf(csrf -> csrf.disable())
	        .cors(Customizer.withDefaults())
	        .authorizeHttpRequests(requests -> requests
	        		.requestMatchers("/api/accessdenied").permitAll()
	        		.requestMatchers("/error").permitAll()
	        		.requestMatchers("/docs/**").permitAll()
	        		.requestMatchers("/v2/api-docs/**").permitAll()	        		
	        		.requestMatchers("/actuator/**").permitAll()
	        		.requestMatchers("/api/**").hasAuthority("user"))	        
	        .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .exceptionHandling(handling -> handling.accessDeniedPage("/api/accessdenied")
	        		.accessDeniedHandler(new RestAccessDeniedHandler(messageSource(), Locale.ENGLISH, new ObjectMapper()))
	                .authenticationEntryPoint(new RestAuthenticationEntryPoint(messageSource(), Locale.ENGLISH, new ObjectMapper())))
	        .authenticationManager(authenticationManager(http));
        return http.build();
	}

	AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		builder.authenticationProvider(new TestAuthenticationProvider());
		return builder.build();
	}
	
	@Bean
	Map<Technology, Class<? extends ApiV2ReadingController<?,?>>> packageControllerClassesByTechnology() {
		return packageControllerClasses;
	}
	
	@Bean
	Map<Technology, Class<? extends ApiV2ReadingController<?,?>>> repositoryControllerClassesByTechnology() {
		return repositoryControllerClasses;
	}
	
	@Bean
	Map<Technology, Class<? extends ApiV2ReadingController<?,?>>> submissionControllerClassesByTechnology() {
		return submissionControllerClasses;
	}

    @Bean
    StaticMessageResolver staticMessageResolver() {
		return new StaticMessageResolver(messageSource());
	}
	
	@Bean
	MessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		
		final String basename = "i18n/messages";
		List<String> basenames = new ArrayList<>();
		for(String ext : EXTENSIONS) {
			basenames.add(basename + "-" + ext);
		}
		
		source.setBasenames(basenames.toArray(new String[0]));
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
    MappingJackson2HttpMessageConverter jsonConverter() {
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
    ObjectMapper objectMapper() {
    	ObjectMapper objectMapper = new ObjectMapper();
//    	objectMapper.registerModule(new JSR353Module());
    	objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
//    	objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    	return objectMapper;
    }

    @Bean
    TimeZone timeZone() {
		TimeZone defaultTimeZone =  TimeZone.getTimeZone("UTC");
		TimeZone.setDefault(defaultTimeZone);
		return defaultTimeZone;
	}
}