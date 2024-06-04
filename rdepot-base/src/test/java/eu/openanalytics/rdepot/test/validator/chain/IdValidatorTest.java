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
package eu.openanalytics.rdepot.test.validator.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.validation.exceptions.RepositoryValidationException;
import eu.openanalytics.rdepot.base.validation.repositories.IdValidation;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IdValidatorTest extends SingleChainValidatorTest {

    private IdValidation<Repository> validation;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        validation = new IdValidation<Repository>(repositoryService);
    }

    @Test
    public void createRepository_whenRepositoryNotFoundInDataBase()
            throws CreateException, RepositoryValidationException {
        when(repositoryService.findById(anyInt())).thenReturn(Optional.ofNullable(null));
        validation.validate(repository, bindingResult);
        expectedBindingResult.rejectValue("id", MessageCodes.REPOSITORY_NOT_FOUND);
        assertEquals(expectedBindingResult, bindingResult);
    }

    @Test
    public void createRepository_whenRepositoryInDataBaseHasTheSameVersion()
            throws CreateException, RepositoryValidationException {
        expectedRepository.setVersion(2);
        when(repositoryService.findById(anyInt())).thenReturn(Optional.of(expectedRepository));
        validation.validate(repository, bindingResult);
        expectedBindingResult.rejectValue("version", MessageCodes.FORBIDDEN_UPDATE);
        assertEquals(expectedBindingResult, bindingResult);
    }

    @Test
    public void createRepository_shouldPass_whenRepositoryExistsInDatabaseAnHasTheSameVerion()
            throws CreateException, RepositoryValidationException {
        when(repositoryService.findById(anyInt())).thenReturn(Optional.of(repository));
        validation.validate(repository, bindingResult);
        assertEquals(expectedBindingResult, bindingResult);
    }

    @Test
    public void createRepository_shouldPass_whenRepositoryIsNew()
            throws CreateException, RepositoryValidationException {
        repository.setId(-1);
        validation.validate(repository, bindingResult);
        assertEquals(expectedBindingResult, bindingResult);
    }
}
