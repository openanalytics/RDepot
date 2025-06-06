/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.validation.repositories;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.validation.ChainValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;

/**
 * Validates name of {@link Repository}.
 * Should neither be empty, nor a duplicate of any existing repository.
 * @param <R> technology-specific repository class
 */
@Getter
@RequiredArgsConstructor
public class NameValidation<R extends Repository> extends ChainValidator<R> {

    private final RepositoryService<R> repositoryService;
    private final BasicNameValidator nameValidator;

    public void validateField(R repository, Errors errors) {
        switch (nameValidator.validateName(repository.getName())) {
            case EMPTY: {
                errors.rejectValue("name", MessageCodes.EMPTY_NAME);
                break;
            }
            case INVALID: {
                errors.rejectValue("name", MessageCodes.INVALID_REPOSITORY_NAME);
                break;
            }
            default:
                break;
        }
    }
}
