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
package eu.openanalytics.rdepot.python.test.strategy.fixture;

import eu.openanalytics.rdepot.python.entities.PythonRepository;

public class PythonRepositoryTestFixture {
	public static PythonRepository GET_NEW_REPOSITORY() {
		PythonRepository repository = GET_EXAMPLE_REPOSITORY();
		repository.setId(0);
		return repository;
	}
	
	public static PythonRepository GET_EXAMPLE_REPOSITORY() {
		PythonRepository repository = new PythonRepository();
		
		repository.setId(123);
		repository.setDeleted(false);
		repository.setName("NewPythonRepository");
		repository.setServerAddress("127.0.0.1");
		repository.setPublicationUri("example.com");
		repository.setVersion(0);
		
		return repository;
	}
}
