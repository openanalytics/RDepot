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
package eu.openanalytics.rdepot.test.fixture;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.python.entities.PythonRepository;

public class RepositoryTestFixture {

	public static List<PythonRepository> GET_EXAMPLE_REPOSITORIES() {
		PythonRepository repository1 = new PythonRepository();
		repository1.setId(123);
		repository1.setName("Test Python Repository 123");
		repository1.setPublicationUri("http://localhost/repo/testrepo123");
		repository1.setServerAddress("http://192.168.1.100/testrepo123");
		repository1.setPublished(true);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		
		PythonRepository repository2 = new PythonRepository();
		repository2.setId(456);
		repository2.setName("Test Python Repository 456");
		repository2.setPublicationUri("http://localhost/repo/testrepo456");
		repository2.setServerAddress("http://192.168.1.100/testrepo456");
		repository2.setPublished(true);
		repository2.setSynchronizing(true);
		repository2.setVersion(5);
		
		PythonRepository repository3 = new PythonRepository();
		repository3.setId(321);
		repository3.setName("Test Python Repository 321");
		repository3.setPublicationUri("http://localhost/repo/testrepo321");
		repository3.setServerAddress("http://192.168.1.100/testrepo321");
		repository3.setPublished(true);
		repository3.setSynchronizing(false);
		repository3.setVersion(2);
		
		return List.of(repository1, repository2, repository3);
	}
	
	public static Page<Repository> GET_EXAMPLE_REPOSITORIES_PAGED() {
		PythonRepository repository1 = new PythonRepository();
		repository1.setId(123);
		repository1.setName("Test Python Repository 123");
		repository1.setPublicationUri("http://localhost/repo/testrepo123");
		repository1.setServerAddress("http://192.168.1.100/testrepo123");
		repository1.setPublished(true);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		
		PythonRepository repository2 = new PythonRepository();
		repository2.setId(456);
		repository2.setName("Just another python repository");
		repository2.setPublicationUri("http://localhost/repo/anotherrepo");
		repository2.setServerAddress("http://192.168.1.101/anotherrepo");
		repository2.setPublished(false);
		repository2.setSynchronizing(true);
		repository2.setVersion(5);
		
		PythonRepository repository3 = new PythonRepository();
		repository3.setId(321);
		repository3.setName("Test Python Repository 321");
		repository3.setPublicationUri("http://localhost/repo/testrepo321");
		repository3.setServerAddress("http://192.168.1.102/testrepo321");
		repository3.setPublished(false);
		repository3.setSynchronizing(false);
		repository3.setVersion(2);
		
		return new PageImpl<>(List.of(repository1, repository2, repository3));
	}
	
	public static PythonRepository GET_EXAMPLE_REPOSITORY(int id) {
		PythonRepository repository = new PythonRepository();
		
		repository.setId(id);
		repository.setDeleted(false);
		repository.setName("Test Python Repository 100");
		repository.setServerAddress("http://192.168.1.100/testrepo100");
		repository.setPublicationUri("http://localhost/repo/testrepo100");
		repository.setVersion(0);
		repository.setSynchronizing(false);
		repository.setVersion(0);
		
		return repository;
	}
	
	public static PythonRepository GET_EXAMPLE_REPOSITORY() {
		return GET_EXAMPLE_REPOSITORIES().get(0);
	}
}
