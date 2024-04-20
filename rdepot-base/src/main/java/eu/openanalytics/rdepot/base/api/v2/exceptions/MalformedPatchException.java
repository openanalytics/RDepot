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
package eu.openanalytics.rdepot.base.api.v2.exceptions;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.Locale;

/**
 * Thrown when a PATCH request is invalid.
 */
public class MalformedPatchException extends ApiException {

    @Serial
    private static final long serialVersionUID = -5220688592909790424L;

    public MalformedPatchException(MessageSource messageSource, Locale locale, Exception e) {
        super(messageSource, locale, MessageCodes.ERROR_MALFORMED_PATCH,
                HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

}