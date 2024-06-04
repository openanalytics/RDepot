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
package eu.openanalytics.rdepot.repo.model;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Synchronization result sent to the client
 */
@Setter
@Getter
public class SynchronizeRepositoryResponseBody {
    String id;
    String message;

    public SynchronizeRepositoryResponseBody(String id, String message) {
        super();
        this.id = id;
        this.message = message;
    }
}
