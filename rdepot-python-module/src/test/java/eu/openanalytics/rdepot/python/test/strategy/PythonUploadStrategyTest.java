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
package eu.openanalytics.rdepot.python.test.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.PythonPersistentStorage;
import eu.openanalytics.rdepot.python.storage.population.PythonPopulator;
import eu.openanalytics.rdepot.python.strategy.upload.PythonPackageUploadStrategy;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

public class PythonUploadStrategyTest extends StrategyTest {

    @Mock
    private SubmissionService submissionService;

    @Mock
    private PackageValidator<PythonPackage> packageValidator;

    @Mock
    private LocalStorage<PythonPackage> localStorage;

    @Mock
    private PythonPopulator pythonPopulator;

    @Mock
    private NewsfeedEventService eventService;

    @Mock
    private EmailService emailService;

    @Mock
    private PythonRepositoryService repositoryService;

    @Mock
    private SecurityMediator securityMediator;

    @Mock
    private PackageMaintainerService maintainerService;

    @Mock
    private PythonPackageDeleter packageDeleter;

    @Mock
    private PythonPersistentStorage pythonPersistentStorage;

    private static final String RESOURCES = "src/test/resources/unit";
    private final String TEST_PACKAGE_PATH = RESOURCES + "/test_packages/strategy_tests/coconutpy-2.2.1.tar.gz";
    private final String TEST_PACKAGE_EXTRACTED = RESOURCES + "/test_packages/strategy_tests/coconutpy/";
    private final String TEST_PACKAGE_FILENAME = "coconutpy-2.2.1.tar.gz";
    private final String TEST_PACKAGE_CONTENTTYPE = "";
    private static final String ZEST_RELEASER_FILE = RESOURCES + "/test_files/properties_files/PKG-INFO_zest_releaser";

    @Test
    public void getLicense_pep639LicenseExpression() throws IOException, NoSuchMethodException {
        final Properties properties = new PropertiesParser(new File(ZEST_RELEASER_FILE));
        PackageUploadRequest<PythonRepository> request = new PackageUploadRequest<>();
        request.setBinaryPackage(false);
        PythonPackageUploadStrategy strategy = new PythonPackageUploadStrategy(
                request,
                new User(),
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
                packageDeleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);
        String result = (String) ReflectionSupport.invokeMethod(
                PythonPackageUploadStrategy.class.getDeclaredMethod("getLicense", Properties.class),
                strategy,
                properties);
        assertEquals("GPL-2.0-or-later", result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmission_whenUserIsAdmin() throws Exception {
        // Prerequisites
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_ADMIN();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean replace = false;
        boolean binary = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, replace, "", binary);

        when(pythonPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(pythonPersistentStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(),
                        List.of(extracted.toPath().toAbsolutePath().toFile()),
                        repository))
                .thenReturn(uploadedFile.toPath());
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(pythonPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/PKG-INFO")));
        doAnswer((Answer<Submission>) invocation -> {
                    Submission submission = invocation.getArgument(0);
                    submission.setId(123);
                    return submission;
                })
                .when(submissionService)
                .create(any());
        when(pythonPersistentStorage.movePackageToAccepted(any())).thenReturn(uploadedFile.toPath());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(securityMediator.canUpload("coconutpy", repository, requester)).thenReturn(true);
        doAnswer((Answer<PythonPackage>) invocation -> {
                    PythonPackage packageBag = invocation.getArgument(0);
                    packageBag.setId(123);
                    return packageBag;
                })
                .when(packageService)
                .create(any());
        doNothing()
                .when(packageValidator)
                .validateUploadPackage(any(), eq(replace), any(DataSpecificValidationResult.class));

        // Execution
        PythonPackage packageBag = getPythonPackage(request, requester);
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

    private PythonPackage getPythonPackage(PackageUploadRequest<PythonRepository> request, User requester)
            throws StrategyFailure {
        Submission submission = getSubmission(request, requester);
        return (PythonPackage) submission.getPackage();
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
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean replace = false;
        boolean binary = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, replace, "", binary);

        when(pythonPersistentStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(),
                        List.of(extracted.toPath().toAbsolutePath().toFile()),
                        repository))
                .thenReturn(uploadedFile.toPath());
        when(pythonPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(pythonPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
                .thenReturn(new PropertiesParser(new File(TEST_PACKAGE_EXTRACTED + "/PKG-INFO")));
        doAnswer((Answer<Submission>) invocation -> {
                    Submission submission = invocation.getArgument(0);
                    submission.setId(123);
                    return submission;
                })
                .when(submissionService)
                .create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
        when(securityMediator.canUpload("coconutpy", repository, requester)).thenReturn(false);
        doAnswer((Answer<PythonPackage>) invocation -> {
                    PythonPackage packageBag = invocation.getArgument(0);
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

    private Submission getSubmission(PackageUploadRequest<PythonRepository> request, User requester)
            throws StrategyFailure {
        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
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
                deleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);
        return strategy.perform();
    }

    @Test
    public void createSubmission_whenStorageFailsToWriteToWaitingRoom() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        boolean replace = false;
        boolean binary = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, replace, "", binary);

        doThrow(new WriteToWaitingRoomException())
                .when(pythonPopulator)
                .writeToTemporaryLocalWaitingRoom(multipartFile, repository);

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
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
                deleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);

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
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        boolean replace = false;
        boolean binary = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, replace, "", binary);

        when(pythonPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        doThrow(new ExtractFileException()).when(localStorage).extractTarGzPackageFile(uploadedFile.getAbsoluteFile());
        doNothing().when(pythonPopulator).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
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
                deleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);

        assertThrows(
                StrategyFailure.class, strategy::perform, "Exception should be thrown when package extraction fails.");
        verify(pythonPopulator, times(1)).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        verify(pythonPersistentStorage, times(0))
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());
    }

