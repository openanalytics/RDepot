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
package eu.openanalytics.rdepot.base.security.backends.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import eu.openanalytics.rdepot.base.security.RestAuthenticationEntryPoint;
import eu.openanalytics.rdepot.base.security.authenticators.Oauth2CustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.basic.AccessTokenAuthenticationFilter;
import eu.openanalytics.rdepot.base.security.basic.AccessTokenBindAuthenticator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@ConditionalOnProperty(value = "app.authentication", havingValue = "oauth2")
public class Oauth2SecurityConfig {

    @Autowired
    private Oauth2Properties oauth2Properties;

    @Value("${allowed-origin}")
    private String origin;

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final Oauth2CustomBindAuthenticator authenticator;
    private final AccessTokenBindAuthenticator accessTokenBindAuthenticator;

    public Oauth2SecurityConfig(
            MessageSource messageSource,
            ObjectMapper objectMapper,
            Oauth2CustomBindAuthenticator authenticator,
            AccessTokenBindAuthenticator accessTokenBindAuthenticator) {
        this.accessTokenBindAuthenticator = accessTokenBindAuthenticator;
        this.authenticator = authenticator;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    private final Locale locale = LocaleContextHolder.getLocale();

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**")
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/docs/**")
                        .permitAll()
                        .requestMatchers("/v2/api-docs/**")
                        .permitAll()
                        .requestMatchers("/.well-known/**")
                        .permitAll()
                        .requestMatchers("/oauth2/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .hasAuthority("user")
                        .anyRequest()
                        .authenticated())
                .addFilterAfter(accessTokenAuthenticationFilter(), BasicAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> {
                    oauth2.jwt(jwt -> {
                        jwt.jwtAuthenticationConverter(oauth2JWTAuthenticationConverter());
                        jwt.decoder(jwtDecoder());
                    });
                    oauth2.authenticationEntryPoint(
                            new RestAuthenticationEntryPoint(messageSource, locale, objectMapper));
                });

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

    AccessTokenAuthenticationFilter accessTokenAuthenticationFilter() {
        return new AccessTokenAuthenticationFilter(accessTokenBindAuthenticator, "oauth2");
    }

    @Bean
    JwtDecoder jwtDecoder() {
        Set<JOSEObjectType> allowedTypes = new HashSet<>();
        allowedTypes.add(null);
        for (String el : oauth2Properties.getJwtTypes()) {
            allowedTypes.add(new JOSEObjectType(el));
        }

        return NimbusJwtDecoder.withJwkSetUri(oauth2Properties.getJwkSetUri())
                .jwtProcessorCustomizer(
                        customizer -> customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(allowedTypes)))
                .build();
    }

    @Bean
    Oauth2JWTAuthenticationConverter oauth2JWTAuthenticationConverter() {
        return new Oauth2JWTAuthenticationConverter(
                authenticator,
                oauth2Properties.getLoginField(),
                oauth2Properties.getEmailField(),
                oauth2Properties.getFullNameField());
    }
}
