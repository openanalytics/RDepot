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
package eu.openanalytics.rdepot.test.validator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;
import eu.openanalytics.rdepot.r.validation.repositories.RBasicNameValidator;
import eu.openanalytics.rdepot.r.validation.repositories.RNameValidation;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

@ExtendWith(MockitoExtension.class)
public class RRepositoryValidatorTest {

    @Mock
    RRepositoryService rRepositoryService;

    @Mock
    RepositoryService<Repository> repositoryService;

    private RRepositoryValidator repositoryValidator;

    @BeforeEach
    public void init() {
        RNameValidation nameValidation =
                new RNameValidation(rRepositoryService, new RBasicNameValidator("[A-Za-z0-9 \\-_.]+", ".+"));
        repositoryValidator = new RRepositoryValidator(rRepositoryService, repositoryService, nameValidation);
    }

    @Test
    public void updateRepository_shouldNotAllowChangingRepositoryVersion() {
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        RRepository updatedRepository = new RRepository(repository);
        updatedRepository.setVersion(100);

        DataBinder dataBinder = new DataBinder(updatedRepository);
        dataBinder.setValidator(repositoryValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        when(repositoryService.findById(repository.getId())).thenReturn((Optional.of(repository)));

        repositoryValidator.validate(updatedRepository, errors);
        verify(errors, times(1)).rejectValue("version", MessageCodes.FORBIDDEN_UPDATE);
    }
}
