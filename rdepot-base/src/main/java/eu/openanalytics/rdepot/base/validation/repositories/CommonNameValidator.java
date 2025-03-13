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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;

@RequiredArgsConstructor
public class CommonNameValidator extends ChainValidator<Repository> {

    private final RepositoryService<Repository> repositoryService;

    @Override
    protected void validateField(Repository repository, Errors errors) {
        Optional<Repository> duplicateName = repositoryService.findByName(repository.getName());
        if (duplicateName.isPresent() && duplicateName.get().getId() != repository.getId()) {
            errors.rejectValue("name", MessageCodes.ERROR_DUPLICATE_NAME);
        }
    }
}
