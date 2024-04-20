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

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;

@ExtendWith(MockitoExtension.class)
public class SingleChainValidatorTest {

	@Mock
	PythonRepositoryService repositoryService;
	
	protected PythonRepository repository;
	protected PythonRepository expectedRepository;
	protected BindingResult expectedBindingResult;
	protected BindingResult bindingResult;
	
	public void init() {
		repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(100);
		expectedRepository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(100);
		expectedBindingResult = createDataBinding(repository);
		bindingResult = createDataBinding(repository);
	}
	
	private BindingResult createDataBinding(PythonRepository repository) {
		final DataBinder dataBinder = new DataBinder(repository);
		BindingResult bindingResult = dataBinder.getBindingResult();
		return bindingResult;
	}

}
