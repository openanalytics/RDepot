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
package eu.openanalytics.rdepot.test.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonSubmissionDeleter;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class PythonRepositoryDeleterTest extends UnitTest {

    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    PythonRepositoryService pythonRepositoryService;

    @Mock
    PackageMaintainerService packageMaintainerService;

    @Mock
    RepositoryMaintainerService repositoryMaintainerService;

    @Mock
    Storage<PythonPackage> storage;

    @Mock
    PythonSubmissionDeleter submissionDeleter;

    @Mock
    PythonPackageService packageService;

    @InjectMocks
    PythonRepositoryDeleter deleter;

    User user;
    PythonRepository repository;
    List<PackageMaintainer> packageMaintainers;
    List<RepositoryMaintainer> repositoryMaintainers;
    List<PythonPackage> packages;

    @BeforeEach
    public void setUpResources() {
        user = UserTestFixture.GET_REGULAR_USER();
        repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        packageMaintainers = PackageMaintainerTestFixture.GET_EXAMPLE_PACKAGE_MAINTAINERS(repository);
        repositoryMaintainers =
                RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINERS_FOR_REPOSITORY(3, repository);
        packages = PythonPackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3, 123);
    }

    @Test
    public void delete() throws Exception {
        when(packageMaintainerService.findByRepository(repository)).thenReturn(packageMaintainers);
        doNothing().when(packageMaintainerService).delete(any(PackageMaintainer.class));
        when(repositoryMaintainerService.findByRepository(repository)).thenReturn(repositoryMaintainers);
        doNothing().when(repositoryMaintainerService).delete(any(RepositoryMaintainer.class));
        when(packageService.findAllByRepositoryIncludeDeleted(repository)).thenReturn(packages);
        doNothing().when(submissionDeleter).delete(any(Submission.class));
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(Resource.class));
        doNothing().when(pythonRepositoryService).delete(any(PythonRepository.class));

        deleter.delete(repository);

        verify(newsfeedEventService, times(1)).deleteRelatedEvents(any(PythonRepository.class));
        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(Submission.class));
        verify(packageMaintainerService, times(3)).delete(any(PackageMaintainer.class));
        verify(repositoryMaintainerService, times(3)).delete(any(RepositoryMaintainer.class));
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
        verify(storage, times(0)).removePackageSource(packages.get(0).getSource());
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

        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(packageMaintainerService, times(3)).delete(any(PackageMaintainer.class));
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

        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(PackageMaintainer.class));
        verify(newsfeedEventService, times(3)).deleteRelatedEvents(any(RepositoryMaintainer.class));
        verify(newsfeedEventService, times(1)).deleteRelatedEvents(any(Submission.class));
        verify(packageMaintainerService, times(3)).delete(any(PackageMaintainer.class));
        verify(repositoryMaintainerService, times(3)).delete(any(RepositoryMaintainer.class));
    }
}
