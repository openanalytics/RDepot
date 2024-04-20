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
package eu.openanalytics.rdepot.base.validation;

import lombok.NonNull;

import java.util.List;

/**
 * Contains validation results as pairs property-message codes.
 * Message Codes have to be resolved with
 * {@link eu.openanalytics.rdepot.base.messaging.StaticMessageResolver Message Resolver}.
 */
public interface ValidationResult {

    /**
     *  Registers validation error. Should disable request from completing.
     */
    void error(@NonNull String property, @NonNull String errorCode);

    /**
     *  Registers validation warning. User should be informed about it
     *  but the warning itself must not disable request from completing.
     */
    void warning(@NonNull String property, @NonNull String warningCode);

    /**
     * @return true if there are any errors
     */
    boolean hasErrors();

    /**
     * @return true if there are any warnings
     */
    boolean hasWarnings();

    /**
     * @return list of registered errors
     */
    List<ValidationResultItem<?>> getErrors();

    /**
     * @return list of registered warnings
     */
    List<ValidationResultItem<?>> getWarnings();
}
