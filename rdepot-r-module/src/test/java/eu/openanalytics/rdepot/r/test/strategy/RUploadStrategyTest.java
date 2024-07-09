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
package eu.openanalytics.rdepot.r.test.strategy;

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
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadRPackageDescriptionException;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import java.io.File;
import java.io.FileInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

public class RUploadStrategyTest extends StrategyTest {

    @Mock
    private SubmissionService submissionService;

    @Mock
    private PackageValidator<RPackage> packageValidator;

    @Mock
    private RLocalStorage storage;

    @Mock
    private NewsfeedEventService eventService;

    @Mock
    private EmailService emailService;

    @Mock
    private RRepositoryService repositoryService;

    @Mock
    private SecurityMediator securityMediator;

    private final String TEST_PACKAGE_PATH = "src/test/resources/unit/test_packages/abc_1.3.tar.gz";
    private final String TEST_PACKAGE_EXTRACTED = "src/test/resources/unit/test_packages/extracted/abc/";
    private final String TEST_PACKAGE_FILENAME = "abc_1.3.tar.gz";
    private final String TEST_PACKAGE_CONTENTTYPE = "";

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_whenUserIsAdmin() throws Exception {
        // Prerequisites
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_ADMIN();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
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
        when(securityMediator.canUpload("abc", repository, requester)).thenReturn(true);
        doAnswer(new Answer<RPackage>() {

                    @Override
                    public RPackage answer(InvocationOnMock invocation) throws Throwable {
                        RPackage packageBag = invocation.getArgument(0);
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
        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);

        Submission submission = strategy.perform();
        RPackage packageBag = (RPackage) submission.getPackage();
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

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_deletesDanglingSource_whenUncheckedExceptionIsThrown() throws Exception {
        // Prerequisites
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_ADMIN();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
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
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                storage,
                rPackageDeleter);

        assertThrows(
                IllegalStateException.class,
                strategy::perform,
                "Unchecked exceptions should be rethrown by strategies.");

        verify(storage).removeFileIfExists(extracted.getAbsolutePath());
        verify(storage).removeFileIfExists(uploadedFile.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_shouldSendEmail_whenUserIsNotAllowedToAccept() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
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

        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);
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
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        doThrow(new WriteToWaitingRoomException()).when(storage).writeToWaitingRoom(multipartFile, repository);

        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);

        assertThrows(
                StrategyFailure.class,
                () -> strategy.perform(),
                "Exception should be thrown when storage fails to " + "write package to the waiting room.");
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenExtractionFails() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
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

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        doThrow(new ExtractFileException()).when(storage).extractTarGzPackageFile(uploadedFile.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(uploadedFile.getAbsolutePath());

        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);

        assertThrows(
                StrategyFailure.class,
                () -> strategy.perform(),
                "Exception should be thrown when package extraction fails.");
        verify(storage, times(1)).removeFileIfExists(uploadedFile.getAbsolutePath());
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenReadingPropertiesFails() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        doThrow(new ReadRPackageDescriptionException())
                .when(storage)
                .getPropertiesFromExtractedFile(extracted.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(uploadedFile.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(extracted.getAbsolutePath());

        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);

        assertThrows(
                StrategyFailure.class,
                () -> strategy.perform(),
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
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        doAnswer(new Answer<>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        DataSpecificValidationResult<?> validationResult =
                                invocation.getArgument(2, DataSpecificValidationResult.class);
                        validationResult.error("author", "invalid.property");
                        return null;
                    }
                })
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        doNothing().when(storage).removeFileIfExists(uploadedFile.getAbsolutePath());
        doNothing().when(storage).removeFileIfExists(extracted.getAbsolutePath());

        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);

        assertThrows(
                StrategyFailure.class,
                () -> strategy.perform(),
                "Exception should be thrown when package validation fails.");
        verify(storage, times(1)).removeFileIfExists(uploadedFile.getAbsolutePath());
        verify(storage, times(1)).removeFileIfExists(extracted.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_shouldGenerateManual() throws Exception {
        FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean generateManual = true;
        boolean replace = false;

        PackageUploadRequest<RRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, generateManual, replace);

        when(storage.writeToWaitingRoom(multipartFile, repository)).thenReturn(uploadedFile.getAbsolutePath());
        when(storage.extractTarGzPackageFile(uploadedFile.getAbsolutePath())).thenReturn(extracted.getAbsolutePath());
        when(storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath()))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
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
        when(securityMediator.canUpload("abc", repository, requester)).thenReturn(true);
        doAnswer(new Answer<RPackage>() {

                    @Override
                    public RPackage answer(InvocationOnMock invocation) throws Throwable {
                        RPackage packageBag = invocation.getArgument(0);
                        packageBag.setId(123);
                        return packageBag;
                    }
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));
        Strategy<Submission> strategy = new RPackageUploadStrategy(
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
                storage,
                rPackageDeleter);

        Submission submission = strategy.perform();
        RPackage packageBag = (RPackage) submission.getPackage();

        verify(storage, times(1)).generateManual(packageBag);
    }
}
