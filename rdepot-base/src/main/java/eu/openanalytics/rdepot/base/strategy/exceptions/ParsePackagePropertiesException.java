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
package eu.openanalytics.rdepot.base.strategy.exceptions;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.exception.LocalizedException;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import java.io.Serial;
import java.util.Properties;

/**
 * Thrown when {@link Properties} object of certain {@link Package}
 * lacks a required key or there is one with an invalid value.
 */
public class ParsePackagePropertiesException extends LocalizedException {
    @Serial
    private static final long serialVersionUID = -2867675359668519565L;

    public ParsePackagePropertiesException() {
        super(MessageCodes.COULD_NOT_PARSE_PACKAGE_PROPERTIES);
    }
}
