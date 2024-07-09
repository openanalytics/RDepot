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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import eu.openanalytics.rdepot.base.api.v2.dtos.PublicConfigurationDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('user')")
@RequestMapping("/api/v2/manager/config")
public class ApiV2ConfigController {

    private final boolean declarative;
    private final boolean deletingPackagesEnabled;
    private final boolean deletingRepositoriesEnabled;
    private final boolean replacingPackagesEnabled;
    private final boolean accessTokenLifetimeConfigurable;
    private final boolean generateManuals;
    private final int accessTokenLifetimeDefault;
    private final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();

    public ApiV2ConfigController(
            MessageSource messageSource,
            @Value("${declarative}") String declarative,
            @Value("${deleting.packages.enabled}") String deletingPackagesEnabled,
            @Value("${deleting.repositories.enabled}") String deletingRepositoriesEnabled,
            @Value("${replacing.packages.enabled}") String replacingPackagesEnabled,
            @Value("${access-token.lifetime-configurable}") String accessTokenLifetimeConfigurable,
            @Value("${access-token.lifetime-default}") String accessTokenLifetimeDefault,
            @Value("${generate-manuals}") String generateManuals) {
        this.messageSource = messageSource;
        this.declarative = Boolean.parseBoolean(declarative);
        this.deletingPackagesEnabled = Boolean.parseBoolean(deletingPackagesEnabled);
        this.deletingRepositoriesEnabled = Boolean.parseBoolean(deletingRepositoriesEnabled);
        this.replacingPackagesEnabled = Boolean.parseBoolean(replacingPackagesEnabled);
        this.accessTokenLifetimeConfigurable = Boolean.parseBoolean(accessTokenLifetimeConfigurable);
        this.accessTokenLifetimeDefault = Integer.parseInt(accessTokenLifetimeDefault);
        this.generateManuals = Boolean.parseBoolean(generateManuals);
    }

    @GetMapping
    public ResponseEntity<ResponseDto<PublicConfigurationDto>> getPublicConfig() {
        final ResponseDto<PublicConfigurationDto> dto = ResponseDto.generateSuccessBody(
                messageSource,
                locale,
                new PublicConfigurationDto(
                        declarative,
                        deletingPackagesEnabled,
                        deletingRepositoriesEnabled,
                        replacingPackagesEnabled,
                        accessTokenLifetimeConfigurable,
                        accessTokenLifetimeDefault,
                        generateManuals));

        return ResponseEntity.ok(dto);
    }
}
