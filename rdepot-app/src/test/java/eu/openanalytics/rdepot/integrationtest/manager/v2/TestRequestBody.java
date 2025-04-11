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
package eu.openanalytics.rdepot.integrationtest.manager.v2;

import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionMultipartBody;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TestRequestBody {
    private RequestType requestType;
    private String expectedJsonPath;

    @Builder.Default
    private String urlSuffix = "";

    private int statusCode;
    private String token;
    private int howManyNewEventsShouldBeCreated;
    private String path;
    private String expectedEventsJson;
    private String body;
    private SubmissionMultipartBody submissionMultipartBody;

    public Optional<SubmissionMultipartBody> getSubmissionMultipartBody() {
        return Optional.ofNullable(submissionMultipartBody);
    }

    public Optional<String> getExpectedEventsJson() {
        return Optional.ofNullable(expectedEventsJson);
    }

    public Optional<String> getBody() {
        return Optional.ofNullable(body);
    }

    public Optional<String> getPath() {
        return Optional.ofNullable(path);
    }
}
