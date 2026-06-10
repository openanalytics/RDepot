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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.storage.PythonPopulator;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonLocalStorage;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import eu.openanalytics.rdepot.python.utils.PythonPackageRepositoryResolver;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonSubmissionTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.io.Serial;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

public class PythonPackageDeleterTest extends UnitTest {

    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    PythonPackageService pythonPackageService;

    @InjectMocks
    PythonPackageDeleter deleter;

    @Mock
    PythonLocalStorage storage;

    @Mock
    PythonPopulator populator;

    @Mock
    SubmissionService submissionService;

    @Mock
    PackageMaintainerService maintainerService;

    @Mock
    PythonRepositorySynchronizer repositorySynchronizer;

    @Mock
    PythonPackageRepositoryResolver packageRepositoryResolver;

    PythonPackage pythonPackage;
    Submission submission;
    List<PackageMaintainer> maintainers;
    private static final String OLD_SOURCE = "/upload_folder/package.tar.gz";
    private static final String TRASHED_SOURCE = "/trash/package.tar.gz";

    @BeforeEach
    public void setUpResources() {
        pythonPackage = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(pythonPackage.getUser(), pythonPackage);
        pythonPackage.setSubmission(submission);
        pythonPackage.setSource(OLD_SOURCE);
        maintainers = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINERS_FOR_PACKAGE(pythonPackage);
    }

    @Test
    public void delete() throws Exception {
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(PythonPackage.class));
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(Submission.class));
        doNothing().when(submissionService).delete(any(Submission.class));
        when(maintainerService.findAllByPackageNameAndRepository(anyString(), any(PythonRepository.class)))
                .thenReturn(maintainers);
        when(populator.moveToTrashDirectory(pythonPackage)).thenReturn(TRASHED_SOURCE);
        doNothing().when(storage).removePackageSource(TRASHED_SOURCE);

        deleter.delete(pythonPackage);

        verify(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(maintainerService)
                .findAllByPackageNameAndRepository(pythonPackage.getName(), pythonPackage.getRepository());
        verify(storage).removePackageSource(TRASHED_SOURCE);
    }

    @Test
    public void delete_throwsNPE_whenTryingToDeleteNullPackage() throws Exception {
        assertThrows(NullPointerException.class, () -> deleter.delete(null));

        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(PythonPackage.class));
        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(Submission.class));
        verify(pythonPackageService, times(0)).delete(any(PythonPackage.class));
        verify(maintainerService, times(0)).findAllByPackageNameAndRepository(anyString(), any(PythonRepository.class));
    }

    @Test
    public void delete_throwsExceptionAndRestoresSources_whenDataAccessExceptionOccurs() throws Exception {
        final DataAccessException exception = new DataAccessException("message") {
            @Serial
            private static final long serialVersionUID = 909822155280557269L;
        };

        when(populator.moveToTrashDirectory(pythonPackage)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doThrow(exception).when(submissionService).delete(submission);
        when(storage.moveSource(pythonPackage, OLD_SOURCE)).thenReturn(OLD_SOURCE);

        assertThrows(DeleteEntityException.class, () -> deleter.delete(pythonPackage));
        assertEquals(
                OLD_SOURCE, pythonPackage.getSource(), "Package source has not been moved back to its previous place.");

        verify(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
    }

    @Test
    public void delete_throwsException_whenPackageSourceFileCannotBeDeleted() throws Exception {
        final SourceFileDeleteException exception = new SourceFileDeleteException();

        when(maintainerService.findAllByPackageNameAndRepository(anyString(), any(PythonRepository.class)))
                .thenReturn(maintainers);
        when(populator.moveToTrashDirectory(pythonPackage)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doThrow(exception).when(storage).removePackageSource(TRASHED_SOURCE);

        assertThrows(DeleteEntityException.class, () -> deleter.delete(pythonPackage));

        verify(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
        verify(maintainerService)
                .findAllByPackageNameAndRepository(pythonPackage.getName(), pythonPackage.getRepository());
    }

    @Test
    public void delete_throwsException_whenPackageSourceFileCannotBeMoved() throws Exception {
        final MovePackageSourceException exception = new MovePackageSourceException();

        doThrow(exception).when(populator).moveToTrashDirectory(pythonPackage);

        assertThrows(DeleteEntityException.class, () -> deleter.delete(pythonPackage));
    }

    @Test
    public void deleteById() throws Exception {
        final int id = pythonPackage.getId();

        when(pythonPackageService.findById(id)).thenReturn(Optional.of(pythonPackage));
        when(populator.moveToTrashDirectory(pythonPackage)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doNothing().when(storage).removePackageSource(TRASHED_SOURCE);
        when(maintainerService.findAllByPackageNameAndRepository(anyString(), any(PythonRepository.class)))
                .thenReturn(maintainers);

        deleter.deleteTransactional(id);

        verify(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
        verify(maintainerService)
                .findAllByPackageNameAndRepository(pythonPackage.getName(), pythonPackage.getRepository());
    }

    @Test
    public void deleteForSubmission() throws Exception {
        final int id = pythonPackage.getId();

        when(pythonPackageService.findById(id)).thenReturn(Optional.of(pythonPackage));
        when(populator.moveToTrashDirectory(pythonPackage)).thenReturn(TRASHED_SOURCE);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        doNothing().when(newsfeedEventService).deleteRelatedEvents(submission);
        doNothing().when(submissionService).delete(submission);
        doNothing().when(storage).removePackageSource(TRASHED_SOURCE);
        when(maintainerService.findAllByPackageNameAndRepository(anyString(), any(PythonRepository.class)))
                .thenReturn(maintainers);

        deleter.deleteForSubmission(submission);

        verify(newsfeedEventService).deleteRelatedEvents(pythonPackage);
        verify(newsfeedEventService).deleteRelatedEvents(submission);
        verify(submissionService).delete(submission);
        verify(storage).removePackageSource(TRASHED_SOURCE);
        verify(maintainerService)
                .findAllByPackageNameAndRepository(pythonPackage.getName(), pythonPackage.getRepository());
    }
}
