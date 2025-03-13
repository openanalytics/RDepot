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
package eu.openanalytics.rdepot.base.validation;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.validation.repositories.CommonNameValidator;
import eu.openanalytics.rdepot.base.validation.repositories.IdValidation;
import eu.openanalytics.rdepot.base.validation.repositories.PublicationUriValidation;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class CommonRepositoryValidator {

    private final PublicationUriValidation<Repository> publicationUriValidation;
    private final IdValidation<Repository> idValidation;
    private final CommonNameValidator nameValidator;

    public CommonRepositoryValidator(RepositoryService<Repository> service) {
        publicationUriValidation = new PublicationUriValidation<>(service);
        idValidation = new IdValidation<>(service);
        nameValidator = new CommonNameValidator(service);
    }

    public void validateField(Repository repository, Errors errors) {
        ChainValidator<Repository> validator = getValidationChain();
        validator.validate(repository, errors);
    }

    private ChainValidator<Repository> getValidationChain() {
        publicationUriValidation.setNext(idValidation);
        nameValidator.setNext(publicationUriValidation);
        return nameValidator;
    }
}
