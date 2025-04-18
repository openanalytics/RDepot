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

import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class RRepositoryTestFixture {

    public static List<RRepository> GET_EXAMPLE_REPOSITORIES() {
        RRepository repository1 = new RRepository();
        repository1.setId(123);
        repository1.setName("Test R Repository");
        repository1.setPublicationUri("http://localhost/repo/testrepo123");
        repository1.setServerAddress("http://192.168.1.100/testrepo123");
        repository1.setPublished(true);
        repository1.setSynchronizing(false);
        repository1.setVersion(10);
        repository1.setDeleted(false);
        repository1.setLastModifiedTimestamp(DateProvider.now());
        repository1.setLastPublicationTimestamp(DateProvider.now());
        repository1.setLastPublicationSuccessful(true);
        repository1.setRequiresAuthentication(true);

        RRepository repository2 = new RRepository();
        repository2.setId(456);
        repository2.setName("Just another R repository");
        repository2.setPublicationUri("http://localhost/repo/anotherrepo");
        repository2.setServerAddress("http://192.168.1.102/anotherrepo");
        repository2.setPublished(false);
        repository2.setSynchronizing(true);
        repository2.setVersion(5);
        repository2.setLastModifiedTimestamp(DateProvider.now());
        repository2.setLastPublicationSuccessful(false);
        repository2.setRequiresAuthentication(false);

        RRepository repository3 = new RRepository();
        repository3.setId(234);
        repository3.setName("Totally different R repository");
        repository3.setPublicationUri("http://localhost/repo/differentrepo");
        repository3.setServerAddress("http://192.168.1.102/differentrepo");
        repository3.setPublished(false);
        repository3.setSynchronizing(false);
        repository3.setVersion(12);
        repository3.setDeleted(true);
        repository3.setLastModifiedTimestamp(DateProvider.now());
        repository3.setLastPublicationSuccessful(false);
        repository3.setRequiresAuthentication(false);

        return List.of(repository1, repository2, repository3);
    }

    public static Page<RRepository> GET_EXAMPLE_REPOSITORIES_PAGED() {
        return new PageImpl<>(GET_EXAMPLE_REPOSITORIES());
    }

    public static RRepository GET_EXAMPLE_REPOSITORY() {
        return GET_EXAMPLE_REPOSITORIES().get(0);
    }

    public static RRepository GET_EXAMPLE_REPOSITORY(int id) {
        RRepository repository = GET_EXAMPLE_REPOSITORIES().get(0);
        repository.setId(id);
        return repository;
    }

    public static RRepository GET_NEW_REPOSITORY() {
        RRepository repository = GET_EXAMPLE_REPOSITORY(0);
        repository.setName("New R Repository");
        repository.setVersion(1);
        return repository;
    }
}
