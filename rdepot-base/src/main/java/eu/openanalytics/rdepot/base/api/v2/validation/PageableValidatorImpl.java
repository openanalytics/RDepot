/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.validation;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UnrecognizedQueryParameterException;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@Slf4j
public class PageableValidatorImpl implements PageableValidator {

    protected final MessageSource messageSource;
    protected static final Locale locale = LocaleContextHolder.getLocale();

    @Override
    public void validate(Class<? extends IDto> dtoClass, DtoResolvedPageable pageable)
            throws UnrecognizedQueryParameterException {

        if (pageable.getDtoSort().isUnsorted()) return;

        Set<String> fieldsToSortBy = pageable.getDtoSort().toSet().stream()
                .map(field -> field.toString().substring(0, field.toString().indexOf(":")))
                .collect(Collectors.toSet());

        Set<String> dtoFields = getDtoFields(dtoClass);

        for (String field : fieldsToSortBy) {
            if (validateField(field, dtoFields)) continue;
            throw new UnrecognizedQueryParameterException(Collections.singletonList(field), messageSource, locale);
        }
    }

    private boolean validateField(String field, Set<String> dtoFields) {
        return dtoFields.contains(field);
    }

    private Set<String> getDtoFields(Class<? extends IDto> dtoClass) {
        Set<String> dtoFields =
                Arrays.stream(dtoClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());

        if (!dtoClass.getSuperclass().equals(Object.class)) {
            dtoFields.addAll(Arrays.stream(dtoClass.getSuperclass().getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toSet()));
        }

        dtoFields.addAll(getProjectionFields(dtoClass));

        if (dtoClass.equals(SubmissionDto.class)) {
            dtoFields.addAll(getFieldsOfSubmissionDtoClass(dtoClass));
        }

        return dtoFields;
    }

    private Set<String> getProjectionFields(Class<? extends IDto> dtoClass) {
        Set<String> dtoProjectionFields = Arrays.stream(
                        dtoClass.getSuperclass().equals(Object.class)
                                ? dtoClass.getDeclaredFields()
                                : dtoClass.getSuperclass().getDeclaredFields())
                .filter(dtoField -> dtoField.getType().toString().contains("Projection"))
                .map(Field::getName)
                .collect(Collectors.toSet());

        Set<String> projectionFields = new HashSet<>();

        if (!dtoProjectionFields.isEmpty()) {
            for (String nameOfProjectionField : dtoProjectionFields) {

                final Field projectionField;
                try {
                    projectionField = dtoClass.getSuperclass().equals(Object.class)
                            ? dtoClass.getDeclaredField(nameOfProjectionField)
                            : dtoClass.getSuperclass().getDeclaredField(nameOfProjectionField);
                } catch (NoSuchFieldException | SecurityException e) {
                    log.error(e.getMessage(), e);
                    throw new IllegalStateException("Reflection error");
                }

                projectionFields.addAll(Arrays.stream(projectionField.getType().getDeclaredFields())
                        .map(dtoField -> nameOfProjectionField + '.' + dtoField.getName())
                        .collect(Collectors.toSet()));
            }
        }

        return projectionFields;
    }

    private Set<String> getFieldsOfSubmissionDtoClass(Class<? extends IDto> dtoClass) {

        ParameterizedType parameterizedType = (ParameterizedType) dtoClass.getGenericSuperclass();
        Class<?> genericClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

        Set<String> submissionDtoFields = Arrays.stream(
                        genericClass.getSuperclass().getDeclaredFields())
                .map(dtoField -> "packageBag." + dtoField.getName())
                .collect(Collectors.toSet());

        Set<String> projectionFieldsOfGenericClass = Arrays.stream(
                        genericClass.getSuperclass().getDeclaredFields())
                .filter(dtoField -> dtoField.getType().toString().contains("Projection"))
                .map(Field::getName)
                .collect(Collectors.toSet());

        for (String nameOfProjectionField : projectionFieldsOfGenericClass) {

            final Field projectionField;
            try {
                projectionField = genericClass.getSuperclass().getDeclaredField(nameOfProjectionField);
            } catch (NoSuchFieldException | SecurityException e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException("Reflection error");
            }

            submissionDtoFields.addAll(Arrays.stream(projectionField.getType().getDeclaredFields())
                    .map(dtoField -> "packageBag." + nameOfProjectionField + '.' + dtoField.getName())
                    .collect(Collectors.toSet()));
        }

        return submissionDtoFields;
    }
}
