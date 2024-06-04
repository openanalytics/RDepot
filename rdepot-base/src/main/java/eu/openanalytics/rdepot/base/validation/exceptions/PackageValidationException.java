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
package eu.openanalytics.rdepot.base.validation.exceptions;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.validation.ValidationResultItem;
import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when {@link Package} is invalid.
 */
public class PackageValidationException extends Exception {

    @Serial
    private static final long serialVersionUID = -7473495079948527879L;

    public PackageValidationException(String messageCode) {
        super(messageCode);
    }

    public PackageValidationException(List<ValidationResultItem<?>> messageCodes) {
        super(messageCodes.stream().map(i -> "\"" + i.messageCode() + "\"").collect(Collectors.joining(", ")));
    }
}
