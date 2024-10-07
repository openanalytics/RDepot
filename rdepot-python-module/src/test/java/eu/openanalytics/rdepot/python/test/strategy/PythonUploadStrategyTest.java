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
package eu.openanalytics.rdepot.python.test.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.implementations.PythonLocalStorage;
import eu.openanalytics.rdepot.python.strategy.upload.PythonPackageUploadStrategy;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.io.File;
import java.io.FileInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

public class PythonUploadStrategyTest extends StrategyTest {

    @Mock
    private SubmissionService submissionService;

    @Mock
    private PackageValidator<PythonPackage> packageValidator;

    @Mock
    private PythonLocalStorage storage;

    @Mock
    private NewsfeedEventService eventService;

    @Mock
    private EmailService emailService;

    @Mock
    private PythonRepositoryService repositoryService;

    @Mock
    private SecurityMediator securityMediator;

    private final String TEST_PACKAGE_PATH =
            "src/test/resources/unit/test_packages/strategy_tests/coconutpy-2.2.1.tar.gz";
    private final String TEST_PACKAGE_EXTRACTED = "src/test/resources/unit/test_packages/strategy_tests/coconutpy/";
    private final String TEST_PACKAGE_FILENAME = "coconutpy-2.2.1.tar.gz";
    private final String TEST_PACKAGE_CONTENTTYPE = "";

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_whenUserIsAdmin() throws Exception {
        // Prerequisites
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_ADMIN();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/PKG-INFO")));
        doAnswer(new Answer<Submission>() {

                    @Override
                    public Submission answer(InvocationOnMock invocation) throws Throwable {
                        Submission submission = invocation.getArgument(0);
                        submission.setId(123);
                        return submission;
                    }
                })
                .when(submissionService)
                .create(any());
        when(storage.moveToMainDirectory(any())).thenReturn(uploadedFile.getAbsolutePath());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(securityMediator.canUpload("coconutpy", repository, requester)).thenReturn(true);
        doAnswer(new Answer<PythonPackage>() {

                    @Override
                    public PythonPackage answer(InvocationOnMock invocation) throws Throwable {
                        PythonPackage packageBag = invocation.getArgument(0);
                        packageBag.setId(123);
                        return packageBag;
                    }
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        // Execution
        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                deleter);

        Submission submission = strategy.perform();
        PythonPackage packageBag = (PythonPackage) submission.getPackage();
        String source = packageBag.getSource();

        // Assertions
        assertEquals(expectedDescription(), packageBag.getDescription(), "Incorrect description");
        assertEquals("MIT License", packageBag.getLicense(), "Incorrect license");
        assertTrue(packageBag.isActive(), "Package should be activated.");
        assertFalse(packageBag.isDeleted(), "Package should not be deleted");
        assertEquals("coconutpy", packageBag.getName(), "Incorrect name");
        assertEquals(repository, packageBag.getRepository(), "Incorrect repository");
        assertEquals(
                "python-module/src/test/resources/unit/test_packages/strategy_tests/coconutpy-2.2.1.tar.gz",
                source.substring(source.lastIndexOf("python-module")),
                "Incorrect source");
        assertEquals("A python wrapper around the Coconut API", packageBag.getTitle(), "Incorrect title");
        assertEquals("A python wrapper around the Coconut API", packageBag.getSummary(), "Incorrect summary");
        assertEquals("2.2.1", packageBag.getVersion(), "Incorrect version");
        assertEquals(requester, packageBag.getUser(), "Incorrect user");
        assertEquals(PythonLanguage.instance, packageBag.getTechnology(), "Incorrect technology");
        assertEquals(123, packageBag.getId(), "Incorrect id");
        assertEquals("Bruno Celeste", packageBag.getAuthor(), "Incorrect author");
    }

    private String expectedDescription() {
        return "Client Library for encoding Videos with Coconut\\n"
                + " \\\\n"
                + " \\ Coconut is a Video Encoding Web Service built for developers.\\n"
                + " \\\\n"
                + " \\ For more information:\\n"
                + " \\\\n"
                + " \\ * Coconut: http://coconut.co\\n"
                + " \\ * API Documentation: http://coconut.co/docs\\n"
                + " \\ * Twitter: @openCoconut\\n"
                + " \\\\n"
                + " \\ Changelogs\\n"
                + " \\\\n"
                + " \\ 2.2.0\\n"
                + " \\ Added a new method #config to generate a full configuration based on the"
                + " given parameters. It's especially useful to handle dynamic settings like"
                + " source or variables that can be set directly in code.\\n"
                + " \\\\n"
                + " \\ 2.0.0\\n"
                + " \\ New version of the client library which uses the HeyWatch API v2. This"
                + " library is not compatible with 1.x\\n"
                + " \\\\n"
                + " \\ 1.0.0\\n"
                + " \\ First version\\n"
                + " \\";
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_shouldSendEmail_whenUserIsNotAllowedToAccept() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/PKG-INFO")));
        doAnswer(new Answer<Submission>() {

                    @Override
                    public Submission answer(InvocationOnMock invocation) throws Throwable {
                        Submission submission = invocation.getArgument(0);
                        submission.setId(123);
                        return submission;
                    }
                })
                .when(submissionService)
                .create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(securityMediator.canUpload("coconutpy", repository, requester)).thenReturn(false);
        doAnswer(new Answer<PythonPackage>() {

                    @Override
                    public PythonPackage answer(InvocationOnMock invocation) throws Throwable {
                        PythonPackage packageBag = invocation.getArgument(0);
                        packageBag.setId(123);
                        return packageBag;
                    }
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));
        doNothing().when(emailService).sendAcceptSubmissionEmail(any());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                deleter);
        Submission submission = strategy.perform();

        assertFalse(submission.getPackage().isActive(), "Package should not be activated.");
        verify(emailService, times(1)).sendAcceptSubmissionEmail(submission);
    }

