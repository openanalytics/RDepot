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
package eu.openanalytics.rdepot.repo.api;

import eu.openanalytics.rdepot.repo.chunks.processing.UploadRequestProcessor;
import eu.openanalytics.rdepot.repo.messaging.SharedMessageCodes;
import eu.openanalytics.rdepot.repo.model.RequestProcessingResult;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@Slf4j
public abstract class FileUploadController<R extends SynchronizeRepositoryRequestBody> {
    protected final UploadRequestProcessor<R> uploadRequestProcessor;

    protected FileUploadController(UploadRequestProcessor<R> uploadRequestProcessor) {
        this.uploadRequestProcessor = uploadRequestProcessor;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // This code protects Spring Core from a "Remote Code Execution" attack (dubbed "Spring4Shell").
        // By applying this mitigation, you prevent the "Class Loader Manipulation" attack vector from firing.
        // For more details, see this post: https://www.lunasec.io/docs/blog/spring-rce-vulnerabilities/
        String[] blackList = {"class.*", "Class.*", "*.class.*", ".*Class.*"};
        binder.setDisallowedFields(blackList);
    }

    protected ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(R requestBody) {

        log.info("Received request.");

        RequestProcessingResult result = uploadRequestProcessor.submit(requestBody);

        return switch (result) {
            case SUCCESS -> ResponseEntity.ok(
                    new SynchronizeRepositoryResponseBody(requestBody.getId(), SharedMessageCodes.RESPONSE_OK));
            case SERVER_ERROR -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SynchronizeRepositoryResponseBody(
                            requestBody.getId(), SharedMessageCodes.RESPONSE_ERROR));
            case CLIENT_ERROR -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SynchronizeRepositoryResponseBody(
                            requestBody.getId(), SharedMessageCodes.RESPONSE_ERROR));
        };
    }
}
