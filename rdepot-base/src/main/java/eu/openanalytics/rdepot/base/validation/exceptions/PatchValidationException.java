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

import eu.openanalytics.rdepot.base.exception.LocalizedException;
import java.io.Serial;

/**
 * Thrown when a Patch operation is invalid.
 * Should be used for a situation when the Patch object itself is correct,
 * but what it attempts to do is illegal.
 */
public class PatchValidationException extends LocalizedException {

    @Serial
    private static final long serialVersionUID = -8858509190487589756L;

    public PatchValidationException(String messageCode) {
        super(messageCode);
    }
}