    @Test
    public void createSubmission_whenStorageFailsToWriteToWaitingRoom() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        doThrow(new WriteToWaitingRoomException()).when(storage).writeToWaitingRoom(multipartFile, repository);

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                deleter);

        assertThrows(
                StrategyFailure.class,
                strategy::perform,
                "Exception should be thrown when storage fails to " + "write package to the waiting room.");
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenExtractionFails() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        doThrow(new ExtractFileException()).when(storage).extractTarGzPackageFile(uploadedFile.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(uploadedFile.getAbsolutePath());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                deleter);

        assertThrows(
                StrategyFailure.class, strategy::perform, "Exception should be thrown when package extraction fails.");
        verify(storage, times(1)).removeFileIfExists(uploadedFile.getAbsolutePath());
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenReadingPropertiesFails() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        doThrow(new ReadPackageDescriptionException(PythonMessageCodes.READ_PYTHON_PKG_INFO_EXCEPTION))
                .when(storage)
                .getPropertiesFromExtractedFile(extracted.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(uploadedFile.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(extracted.getAbsolutePath());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                deleter);

        assertThrows(
                StrategyFailure.class,
                strategy::perform,
                "Exception should be thrown when reading package description fails.");
        verify(storage, times(1)).removeFileIfExists(uploadedFile.getAbsolutePath());
        verify(storage, times(1)).removeFileIfExists(extracted.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmissionAndRemovePackageSource_whenValidationFails() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/PKG-INFO")));
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        doAnswer(invocation -> {
                    DataSpecificValidationResult<?> validationResult =
                            invocation.getArgument(2, DataSpecificValidationResult.class);
                    validationResult.error("author", "invalid.property");
                    return null;
                })
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        doNothing().when(storage).removeFileIfExists(uploadedFile.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(extracted.getAbsolutePath());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                eventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                deleter);

        assertThrows(
                StrategyFailure.class, strategy::perform, "Exception should be thrown when package validation fails.");
        verify(storage, times(1)).removeFileIfExists(uploadedFile.getAbsolutePath());
        verify(storage, times(1)).removeFileIfExists(extracted.getAbsolutePath());
    }
}
