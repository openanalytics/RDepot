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
package eu.openanalytics.rdepot.repo.model;

/**
 * Final result of request processing used to generate HTTP response.
 */
public enum RequestProcessingResult {
    /**
     * Everything went fine, chunk has been processed with no issues.
     */
    SUCCESS,

    /**
     * There was an error in the request, e.g.
     * - user tried to upload (not first) chunk that was not part of any ongoing transaction
     * - user tried to upload chunks in a wrong order
     * - user tried to start transaction for a repository which was already busy with another one
     */
    CLIENT_ERROR,

    /**
     * Internal (e.g. storage) error that was not user's fault.
     */
    SERVER_ERROR
}
