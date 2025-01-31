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
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class PythonRepositoryTestFixture {

    public static List<PythonRepository> GET_EXAMPLE_REPOSITORIES() {
        PythonRepository repository1 = new PythonRepository();
        repository1.setId(123);
        repository1.setName("Test Python Repository");
        repository1.setPublicationUri("http://localhost/repo/testrepo");
        repository1.setServerAddress("http://192.168.1.100/testrepo");
        repository1.setPublished(true);
        repository1.setSynchronizing(false);
        repository1.setVersion(10);
        repository1.setLastModifiedTimestamp(DateProvider.now());
        repository1.setLastPublicationTimestamp(DateProvider.now());
        repository1.setLastPublicationSuccessful(true);
        repository1.setRequiresAuthentication(true);

        PythonRepository repository2 = new PythonRepository();
        repository2.setId(456);
        repository2.setName("Just another python repository");
        repository2.setPublicationUri("http://localhost/repo/anotherrepo");
        repository2.setServerAddress("http://192.168.1.101/anotherrepo");
        repository2.setPublished(false);
        repository2.setSynchronizing(true);
        repository2.setVersion(5);
        repository2.setLastModifiedTimestamp(DateProvider.now());
        repository2.setLastPublicationSuccessful(false);
        repository2.setRequiresAuthentication(false);

        PythonRepository repository3 = new PythonRepository();
        repository3.setId(321);
        repository3.setName("Totally different python repository");
        repository3.setPublicationUri("http://localhost/repo/differentrepo");
        repository3.setServerAddress("http://192.168.1.102/differentrepo");
        repository3.setPublished(false);
        repository3.setSynchronizing(false);
        repository3.setVersion(2);
        repository3.setLastModifiedTimestamp(DateProvider.now());
        repository3.setLastPublicationSuccessful(false);
        repository3.setRequiresAuthentication(false);

        return List.of(repository1, repository2, repository3);
    }

    public static Page<PythonRepository> GET_EXAMPLE_REPOSITORIES_PAGED() {
        return new PageImpl<>(GET_EXAMPLE_REPOSITORIES());
    }

    public static PythonRepository GET_EXAMPLE_REPOSITORY(int id) {
        PythonRepository repository = GET_EXAMPLE_REPOSITORIES().get(0);
        repository.setId(id);
        return repository;
    }

    public static PythonRepository GET_EXAMPLE_REPOSITORY() {
        return GET_EXAMPLE_REPOSITORIES().get(0);
    }

    public static PythonRepository GET_NEW_REPOSITORY() {
        PythonRepository repository = GET_EXAMPLE_REPOSITORY(0);
        repository.setName("New Python Repository");
        repository.setVersion(1);
        return repository;
    }
}
