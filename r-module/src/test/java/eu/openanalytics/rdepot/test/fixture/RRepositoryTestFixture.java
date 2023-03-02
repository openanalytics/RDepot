/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.test.fixture;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RRepositoryTestFixture {

	public static List<RRepository> GET_EXAMPLE_REPOSITORIES() {
		RRepository repository1 = new RRepository();
		repository1.setId(123);
		repository1.setName("Test Repository 123");
		repository1.setPublicationUri("http://localhost/repo/testrepo123");
		repository1.setServerAddress("http://192.168.1.100/testrepo123");
		repository1.setPublished(true);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		
		RRepository repository2 = new RRepository();
		repository1.setId(456);
		repository1.setName("Test Repository 456");
		repository1.setPublicationUri("http://localhost/repo/testrepo456");
		repository1.setServerAddress("http://192.168.1.100/testrepo456");
		repository1.setPublished(true);
		repository1.setSynchronizing(true);
		repository1.setVersion(5);
		
		RRepository repository3 = new RRepository();
		repository1.setId(123);
		repository1.setName("Test Repository 123");
		repository1.setPublicationUri("http://localhost/repo/testrepo123");
		repository1.setServerAddress("http://192.168.1.100/testrepo123");
		repository1.setPublished(true);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		
		return List.of(repository1, repository2, repository3);
	}
	
	public static Page<RRepository> GET_EXAMPLE_REPOSITORIES_PAGED() {
		RRepository repository1 = new RRepository();
		repository1.setId(123);
		repository1.setName("Test Repository 123");
		repository1.setPublicationUri("http://localhost/repo/testrepo123");
		repository1.setServerAddress("http://192.168.1.100/testrepo123");
		repository1.setPublished(true);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		repository1.setDeleted(false);
		
		RRepository repository2 = new RRepository();
		repository2.setId(456);
		repository2.setName("Just another repository");
		repository2.setPublicationUri("http://localhost/repo/anotherrepo");
		repository2.setServerAddress("http://192.168.1.102/anotherrepo");
		repository2.setPublished(false);
		repository2.setSynchronizing(true);
		repository2.setVersion(5);
		
		RRepository repository3 = new RRepository();
		repository3.setId(234);
		repository3.setName("Test Repository 234");
		repository3.setPublicationUri("http://localhost/repo/testrepo234");
		repository3.setServerAddress("http://192.168.1.101/testrepo234");
		repository3.setPublished(false);
		repository3.setSynchronizing(false);
		repository3.setVersion(12);
		repository3.setDeleted(true);
		
		return new PageImpl<>(List.of(repository1, repository2, repository3));		
	}

	public static RRepository GET_EXAMPLE_REPOSITORY() {
		return GET_EXAMPLE_REPOSITORIES().get(0);
	}
	
	public static RRepository GET_EXAMPLE_REPOSITORY(int id) {
		RRepository repository = new RRepository();
		
		repository.setId(id);
		repository.setDeleted(false);
		repository.setName("Test Repository 100");
		repository.setServerAddress("http://192.168.1.100/testrepo100");
		repository.setPublicationUri("http://localhost/repo/testrepo100");
		repository.setVersion(0);
		
//				<Repository<E,D>>(id, "http://localhost/repo/testrepo" + id, "Test Repository " + id, "http://192.168.1.100/testrepo" + id, false, false, null, null, null);
		repository.setSynchronizing(false);
		repository.setVersion(0);
		
		return repository;
	}
}