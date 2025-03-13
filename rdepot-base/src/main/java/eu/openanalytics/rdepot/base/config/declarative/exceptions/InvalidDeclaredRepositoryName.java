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
package eu.openanalytics.rdepot.base.config.declarative.exceptions;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import java.io.Serial;

public class InvalidDeclaredRepositoryName extends Exception {

    @Serial
    private static final long serialVersionUID = -3853959410432257877L;

    public InvalidDeclaredRepositoryName(String repositoryName) {
        super(StaticMessageResolver.getMessage(MessageCodes.INVALID_REPOSITORY_NAME) + repositoryName);
    }
}
