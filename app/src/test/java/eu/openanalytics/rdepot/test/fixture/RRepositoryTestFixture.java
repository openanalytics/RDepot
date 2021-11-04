/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import eu.openanalytics.rdepot.model.Repository;

public class RRepositoryTestFixture {

	public static List<Repository> GET_EXAMPLE_REPOSITORIES() {
		Repository repository1 = new Repository(123, "http://localhost/repo/testrepo123", "Test Repository 123", "http://192.168.1.100/testrepo123", true, false, null, null, null, null);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		
		Repository repository2 = new Repository(234, "http://localhost/repo/testrepo234", "Test Repository 234", "http://192.168.1.101/testrepo234", false, true, null, null, null, null);
		repository2.setSynchronizing(false);
		repository2.setVersion(12);
		
		Repository repository3 = new Repository(456, "http://localhost/repo/anotherrepo", "Just another repository", "http://192.168.1.102/anotherrepo", false, false, null, null, null, null);
		repository3.setSynchronizing(true);
		repository3.setVersion(5);
		
		return List.of(repository1, repository2, repository3);
	}
	
	public static Page<Repository> GET_EXAMPLE_REPOSITORIES_PAGED() {
		Repository repository1 = new Repository(123, "http://localhost/repo/testrepo123", "Test Repository 123", "http://192.168.1.100/testrepo123", true, false, null, null, null, null);
		repository1.setSynchronizing(false);
		repository1.setVersion(10);
		
		Repository repository2 = new Repository(234, "http://localhost/repo/testrepo234", "Test Repository 234", "http://192.168.1.101/testrepo234", false, true, null, null, null, null);
		repository2.setSynchronizing(false);
		repository2.setVersion(12);
		
		Repository repository3 = new Repository(456, "http://localhost/repo/anotherrepo", "Just another repository", "http://192.168.1.102/anotherrepo", false, false, null, null, null, null);
		repository3.setSynchronizing(true);
		repository3.setVersion(5);
		
		return new PageImpl<>(List.of(repository1, repository2, repository3));		
	}

	public static Repository GET_EXAMPLE_REPOSITORY() {
		return GET_EXAMPLE_REPOSITORIES().get(0);
	}
	
	public static Repository GET_EXAMPLE_REPOSITORY(int id) {
		Repository repository = new Repository(id, "http://localhost/repo/testrepo" + id, "Test Repository " + id, "http://192.168.1.100/testrepo" + id, false, false, null, null, null, null);
		repository.setSynchronizing(false);
		repository.setVersion(0);
		
		return repository;
	}
}