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
package eu.openanalytics.rdepot.python.validation;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.validation.RepositoryValidator;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.validation.repositories.PythonNameValidation;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class PythonRepositoryValidator extends RepositoryValidator<PythonRepository> {

    public PythonRepositoryValidator(
            PythonRepositoryService repositoryService,
            RepositoryService<Repository> commonRepositoryService,
            PythonNameValidation pythonNameValidator) {
        super(repositoryService, commonRepositoryService, pythonNameValidator);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PythonRepository.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validate((PythonRepository) target, errors);
    }
}
