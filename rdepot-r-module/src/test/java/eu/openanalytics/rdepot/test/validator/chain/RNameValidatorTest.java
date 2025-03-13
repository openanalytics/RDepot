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
package eu.openanalytics.rdepot.test.validator.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.validation.repositories.RBasicNameValidator;
import eu.openanalytics.rdepot.r.validation.repositories.RNameValidation;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

@ExtendWith(MockitoExtension.class)
public class RNameValidatorTest {

    @Mock
    RRepositoryService repositoryService;

    protected RRepository repository;
    protected BindingResult expectedBindingResult;
    protected BindingResult bindingResult;
    private RNameValidation nameValidation;

    @BeforeEach
    public void init() {
        repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(100);
        expectedBindingResult = createDataBinding(repository);
        bindingResult = createDataBinding(repository);
        nameValidation = new RNameValidation(repositoryService, new RBasicNameValidator("[A-Za-z0-9 \\-_.]+", ".+"));
    }

    private BindingResult createDataBinding(Repository repository) {
        final DataBinder dataBinder = new DataBinder(repository);
        return dataBinder.getBindingResult();
    }

    @Test
    public void createRepository_whenRepositoryNameIsNotAllowed() {
        repository.setName("testrepo #3");
        nameValidation.validate(repository, bindingResult);
        expectedBindingResult.rejectValue("name", MessageCodes.INVALID_REPOSITORY_NAME);
        assertEquals(expectedBindingResult, bindingResult);
    }
}
