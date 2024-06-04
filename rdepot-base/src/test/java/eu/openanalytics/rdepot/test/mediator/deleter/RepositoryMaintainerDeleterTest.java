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
package eu.openanalytics.rdepot.test.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class RepositoryMaintainerDeleterTest extends UnitTest {
    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    RepositoryMaintainerService repositoryMaintainerService;

    @InjectMocks
    RepositoryMaintainerDeleter deleter;

    User user;
    Repository repository;
    RepositoryMaintainer maintainer;

    @BeforeEach
    public void setUpResources() throws Exception {
        user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        maintainer = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(repository);
        maintainer.setUser(user);
    }

    @Test
    public void delete_throwsNPE_whenTryingToDeleteNullMaintainer() throws Exception {
        assertThrows(NullPointerException.class, () -> deleter.delete(null));

        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(repositoryMaintainerService, times(0)).delete(any(RepositoryMaintainer.class));
    }

    @Test
    public void delete() throws Exception {
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(RepositoryMaintainer.class));
        doNothing().when(repositoryMaintainerService).delete(any(RepositoryMaintainer.class));

        deleter.delete(maintainer);

        verify(newsfeedEventService, times(1)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(repositoryMaintainerService, times(1)).delete(any(RepositoryMaintainer.class));
    }
}
