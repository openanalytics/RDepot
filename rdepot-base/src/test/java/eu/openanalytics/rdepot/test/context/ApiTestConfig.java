/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2ReadingController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2SubmissionController;
import eu.openanalytics.rdepot.base.api.v2.sorting.DtoToEntityPropertyMappingImpl;
import eu.openanalytics.rdepot.base.api.v2.sorting.PackageDtoToEntityPropertyMapping;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.security.RestAccessDeniedHandler;
import eu.openanalytics.rdepot.base.security.RestAuthenticationEntryPoint;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import eu.openanalytics.rdepot.base.technology.Technology;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Simple security configuration for API unit tests.
 */
@Configuration
@ComponentScan({
    "eu.openanalytics.rdepot.base.api.v2.controllers",
    "eu.openanalytics.rdepot.base.api.v2",
    "eu.openanalytics.rdepot.base.validation"
})
@EnableWebSecurity
@EnableAutoConfiguration
@EnableMethodSecurity
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
public class ApiTestConfig implements WebMvcConfigurer {

    private static final String[] EXTENSIONS = {"common"};
    private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> packageControllerClasses =
            Map.of(InternalTechnology.instance, ApiV2PackageController.class);

    private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> repositoryControllerClasses =
            Map.of(InternalTechnology.instance, ApiV2RepositoryController.class);

    private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> submissionControllerClasses =
            Map.of(InternalTechnology.instance, ApiV2SubmissionController.class);

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests.requestMatchers("/api/accessdenied")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers("/docs/**")
                        .permitAll()
                        .requestMatchers("/v2/api-docs/**")
                        .permitAll()
                        .requestMatchers("/actuator/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .hasAuthority("user"))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling.accessDeniedPage("/api/accessdenied")
                        .accessDeniedHandler(
                                new RestAccessDeniedHandler(messageSource(), Locale.ENGLISH, new ObjectMapper()))
                        .authenticationEntryPoint(
                                new RestAuthenticationEntryPoint(messageSource(), Locale.ENGLISH, new ObjectMapper())))
                .authenticationManager(authenticationManager(http));
        return http.build();
    }

    AuthenticationManager authenticationManager(HttpSecurity http) {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(new TestAuthenticationProvider());
        return builder.build();
    }

    @Bean
    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> packageControllerClassesByTechnology() {
        return packageControllerClasses;
    }

    @Bean
    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> repositoryControllerClassesByTechnology() {
        return repositoryControllerClasses;
    }

    @Bean
    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> submissionControllerClassesByTechnology() {
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
        for (String ext : EXTENSIONS) {
            basenames.add(basename + "-" + ext);
        }

        source.setBasenames(basenames.toArray(new String[0]));
        source.setUseCodeAsDefaultMessage(true);

        return source;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        return new DefaultMessageCodesResolver() {

            @Serial
            private static final long serialVersionUID = -6517730864672588910L;

            @Override
            @NullMarked
            public String[] resolveMessageCodes(String errorCode, String objectName) {
                return new String[] {errorCode};
            }
        };
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return objectMapper;
    }

    @Bean
    TimeZone timeZone() {
        TimeZone defaultTimeZone = TimeZone.getTimeZone("UTC");
        TimeZone.setDefault(defaultTimeZone);
        return defaultTimeZone;
    }

    @Bean(name = "dtoToEntityPropertyMapping")
    public DtoToEntityPropertyMappingImpl dtoToEntityPropertyMapping() {
        return new DtoToEntityPropertyMappingImpl();
    }

    @Bean(name = "packageDtoToEntityPropertyMapping")
    public PackageDtoToEntityPropertyMapping packageDtoToEntityPropertyMapping() {
        return new PackageDtoToEntityPropertyMapping();
    }
}
