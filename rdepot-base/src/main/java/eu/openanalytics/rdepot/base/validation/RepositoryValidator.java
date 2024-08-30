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

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.validation.repositories.IdValidation;
import eu.openanalytics.rdepot.base.validation.repositories.NameValidation;
import eu.openanalytics.rdepot.base.validation.repositories.PublicationUriValidation;
import eu.openanalytics.rdepot.base.validation.repositories.ServerAddressValidation;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class RepositoryValidator<R extends Repository> implements Validator {

    private final PublicationUriValidation<R> publicationUriValidation;
    private final ServerAddressValidation<R> serverAddressValidation;
    private final NameValidation<R> nameValidation;
    private final IdValidation<R> idValidation;

    protected RepositoryValidator(RepositoryService<R> service) {
        publicationUriValidation = new PublicationUriValidation<>(service);
        serverAddressValidation = new ServerAddressValidation<>(service);
        nameValidation = new NameValidation<>(service);
        idValidation = new IdValidation<>(service);
    }

    private ChainValidator<R> getValidationChain() {
        publicationUriValidation.setNext(serverAddressValidation);
        nameValidation.setNext(publicationUriValidation);
        idValidation.setNext(nameValidation);
        return idValidation;
    }

    protected void validate(R repository, Errors errors) {
        ChainValidator<R> validator = getValidationChain();
        validator.validate(repository, errors);
    }
}
