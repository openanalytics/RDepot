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
package eu.openanalytics.rdepot.r.utils.exceptions;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.r.messaging.RMessageCodes;

public class ParsePackagesFileException extends Exception {

    private static final long serialVersionUID = -7221220068660991883L;

    public ParsePackagesFileException(String line) {
        super(StaticMessageResolver.getMessage(RMessageCodes.COULD_NOT_PARSE_PACKAGES_FILE) + ": line '" + line + "'");
    }
}
