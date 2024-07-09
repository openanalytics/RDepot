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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**
 * Thrown for API requests when something goes wrong.
 * They include error details, as well as
 * corresponding {@link HttpStatus HTTP Status}.
 * It is caught and converted into a proper HTTP response
 * by the
 * {@link eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2ErrorController Error Controller}.
 */
@Getter
public class ApiException extends Exception implements Serializable {

    @Serial
    private static final long serialVersionUID = 7207340302826282335L;

    private final String messageCode;
    private final HttpStatus httpStatus;

    @Setter
    private transient Optional<String> details = Optional.empty();

    public ApiException(MessageSource messageSource, Locale locale, String messageCode, HttpStatus httpStatus) {
        super(messageSource.getMessage(messageCode, null, messageCode, locale));

        this.messageCode = messageCode;
        this.httpStatus = httpStatus;
    }

    public ApiException(
            MessageSource messageSource, Locale locale, String messageCode, HttpStatus httpStatus, String details) {
        this(messageSource, locale, messageCode, httpStatus);

        this.setDetails(Optional.of(details));
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException, ClassNotFoundException, NotSerializableException {
        out.defaultWriteObject();
        out.writeObject(getDetails());
    }

    private void readObject(ObjectInputStream in) throws IOException, NotSerializableException, ClassNotFoundException {
        in.defaultReadObject();
        details = Optional.of((String) in.readObject());
    }
}
