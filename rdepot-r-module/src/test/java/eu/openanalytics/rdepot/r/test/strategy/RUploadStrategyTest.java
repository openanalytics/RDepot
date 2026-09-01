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
package eu.openanalytics.rdepot.r.test.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.manuals.ManualGenerator;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadRPackageDescriptionException;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalFSPersistentStorage;
import eu.openanalytics.rdepot.r.storage.population.implementations.LocalVignetteReader;
import eu.openanalytics.rdepot.r.storage.population.implementations.LocalVignetteUploader;
import eu.openanalytics.rdepot.r.storage.population.implementations.RLocalPopulator;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

public class RUploadStrategyTest extends StrategyTest {

    @Mock
    private SubmissionService submissionService;

    @Mock
    private PackageValidator<RPackage> packageValidator;

    @Mock
    private LocalStorage<RPackage> localStorage;

    @Mock
    private NewsfeedEventService eventService;

    @Mock
    private EmailService emailService;

    @Mock
    private RRepositoryService repositoryService;

    @Mock
    private SecurityMediator securityMediator;

    @Mock
    private PackageMaintainerService maintainerService;

    @Mock
    private RLocalPopulator rLocalPopulator;

    @Mock
    private ManualGenerator manualGenerator;

    @Mock
    private RLocalFSPersistentStorage persistentRStorage;

    @Mock
    private LocalVignetteReader localVignetteReader;

    @Mock
    private LocalVignetteUploader localVignetteUploader;

