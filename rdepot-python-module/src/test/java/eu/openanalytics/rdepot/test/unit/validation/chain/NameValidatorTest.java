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
package eu.openanalytics.rdepot.test.unit.validation.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.validation.repositories.NameValidation;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.validation.exceptions.PythonReposiotryValidationError;

@ExtendWith(MockitoExtension.class)
public class NameValidatorTest extends SingleChainValidatorTest{
	
private NameValidation<PythonRepository> validation;
	
	@Override
	@BeforeEach
	public void init() {
		super.init();
		validation = new NameValidation<PythonRepository>(repositoryService);
	}
	
	@Test
	public void createRepository_whenRepositoryNameEqualsNull() throws CreateException, PythonReposiotryValidationError
	{
		repository.setName(null);
		validation.validate(repository, bindingResult);
		expectedBindingResult.rejectValue("name", MessageCodes.EMPTY_NAME);
		assertEquals(expectedBindingResult, bindingResult);	
	}
	
	@Test
	public void createRepository_whenRepositoryNameIsDuplicatedInOtherId() throws CreateException,  PythonReposiotryValidationError
	{
		expectedRepository.setId(99);
		when(repositoryService.findByName(repository.getName())).thenReturn(Optional.of(expectedRepository));
		validation.validate(repository, bindingResult);
		expectedBindingResult.rejectValue("name", MessageCodes.ERROR_DUPLICATE_NAME);
		assertEquals(expectedBindingResult, bindingResult);	
	}
	
	@Test
	public void createRepository_shouldPass_whenRepositoryNameIsDuplicatedButInTheSameId() throws CreateException,  PythonReposiotryValidationError
	{
		when(repositoryService.findByName(repository.getName())).thenReturn(Optional.of(repository));
		validation.validate(repository, bindingResult);
		assertEquals(expectedBindingResult, bindingResult);	
	}
	
	@Test
	public void createRepository_shouldPass_whenRepositoryNameIsNew() throws CreateException,  PythonReposiotryValidationError
	{
		when(repositoryService.findByName(repository.getName())).thenReturn(Optional.ofNullable(null));
		validation.validate(repository, bindingResult);
		assertEquals(expectedBindingResult, bindingResult);	
	}
}
