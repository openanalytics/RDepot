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
import java.io.Serial;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a resource cannot be deleted due to an internal error.
 */
public class DeleteException extends ApiException {

    @Serial
    private static final long serialVersionUID = 6602106189983212106L;

    public DeleteException(MessageSource messageSource, Locale locale) {
        super(messageSource, locale, MessageCodes.ERROR_DELETE_RESOURCE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
