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
package eu.openanalytics.rdepot.test.unit.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import eu.openanalytics.rdepot.r.utils.RPackageRepositoryResolver;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RSubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

public class RPackageDeleterTest extends UnitTest {

    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    RPackageService packageService;

    @Mock
    RLocalStorage storage;

    @Mock
    SubmissionService submissionService;

    @Mock
    RRepositorySynchronizer repositorySynchronizer;

    @Mock
    RPackageRepositoryResolver packageRepositoryResolver;

    @InjectMocks
    RPackageDeleter deleter;

    private static final String OLD_SOURCE = "/upload_folder/package.tar.gz";
    private static final String TRASHED_SOURCE = "/trash/package.tar.gz";
    private RRepository repository;
    private User user;
    private RPackage packageBag;
    private Submission submission;

    @BeforeEach
    public void setUp() {
        repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        user = UserTestFixture.GET_REGULAR_USER();
        packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
        packageBag.setSubmission(submission);
        packageBag.setSource(OLD_SOURCE);

        new StaticMessageResolver(messageSource);
    }

    @Test
    public void delete() throws Exception {
        when(storage.moveToTrashDirectory(packageBag)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(packageBag);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doNothing().when(storage).removePackageSource(TRASHED_SOURCE);

        deleter.delete(packageBag);

        verify(newsfeedEventService).deleteRelatedEvents(packageBag);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
    }

    @Test
    public void delete_throwsExceptionAndResotresSources_whenDataAccessExceptionOccurs() throws Exception {
        final DataAccessException exception = new DataAccessException("message") {
            private static final long serialVersionUID = 909822155280557269L;
        };

        when(storage.moveToTrashDirectory(packageBag)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(packageBag);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doThrow(exception).when(submissionService).delete(submission);
        when(storage.moveSource(packageBag, OLD_SOURCE)).thenReturn(OLD_SOURCE);

        assertThrows(DeleteEntityException.class, () -> deleter.delete(packageBag));
        assertEquals(
                OLD_SOURCE, packageBag.getSource(), "Package source has not been moved back to its previous place.");

        verify(newsfeedEventService).deleteRelatedEvents(packageBag);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
    }

    @Test
    public void delete_throwsException_whenPackageSourceFileCannotBeDeleted() throws Exception {
        final SourceFileDeleteException exception = new SourceFileDeleteException();

        when(storage.moveToTrashDirectory(packageBag)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(packageBag);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doThrow(exception).when(storage).removePackageSource(TRASHED_SOURCE);

        assertThrows(DeleteEntityException.class, () -> deleter.delete(packageBag));

        verify(newsfeedEventService).deleteRelatedEvents(packageBag);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
    }

    @Test
    public void delete_throwsException_whenPackageSourceFileCannotBeMoved() throws Exception {
        final MovePackageSourceException exception = new MovePackageSourceException();

        doThrow(exception).when(storage).moveToTrashDirectory(packageBag);

        assertThrows(DeleteEntityException.class, () -> deleter.delete(packageBag));
    }

    @Test
    public void deleteById() throws Exception {
        final int id = packageBag.getId();

        when(packageService.findById(id)).thenReturn(Optional.of(packageBag));
        when(storage.moveToTrashDirectory(packageBag)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(packageBag);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doNothing().when(storage).removePackageSource(TRASHED_SOURCE);

        deleter.deleteTransactional(id);

        verify(newsfeedEventService).deleteRelatedEvents(packageBag);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
    }

    @Test
    public void deleteForSubmission() throws Exception {
        final int id = packageBag.getId();

        when(packageService.findById(id)).thenReturn(Optional.of(packageBag));
        when(storage.moveToTrashDirectory(packageBag)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(packageBag);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doNothing().when(storage).removePackageSource(TRASHED_SOURCE);

        deleter.deleteForSubmission(submission);

        verify(newsfeedEventService).deleteRelatedEvents(packageBag);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
    }
}
