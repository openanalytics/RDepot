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
package eu.openanalytics.rdepot.base.validation.repositories;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.validation.ChainValidator;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;

/**
 * Validates {@link Repository Repository's} Publication URI.
 * Should neither be empty, nor point to the same URI as another repository.
 * @param <R> technology-specific repository class
 */
@AllArgsConstructor
public class PublicationUriValidation<R extends Repository> extends ChainValidator<R> {

    private final RepositoryService<R> service;

    public void validateField(R repository, Errors errors) {
        if (repository.getPublicationUri() == null
                || repository.getPublicationUri().isBlank()) {
            errors.rejectValue("publicationUri", MessageCodes.EMPTY_PUBLICATIONURI);
        } else {
            Optional<R> duplicatePublicationUri = service.findByPublicationUri(repository.getPublicationUri());
            if (duplicatePublicationUri.isPresent()
                    && duplicatePublicationUri.get().getId() != repository.getId()) {
                errors.rejectValue("publicationUri", MessageCodes.ERROR_DUPLICATE_PUBLICATIONURI);
            }
        }
    }
}
