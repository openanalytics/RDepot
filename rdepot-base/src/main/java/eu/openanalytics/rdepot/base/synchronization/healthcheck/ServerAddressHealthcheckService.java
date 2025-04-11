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
package eu.openanalytics.rdepot.base.synchronization.healthcheck;

import java.net.URL;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerAddressHealthcheckService {

    private final RestTemplate restTemplate;
    static final String STATUS_ENDPOINT = "/status";
    static final String OK_RESPONSE = "OK";

    public boolean isHealthy(final URL serverAddress) {
        final String addr = serverAddress.toString().replaceAll("/$", "");
        final String[] tokens = addr.split("/"); // "http://oa-rdepot-repo:8080/testrepo1/status"
        if (tokens.length < 4) return false;
        final String healthcheckUrl = String.format("%s%s", addr, STATUS_ENDPOINT);

        final String responseStr;
        try {
            final ResponseEntity<String> response = restTemplate.getForEntity(healthcheckUrl, String.class);
            if (response.getStatusCode() != HttpStatus.OK || !response.hasBody()) return false;
            responseStr = response.getBody();
        } catch (RestClientException e) {
            log.debug(e.getMessage(), e);
            return false;
        }

        final String debugMessage = "Response from the repo server after healthcheck: "
                + (responseStr != null ? responseStr.replace("\n", "\\n").replace("\r", "\\r") : "null");
        log.debug(debugMessage);
        return Objects.equals(responseStr, OK_RESPONSE);
    }
}
