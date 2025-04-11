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

import eu.openanalytics.rdepot.base.api.v2.dtos.CheckServerAddressDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.synchronization.healthcheck.ServerAddressHealthcheckService;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAuthority('admin')")
@RequestMapping("/api/v2/manager/check-server-address")
@Slf4j
public class ApiV2StatusController {

    private final MessageSource messageSource;
    private final ServerAddressHealthcheckService serverAddressHealthcheckService;
    private static final Locale locale = LocaleContextHolder.getLocale();

    public ApiV2StatusController(
            MessageSource messageSource, ServerAddressHealthcheckService serverAddressHealthcheckService) {
        this.messageSource = messageSource;
        this.serverAddressHealthcheckService = serverAddressHealthcheckService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<?> validateNewServerAddress(
            @RequestBody final CheckServerAddressDto checkServerAddressDto) {

        try {
            return serverAddressHealthcheckService.isHealthy(new URL(checkServerAddressDto.getServerAddress()))
                    ? serverOk()
                    : serverUnhealthy();
        } catch (MalformedURLException e) {
            log.debug(e.getMessage(), e);
            return invalidServerAddress();
        }
    }

    private ResponseEntity<?> invalidServerAddress() {
        return serverAddressError(MessageCodes.ERROR_INVALID_SERVERADDRESS);
    }

    private ResponseEntity<?> serverOk() {
        return ResponseEntity.ok(ResponseDto.generateSuccessBody(messageSource, locale, "OK"));
    }

    private ResponseEntity<?> serverUnhealthy() {
        return serverAddressError(MessageCodes.ERROR_UNHEALTHY_SERVERADDRESS);
    }

    private ResponseEntity<?> serverAddressError(String messageCode) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        StaticMessageResolver.getMessage(messageCode),
                        messageCode,
                        null));
    }
}
