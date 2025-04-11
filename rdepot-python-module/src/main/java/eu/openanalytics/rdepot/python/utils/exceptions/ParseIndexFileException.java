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
package eu.openanalytics.rdepot.python.utils.exceptions;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import java.io.Serial;

public class ParseIndexFileException extends Exception {

    @Serial
    private static final long serialVersionUID = 8749249349059423139L;

    public ParseIndexFileException(String type) {
        super(StaticMessageResolver.getMessage(PythonMessageCodes.COULD_NOT_PARSE_INDEX_FILE) + " for " + type);
    }
}