    private final String TEST_PACKAGE_PATH = "src/test/resources/unit/test_packages/abc_1.3.tar.gz";
    private final String TEST_PACKAGE_EXTRACTED = "src/test/resources/unit/test_packages/extracted/abc/";
    private final String TEST_PACKAGE_FILENAME = "abc_1.3.tar.gz";
    private final String TEST_PACKAGE_CONTENTTYPE = "";

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_whenUserIsAdmin() throws Exception {
        // Prerequisites
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_ADMIN();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted =
                new File(TEST_PACKAGE_EXTRACTED).toPath().toAbsolutePath().toFile();
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(persistentRStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(), List.of(extracted.getAbsoluteFile()), repository))
                .thenReturn(uploadedFile.toPath());
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(rLocalPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
        doAnswer((Answer<Submission>) invocation -> {
                    Submission submission = invocation.getArgument(0);
                    submission.setId(123);
                    return submission;
                })
                .when(submissionService)
                .create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(persistentRStorage.movePackageToAccepted(any())).thenReturn(uploadedFile.toPath());
        when(securityMediator.canUpload("abc", repository, requester)).thenReturn(true);
        doAnswer((Answer<RPackage>) invocation -> {
                    RPackage packageBag = invocation.getArgument(0);
                    packageBag.setId(123);
                    return packageBag;
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        // Execution
        RPackage packageBag = getRPackage(request, requester);
        String source = packageBag.getSource();

        // Assertions
        assertEquals("R (>= 2.10), nnet, quantreg, locfit", packageBag.getDepends(), "Incorrect depends property");
        assertEquals(
                "The package implements several ABC algorithms for\\n "
                        + "performing parameter estimation and model selection.\\n "
                        + "Cross-validation tools are also available for measuring the\\n "
                        + "accuracy of ABC estimates, and to calculate the\\n "
                        + "misclassification probabilities of different models.",
                packageBag.getDescription(),
                "Incorrect description");
        assertEquals("GPL (>= 3)", packageBag.getLicense(), "Incorrect license");
        assertTrue(packageBag.isActive(), "Package should be activated.");
        assertFalse(packageBag.isDeleted(), "Package should not be deleted");
        assertEquals("abc", packageBag.getName(), "Incorrect name");
        assertEquals(repository, packageBag.getRepository(), "Incorrect repository");
        assertEquals(
                "r-module/src/test/resources/unit/test_packages/abc_1.3.tar.gz",
                source.substring(source.lastIndexOf("r-module")),
                "Incorrect source");
        assertEquals("Tools for Approximate Bayesian Computation (ABC)", packageBag.getTitle(), "Incorrect title");
        assertEquals("1.3", packageBag.getVersion(), "Incorrect version");
        assertEquals(requester, packageBag.getUser(), "Incorrect user");
        assertEquals(RLanguage.instance, packageBag.getTechnology(), "Incorrect technology");
        assertEquals(123, packageBag.getId(), "Incorrect id");
        assertEquals("Katalin Csillery, Michael Blum and Olivier Francois", packageBag.getAuthor(), "Incorrect author");
    }

    private RPackage getRPackage(RPackageUploadRequest request, User requester) throws StrategyFailure {
        Submission submission = getSubmission(request, requester);
        return (RPackage) submission.getPackage();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_deletesDanglingSource_whenUncheckedExceptionIsThrown() throws Exception {
        // Prerequisites
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_ADMIN();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted =
                new File(TEST_PACKAGE_EXTRACTED).toPath().toAbsolutePath().toFile();
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(persistentRStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(), List.of(extracted.getAbsoluteFile()), repository))
                .thenReturn(uploadedFile.toPath());
        when(rLocalPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        doThrow(IllegalStateException.class).when(packageService).create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        // Execution
        Strategy<Submission> strategy = new RPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                rLocalPopulator,
                rPackageDeleter,
                request,
                maintainerService,
                manualGenerator,
                persistentRStorage,
                localVignetteUploader,
                localVignetteReader);

        assertThrows(
                IllegalStateException.class,
                strategy::perform,
                "Unchecked exceptions should be rethrown by strategies.");

        verify(persistentRStorage, times(1))
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_shouldSendEmail_whenUserIsNotAllowedToAccept() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted =
                new File(TEST_PACKAGE_EXTRACTED).toPath().toAbsolutePath().toFile();
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(persistentRStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(), List.of(extracted.getAbsoluteFile()), repository))
                .thenReturn(uploadedFile.toPath());
        when(rLocalPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
        doAnswer((Answer<Submission>) invocation -> {
                    Submission submission = invocation.getArgument(0);
                    submission.setId(123);
                    return submission;
                })
                .when(submissionService)
                .create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(securityMediator.canUpload("abc", repository, requester)).thenReturn(false);
        doAnswer((Answer<RPackage>) invocation -> {
                    RPackage packageBag = invocation.getArgument(0);
                    packageBag.setId(123);
                    return packageBag;
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));
        doNothing().when(emailService).sendAcceptSubmissionEmail(any());

        Submission submission = getSubmission(request, requester);

        assertFalse(submission.getPackage().isActive(), "Package should not be activated.");
        verify(emailService, times(1)).sendAcceptSubmissionEmail(submission);
    }

    private Submission getSubmission(RPackageUploadRequest request, User requester) throws StrategyFailure {
        Strategy<Submission> strategy = new RPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                rLocalPopulator,
                rPackageDeleter,
                request,
                maintainerService,
                manualGenerator,
                persistentRStorage,
                localVignetteUploader,
                localVignetteReader);
        return strategy.perform();
    }

    @Test
    public void createSubmission_whenStorageFailsToWriteToWaitingRoom() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        doThrow(new WriteToWaitingRoomException())
                .when(rLocalPopulator)
                .writeToTemporaryLocalWaitingRoom(multipartFile, repository);

        Strategy<Submission> strategy = new RPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                rLocalPopulator,
                rPackageDeleter,
                request,
                maintainerService,
                manualGenerator,
                persistentRStorage,
                localVignetteUploader,
                localVignetteReader);

