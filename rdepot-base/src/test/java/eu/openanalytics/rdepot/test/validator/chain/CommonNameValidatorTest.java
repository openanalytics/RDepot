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
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.validation.repositories.CommonNameValidator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommonNameValidatorTest extends SingleChainValidatorTest {

    private static CommonNameValidator validator;

    @Override
    @BeforeEach
    public void init() {
        super.init();
        validator = new CommonNameValidator(repositoryService);
    }

    @Test
    public void createRepository_whenRepositoryNameIsDuplicatedInOtherId() {
        expectedRepository.setId(99);
        when(repositoryService.findByName(repository.getName())).thenReturn(Optional.of(expectedRepository));
        validator.validate(repository, bindingResult);
        expectedBindingResult.rejectValue("name", MessageCodes.ERROR_DUPLICATE_NAME);
        assertEquals(expectedBindingResult, bindingResult);
    }
}
