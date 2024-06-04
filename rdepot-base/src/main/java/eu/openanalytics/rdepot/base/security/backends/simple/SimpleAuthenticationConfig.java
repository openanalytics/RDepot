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
package eu.openanalytics.rdepot.base.security.backends.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.security.RestAccessDeniedHandler;
import eu.openanalytics.rdepot.base.security.RestAuthenticationEntryPoint;
import eu.openanalytics.rdepot.base.security.authenticators.SimpleCustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.basic.AccessTokenAuthenticationFilter;
import eu.openanalytics.rdepot.base.security.basic.AccessTokenBindAuthenticator;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(value = "app.authentication", havingValue = "simple")
public class SimpleAuthenticationConfig {

    private final SimpleCustomBindAuthenticator authenticator;
    private final AccessTokenBindAuthenticator accessTokenBindAuthenticator;

    @Resource
    private Environment environment;

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    private final Locale locale = LocaleContextHolder.getLocale();
    private final ApiTokenProperties apiTokenProperties;

    public SimpleAuthenticationConfig(
            SimpleCustomBindAuthenticator authenticator,
            AccessTokenBindAuthenticator accessTokenBindAuthenticator,
            MessageSource messageSource,
            ObjectMapper objectMapper,
            ApiTokenProperties apiTokenProperties) {
        this.authenticator = authenticator;
        this.accessTokenBindAuthenticator = accessTokenBindAuthenticator;
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
        this.apiTokenProperties = apiTokenProperties;
    }

    @Value("${allowed-origin}")
    private String origin;

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(new CustomAuthenticationProvider(environment, authenticator));
        return builder.build();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                .addFilter(jwtAuthenticationFilter(http))
                .addFilterAfter(jwtAuthorizationFilter(), JWTAuthenticationFilter.class)
                .addFilterAfter(accessTokenAuthenticationFilter(), JWTAuthorizationFilter.class)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling.accessDeniedPage("/api/accessdenied")
                        .accessDeniedHandler(new RestAccessDeniedHandler(messageSource, locale, objectMapper))
                        .authenticationEntryPoint(
                                new RestAuthenticationEntryPoint(messageSource, locale, objectMapper)))
                .authenticationManager(authenticationManager(http));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(origin);
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "DELETE", "PATCH", "OPTIONS", "PUT"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    JWTAuthenticationFilter jwtAuthenticationFilter(HttpSecurity http) throws Exception {
        return new JWTAuthenticationFilter(environment, authenticationManager(http), apiTokenProperties);
    }

    JWTAuthorizationFilter jwtAuthorizationFilter() {
        return new JWTAuthorizationFilter(apiTokenProperties, authenticator);
    }

    AccessTokenAuthenticationFilter accessTokenAuthenticationFilter() {
        return new AccessTokenAuthenticationFilter(accessTokenBindAuthenticator, "simple");
    }
}