        assertThrows(
                StrategyFailure.class,
                strategy::perform,
                "Exception should be thrown when localStorage fails to " + "write package to the waiting room.");
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenExtractionFails() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile.getAbsoluteFile());
        doThrow(new ExtractFileException()).when(localStorage).extractTarGzPackageFile(uploadedFile.getAbsoluteFile());
        doNothing().when(rLocalPopulator).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());

        Strategy<Submission> strategy = new RPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                rLocalPopulator,
                rPackageDeleter,
                request,
                maintainerService,
                manualGenerator,
                persistentRStorage,
                localVignetteUploader,
                localVignetteReader);

        assertThrows(
                StrategyFailure.class, strategy::perform, "Exception should be thrown when package extraction fails.");
        verify(rLocalPopulator, times(1)).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        verify(persistentRStorage, times(0))
                .deleteFromStorageIfExists(
                        uploadedFile.getAbsoluteFile().toPath().getParent());
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenReadingPropertiesFails() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted =
                new File(TEST_PACKAGE_EXTRACTED).toPath().toAbsolutePath().toFile();
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(persistentRStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(), List.of(extracted.getAbsoluteFile()), repository))
                .thenReturn(uploadedFile.getAbsoluteFile().toPath());
        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile.getAbsoluteFile());
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsoluteFile().getAbsolutePath());
        doThrow(new ReadRPackageDescriptionException())
                .when(rLocalPopulator)
                .getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile()));
        doNothing().when(rLocalPopulator).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        doNothing()
                .when(persistentRStorage)
                .deleteFromStorageIfExists(
                        uploadedFile.getAbsoluteFile().toPath().getParent());

        Strategy<Submission> strategy = new RPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                rLocalPopulator,
                rPackageDeleter,
                request,
                maintainerService,
                manualGenerator,
                persistentRStorage,
                localVignetteUploader,
                localVignetteReader);

        assertThrows(
                StrategyFailure.class,
                strategy::perform,
                "Exception should be thrown when reading package description fails.");
        verify(rLocalPopulator, times(1)).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        verify(persistentRStorage, times(1))
                .deleteFromStorageIfExists(
                        uploadedFile.getAbsoluteFile().toPath().getParent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmissionAndRemovePackageSource_whenValidationFails() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted =
                new File(TEST_PACKAGE_EXTRACTED).toPath().toAbsolutePath().toFile();
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(persistentRStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(), List.of(extracted.getAbsoluteFile()), repository))
                .thenReturn(uploadedFile.toPath());
        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(rLocalPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        doAnswer((Answer<Object>) invocation -> {
                    DataSpecificValidationResult<?> validationResult =
                            invocation.getArgument(2, DataSpecificValidationResult.class);
                    validationResult.error("author", "invalid.property");
                    return null;
                })
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        doNothing().when(rLocalPopulator).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        doNothing()
                .when(persistentRStorage)
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());

        Strategy<Submission> strategy = new RPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                rLocalPopulator,
                rPackageDeleter,
                request,
                maintainerService,
                manualGenerator,
                persistentRStorage,
                localVignetteUploader,
                localVignetteReader);

        assertThrows(
                StrategyFailure.class, strategy::perform, "Exception should be thrown when package validation fails.");
        verify(rLocalPopulator, times(1)).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        verify(persistentRStorage, times(1))
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_shouldGenerateManual() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted =
                new File(TEST_PACKAGE_EXTRACTED).toPath().toAbsolutePath().toFile();
        boolean generateManual = true;
        boolean replace = false;

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile, repository, generateManual, replace, false, null, null, null, "");

        when(persistentRStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(), List.of(extracted.getAbsoluteFile()), repository))
                .thenReturn(uploadedFile.getAbsoluteFile().toPath());
        when(rLocalPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile.getAbsoluteFile());
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(rLocalPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
        doAnswer((Answer<Submission>) invocation -> {
                    Submission submission = invocation.getArgument(0);
                    submission.setId(123);
                    return submission;
                })
                .when(submissionService)
                .create(any());
        when(persistentRStorage.movePackageToAccepted(any())).thenReturn(uploadedFile.toPath());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(securityMediator.canUpload("abc", repository, requester)).thenReturn(true);
        doAnswer((Answer<RPackage>) invocation -> {
                    RPackage packageBag = invocation.getArgument(0);
                    packageBag.setId(123);
                    return packageBag;
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        RPackage packageBag = getRPackage(request, requester);

        verify(manualGenerator, times(1)).generateManual(packageBag);
    }
}