    @Test
    public void createSubmissionAndAttemptToCleanUp_whenReadingPropertiesFails() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean replace = false;
        boolean binary = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, replace, "", binary);

        when(pythonPersistentStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(),
                        List.of(extracted.toPath().toAbsolutePath().toFile()),
                        repository))
                .thenReturn(uploadedFile.toPath());
        when(pythonPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile);
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        doThrow(new ReadPackageDescriptionException(PythonMessageCodes.READ_PYTHON_PROPERTIES_FILE_EXCEPTION))
                .when(pythonPopulator)
                .getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile()));
        doNothing().when(pythonPopulator).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        doNothing()
                .when(pythonPersistentStorage)
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
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
                deleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);

        assertThrows(
                StrategyFailure.class,
                strategy::perform,
                "Exception should be thrown when reading package description fails.");
        verify(pythonPopulator, times(1)).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        verify(pythonPersistentStorage, times(1))
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createSubmissionAndRemovePackageSource_whenValidationFails() throws Exception {
        FileInputStream fis = new FileInputStream(TEST_PACKAGE_PATH);
        byte[] packageBytes = fis.readAllBytes();
        fis.close();

        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);
        MockMultipartFile multipartFile = new MockMultipartFile(
                TEST_PACKAGE_FILENAME, TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
        File uploadedFile = new File(TEST_PACKAGE_PATH);
        File extracted = new File(TEST_PACKAGE_EXTRACTED);
        boolean replace = false;
        boolean binary = false;

        PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, replace, "", binary);

        when(pythonPersistentStorage.storeNewPackage(
                        uploadedFile.getAbsoluteFile(),
                        List.of(extracted.toPath().toAbsolutePath().toFile()),
                        repository))
                .thenReturn(uploadedFile.toPath());
        when(pythonPopulator.writeToTemporaryLocalWaitingRoom(multipartFile, repository))
                .thenReturn(uploadedFile.getAbsoluteFile());
        when(localStorage.extractTarGzPackageFile(uploadedFile.getAbsoluteFile()))
                .thenReturn(extracted.getAbsolutePath());
        when(pythonPopulator.getPropertiesFromExtractedFile(List.of(extracted.getAbsoluteFile())))
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

        doNothing().when(pythonPopulator).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        doNothing()
                .when(pythonPersistentStorage)
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());

        Strategy<Submission> strategy = new PythonPackageUploadStrategy(
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
                deleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);

        assertThrows(
                StrategyFailure.class, strategy::perform, "Exception should be thrown when package validation fails.");
        verify(pythonPopulator, times(1)).removeTemporaryLocalWaitingRoomOfFile(uploadedFile.getAbsoluteFile());
        verify(pythonPersistentStorage, times(1))
                .deleteFromStorageIfExists(uploadedFile.toPath().getParent());
    }
}
