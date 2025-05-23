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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.time.DateProvider;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class RepositoryTestFixture {

    public static List<Repository> GET_EXAMPLE_REPOSITORIES() {
        Repository repository1 = new Repository() {
            private static final long serialVersionUID = 1L;
        };
        repository1.setId(123);
        repository1.setName("Test RDepot Repository");
        repository1.setPublicationUri("http://localhost/repo/testrepo123");
        repository1.setServerAddress("http://192.168.1.100/testrepo123");
        repository1.setPublished(true);
        repository1.setSynchronizing(false);
        repository1.setVersion(10);
        repository1.setDeleted(false);
        repository1.setLastPublicationSuccessful(true);
        repository1.setLastModifiedTimestamp(DateProvider.now());
        repository1.setLastPublicationTimestamp(DateProvider.now());
        repository1.setRequiresAuthentication(true);

        Repository repository2 = new Repository() {
            private static final long serialVersionUID = 1L;
        };
        repository2.setId(456);
        repository2.setName("Just another RDepot repository");
        repository2.setPublicationUri("http://localhost/repo/anotherrepo");
        repository2.setServerAddress("http://192.168.1.102/anotherrepo");
        repository2.setPublished(false);
        repository2.setSynchronizing(true);
        repository2.setVersion(5);
        repository2.setLastPublicationSuccessful(false);
        repository2.setLastModifiedTimestamp(DateProvider.now());
        repository2.setRequiresAuthentication(false);

        Repository repository3 = new Repository() {
            private static final long serialVersionUID = 1L;
        };
        repository3.setId(234);
        repository3.setName("Totally different RDepot repository");
        repository3.setPublicationUri("http://localhost/repo/differentrepo");
        repository3.setServerAddress("http://192.168.1.102/differentrepo");
        repository3.setPublished(false);
        repository3.setSynchronizing(false);
        repository3.setVersion(12);
        repository3.setDeleted(true);
        repository3.setLastPublicationSuccessful(false);
        repository3.setLastModifiedTimestamp(DateProvider.now());
        repository1.setRequiresAuthentication(false);

        return List.of(repository1, repository2, repository3);
    }

    public static Page<Repository> GET_EXAMPLE_REPOSITORIES_PAGED() {
        return new PageImpl<>(GET_EXAMPLE_REPOSITORIES());
    }

    public static Repository GET_EXAMPLE_REPOSITORY() {
        return GET_EXAMPLE_REPOSITORIES().get(0);
    }

    public static Repository GET_EXAMPLE_REPOSITORY(int id) {
        Repository repository = GET_EXAMPLE_REPOSITORIES().get(0);
        repository.setId(id);
        return repository;
    }

    public static Repository GET_NEW_REPOSITORY() {
        Repository repository = GET_EXAMPLE_REPOSITORY(0);
        repository.setName("New Repository");
        repository.setVersion(1);
        return repository;
    }
}
