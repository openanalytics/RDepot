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
package eu.openanalytics.rdepot.test.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class PackageMaintainerDeleterTest extends UnitTest {
    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    PackageMaintainerService packageMaintainerService;

    @InjectMocks
    PackageMaintainerDeleter deleter;

    PackageMaintainer maintainer;
    User user;
    Repository repository;

    @BeforeEach
    public void setUpResources() throws Exception {
        user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        maintainer = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
        maintainer.setUser(user);
    }

    @Test
    public void delete() throws Exception {
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(PackageMaintainer.class));
        doNothing().when(packageMaintainerService).delete(any(PackageMaintainer.class));

        deleter.delete(maintainer);

        verify(newsfeedEventService, times(1)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(packageMaintainerService, times(1)).delete(any(PackageMaintainer.class));
    }

    @Test
    public void delete_throwsNPE_whenTryingToDeleteNullMaintainer() throws Exception {
        assertThrows(NullPointerException.class, () -> deleter.delete(null));

        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(packageMaintainerService, times(0)).delete(any(PackageMaintainer.class));
    }
}
