/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('user')")
@RequestMapping("/api/v2/manager/version")
public class ApiV2VersionController {

    private final MessageSource messageSource;
    private final Locale locale;

    public ApiV2VersionController(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.locale = LocaleContextHolder.getLocale();
    }

    @GetMapping
    @Operation(operationId = "getVersion")
    public ResponseEntity<ResponseDto<String>> getVersion() {
        return ResponseEntity.ok(
                ResponseDto.generateSuccessBody(messageSource, locale, InternalTechnology.instance.getVersion()));
    }
}
