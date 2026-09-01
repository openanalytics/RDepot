/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.entities.*;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mediator.deletion.RSubmissionDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.PersistentRStorage;
import eu.openanalytics.rdepot.test.fixture.*;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class RRepositoryDeleterTest extends UnitTest {
    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    RRepositoryService repositoryService;

    @Mock
    PackageMaintainerService packageMaintainerService;

    @Mock
    RepositoryMaintainerService repositoryMaintainerService;

    @Mock
    LocalStorage<RPackage> localStorage;

    @Mock
    RSubmissionDeleter submissionDeleter;

    @Mock
    RPackageService packageService;

    @InjectMocks
    RRepositoryDeleter deleter;

    @Mock
    PersistentRStorage persistentRStorage;

    private RRepository repository;
    private List<PackageMaintainer> packageMaintainers;
    private List<RepositoryMaintainer> repositoryMaintainers;
    private List<RPackage> packages;

    @BeforeEach
    public void setUpResources() {
        repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        packageMaintainers = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINERS_FOR_REPOSITORY(2, repository);
        repositoryMaintainers =
                RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINERS_FOR_REPOSITORY(2, repository);
        packages = RPackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3, 123);
    }

    @Test
    public void delete() throws Exception {
        when(packageMaintainerService.findByRepository(repository)).thenReturn(packageMaintainers);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(Resource.class));
        doNothing().when(packageMaintainerService).delete(any(PackageMaintainer.class));
        when(repositoryMaintainerService.findByRepository(repository)).thenReturn(repositoryMaintainers);
        doNothing().when(repositoryMaintainerService).delete(any(RepositoryMaintainer.class));
        when(packageService.findAllByRepositoryIncludeDeleted(repository)).thenReturn(packages);
        doNothing().when(submissionDeleter).delete(any(Submission.class));
        doNothing().when(repositoryService).delete(eq(repository));

        deleter.delete(repository);

        verify(newsfeedEventService, times(2)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(newsfeedEventService, times(2)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(Submission.class));
        verify(packageMaintainerService, times(2)).delete(any(PackageMaintainer.class));
        verify(repositoryMaintainerService, times(2)).delete(any(RepositoryMaintainer.class));
        verify(submissionDeleter, times(3)).delete(any(Submission.class));
    }

    @Test
    public void delete_throwsException_whenEventsCannotBeDeleted() throws Exception {
        final DeleteEntityException exception = new DeleteEntityException();
        when(packageMaintainerService.findByRepository(repository)).thenReturn(packageMaintainers);
        doThrow(exception).when(newsfeedEventService).deleteRelatedEvents(any(Resource.class));

        assertThrows(DeleteEntityException.class, () -> deleter.delete(repository));
    }

    @Test
    public void delete_throwsNPE_whenTryingToDeleteNullEvent() throws Exception {
        assertThrows(NullPointerException.class, () -> deleter.delete(null));

        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(Submission.class));
        verify(packageMaintainerService, times(0)).delete(any(PackageMaintainer.class));
        verify(repositoryMaintainerService, times(0)).delete(any(RepositoryMaintainer.class));
        verify(submissionDeleter, times(0)).delete(any(Submission.class));
        verify(persistentRStorage, times(0)).deleteAllPackageFilesIfExist(any());
    }

    @Test
    public void delete_throwsException_whenPackageMaintainerCannotBeDeleted() throws Exception {
        final DeleteEntityException exception = new DeleteEntityException();
        when(packageMaintainerService.findByRepository(repository)).thenReturn(packageMaintainers);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(Resource.class));
        doNothing().when(packageMaintainerService).delete(any(PackageMaintainer.class));
        when(repositoryMaintainerService.findByRepository(repository)).thenReturn(repositoryMaintainers);
        doThrow(exception).when(repositoryMaintainerService).delete(any(RepositoryMaintainer.class));

        assertThrows(DeleteEntityException.class, () -> deleter.delete(repository));

        verify(newsfeedEventService, times(2)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(packageMaintainerService, times(2)).delete(any(PackageMaintainer.class));
    }

    @Test
    public void delete_throwsException_whenSubmissionCannotBeDeleted() throws Exception {
        final DeleteEntityException exception = new DeleteEntityException();
        when(packageMaintainerService.findByRepository(repository)).thenReturn(packageMaintainers);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(Resource.class));
        doNothing().when(packageMaintainerService).delete(any(PackageMaintainer.class));
        when(repositoryMaintainerService.findByRepository(repository)).thenReturn(repositoryMaintainers);
        doNothing().when(repositoryMaintainerService).delete(any(RepositoryMaintainer.class));
        when(packageService.findAllByRepositoryIncludeDeleted(repository)).thenReturn(packages);
        doThrow(exception).when(submissionDeleter).delete(any(Submission.class));

        assertThrows(DeleteEntityException.class, () -> deleter.delete(repository));

        verify(newsfeedEventService, times(2)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(newsfeedEventService, times(2)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(newsfeedEventService, times(1)).deleteRelatedEvents(any(Submission.class));
        verify(packageMaintainerService, times(2)).delete(any(PackageMaintainer.class));
        verify(repositoryMaintainerService, times(2)).delete(any(RepositoryMaintainer.class));
    }
}
