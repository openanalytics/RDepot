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

import java.util.List;
import lombok.NonNull;

/**
 * {@link ValidationResult Validation Result Object} that also carries
 * additional data that can be helpful in understanding the result.
 * @param <T> type of data attached to validation item
 */
public interface DataSpecificValidationResult<T> extends ValidationResult {

    void error(@NonNull String property, @NonNull String errorCode, T data);

    void warning(@NonNull String property, @NonNull String warningCode, T data);

    List<ValidationResultItem<T>> getDataSpecificErrors();

    List<ValidationResultItem<T>> getDataSpecificWarnings();
}
