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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

/**
 * Default implementation of {@link ValidationResult}
 */
public class ValidationResultImpl<T> implements DataSpecificValidationResult<T> {

    public static ValidationResult createResult() {
        return new ValidationResultImpl<>();
    }

    public static <T> DataSpecificValidationResult<T> createDataSpecificResult(Class<T> clazz) {
        return new ValidationResultImpl<>();
    }

    private ValidationResultImpl() {}

    private final List<ValidationResultItem<T>> errors = new ArrayList<>();
    private final List<ValidationResultItem<T>> warnings = new ArrayList<>();

    @Override
    public void error(@NonNull String property, @NonNull String errorCode) {
        errors.add(new ValidationResultItem<>(property, errorCode));
    }

    @Override
    public void warning(@NonNull String property, @NonNull String warningCode) {
        warnings.add(new ValidationResultItem<>(property, warningCode));
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public List<ValidationResultItem<?>> getErrors() {
        return errors.stream().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<ValidationResultItem<?>> getWarnings() {
        return warnings.stream().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void error(@NonNull String property, @NonNull String errorCode, T data) {
        errors.add(new ValidationResultItem<>(property, errorCode, data));
    }

    @Override
    public void warning(@NonNull String property, @NonNull String warningCode, T data) {
        warnings.add(new ValidationResultItem<>(property, warningCode, data));
    }

    @Override
    public List<ValidationResultItem<T>> getDataSpecificErrors() {
        return errors;
    }

    @Override
    public List<ValidationResultItem<T>> getDataSpecificWarnings() {
        return warnings;
    }
}
