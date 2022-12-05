/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
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

import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.EmailService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.r.RDescription;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;

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
	
	private final String TEST_PACKAGE_PATH = 
			"src/test/resources/unit/test_packages/abc_1.3.tar.gz";
	private final String TEST_PACKAGE_EXTRACTED = "src/test/resources/unit/test_packages/extracted/abc/";
	private final String TEST_PACKAGE_FILENAME = "abc_1.3.tar.gz";
	private final String TEST_PACKAGE_CONTENTTYPE = "";
	
	@Test
	public void createSubmission_whenUserIsAdmin() throws Exception {
		//Prerequisites
		FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
		byte[] packageBytes = fis.readAllBytes();
		fis.close();
		
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		MockMultipartFile multipartFile = new MockMultipartFile(TEST_PACKAGE_FILENAME, 
				TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
		File uploadedFile = new File(TEST_PACKAGE_PATH);
		File extracted = new File(TEST_PACKAGE_EXTRACTED);
		boolean generateManual = true;
		boolean replace = false;
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repository, generateManual, replace);
		
		when(storage.writeToWaitingRoom(multipartFile, repository))
			.thenReturn(uploadedFile);
		when(storage.extractTarGzPackageFile(uploadedFile))
			.thenReturn(extracted);
		when(storage.getPropertiesFromExtractedFile(extracted))
			.thenReturn(new RDescription(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
		doAnswer(new Answer<Submission>() {

			@Override
			public Submission answer(InvocationOnMock invocation) throws Throwable {
				Submission submission = invocation.getArgument(0);
				submission.setId(123);
				return submission;
			}}
		).when(submissionService).create(any());
		when(storage.moveToMainDirectory(any())).thenReturn(uploadedFile);
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
		when(securityMediator.canUpload("abc", repository, requester)).thenReturn(true);
		doAnswer(new Answer<RPackage>() {

			@Override
			public RPackage answer(InvocationOnMock invocation) throws Throwable {
				RPackage packageBag = invocation.getArgument(0);
				packageBag.setId(123);
				return packageBag;
			}
		}).when(packageService).create(any());
		doNothing().when(packageValidator).validateUploadPackage(any(), eq(replace));
		
		//Execution
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
				storage);
		
		Submission submission = strategy.perform();
		RPackage packageBag = (RPackage)submission.getPackage();
		String source = packageBag.getSource();
		
		//Assertions
		assertEquals("R (>= 2.10), nnet, quantreg, locfit", packageBag.getDepends(), "Incorrect depends property");
		assertEquals("The package implements several "
				+ "ABC algorithms for performing parameter estimation "
				+ "and model selection. "
				+ "Cross-validation tools are also available "
				+ "for measuring the accuracy of ABC estimates, "
				+ "and to calculate the misclassification "
				+ "probabilities of different models.", 
				packageBag.getDescription(), "Incorrect description"
		);
		assertEquals("GPL (>= 3)", packageBag.getLicense(), "Incorrect license");
		assertTrue(packageBag.isActive(), "Package should be activated.");
		assertFalse(packageBag.isDeleted(), "Package should not be deleted");
		assertEquals("abc", packageBag.getName(), "Incorrect name");
		assertEquals(repository, packageBag.getRepository(), "Incorrect repository");
		assertEquals("r-module/src/test/resources/unit/test_packages/abc_1.3.tar.gz", 
				source.substring(source.lastIndexOf("r-module")), "Incorrect source");
		assertEquals("Tools for Approximate Bayesian Computation (ABC)", 
				packageBag.getTitle(), "Incorrect title");
		assertEquals("1.3", packageBag.getVersion(), "Incorrect version");
		assertEquals(requester, packageBag.getUser(), "Incorrect user");
		assertEquals(RLanguage.instance, packageBag.getTechnology(), "Incorrect technology");
		assertEquals(123, packageBag.getId(), "Incorrect id");
		assertEquals("Katalin Csillery, Michael Blum and Olivier Francois", packageBag.getAuthor(), "Incorrect author");
		}
	
	@Test
	public void createSubmission_shouldSendEmail_whenUserIsNotAllowedToAccept() throws Exception {
		FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
		byte[] packageBytes = fis.readAllBytes();
		fis.close();
		
		User requester = UserTestFixture.GET_REGULAR_USER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		MockMultipartFile multipartFile = new MockMultipartFile(TEST_PACKAGE_FILENAME, 
				TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
		File uploadedFile = new File(TEST_PACKAGE_PATH);
		File extracted = new File(TEST_PACKAGE_EXTRACTED);
		boolean generateManual = true;
		boolean replace = false;
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repository, generateManual, replace);
		
		when(storage.writeToWaitingRoom(multipartFile, repository))
			.thenReturn(uploadedFile);
		when(storage.extractTarGzPackageFile(uploadedFile))
			.thenReturn(extracted);
		when(storage.getPropertiesFromExtractedFile(extracted))
			.thenReturn(new RDescription(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
		doAnswer(new Answer<Submission>() {

			@Override
			public Submission answer(InvocationOnMock invocation) throws Throwable {
				Submission submission = invocation.getArgument(0);
				submission.setId(123);
				return submission;
			}}
		).when(submissionService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
		when(securityMediator.canUpload("abc", repository, requester)).thenReturn(false);
		doAnswer(new Answer<RPackage>() {

			@Override
			public RPackage answer(InvocationOnMock invocation) throws Throwable {
				RPackage packageBag = invocation.getArgument(0);
				packageBag.setId(123);
				return packageBag;
			}
		}).when(packageService).create(any());
		doNothing().when(packageValidator).validateUploadPackage(any(), eq(replace));
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
				storage);
		Submission submission = strategy.perform();
		
//		strategy.perform();
		
		assertFalse(submission.getPackage().isActive(), "Package should not be activated.");
		verify(emailService, times(1)).sendAcceptSubmissionEmail(submission);
	}
	
	@Test
	public void createSubmission_shouldFail_whenStorageFailsToWriteToWaitingRoom() throws Exception {
		FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
		byte[] packageBytes = fis.readAllBytes();
		fis.close();
		
		User requester = UserTestFixture.GET_REGULAR_USER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		MockMultipartFile multipartFile = new MockMultipartFile(TEST_PACKAGE_FILENAME, 
				TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
		boolean generateManual = true;
		boolean replace = false;
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repository, generateManual, replace);
		
		doThrow(new WriteToWaitingRoomException())
			.when(storage)
			.writeToWaitingRoom(multipartFile, repository);
		
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
				storage);
		
		assertThrows(StrategyFailure.class, () -> strategy.perform(), 
				"Exception should be thrown when storage fails to "
				+ "write package to the waiting room.");
	}
	
	@Test
	public void createSubmission_shouldFailAndAttemptToCleanUp_whenExtractionFails() throws Exception {
		FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
		byte[] packageBytes = fis.readAllBytes();
		fis.close();
		
		User requester = UserTestFixture.GET_REGULAR_USER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		MockMultipartFile multipartFile = new MockMultipartFile(TEST_PACKAGE_FILENAME, 
				TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
		File uploadedFile = new File(TEST_PACKAGE_PATH);
		boolean generateManual = true;
		boolean replace = false;
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repository, generateManual, replace);
		
		when(storage.writeToWaitingRoom(multipartFile, repository))
			.thenReturn(uploadedFile);
		doThrow(new ExtractFileException())
			.when(storage).extractTarGzPackageFile(uploadedFile);
		doNothing().when(storage).removeFileIfExists(uploadedFile);
		
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
				storage);
		
		assertThrows(StrategyFailure.class, () -> strategy.perform(),
				"Exception should be thrown when package extraction fails.");
		verify(storage, times(1)).removeFileIfExists(uploadedFile);
	}
	
	@Test
	public void createSubmission_shouldFailAndAttemptToCleanUp_whenReadingPropertiesFails() throws Exception {
		FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
		byte[] packageBytes = fis.readAllBytes();
		fis.close();
		
		User requester = UserTestFixture.GET_REGULAR_USER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		MockMultipartFile multipartFile = new MockMultipartFile(TEST_PACKAGE_FILENAME, 
				TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
		File uploadedFile = new File(TEST_PACKAGE_PATH);
		File extracted = new File(TEST_PACKAGE_EXTRACTED);
		boolean generateManual = true;
		boolean replace = false;
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repository, generateManual, replace);
		
		when(storage.writeToWaitingRoom(multipartFile, repository))
			.thenReturn(uploadedFile);
		when(storage.extractTarGzPackageFile(uploadedFile))
			.thenReturn(extracted);
		doThrow(new ReadPackageDescriptionException())
			.when(storage).getPropertiesFromExtractedFile(extracted);
		doNothing().when(storage).removeFileIfExists(uploadedFile);
		doNothing().when(storage).removeFileIfExists(extracted);

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
				storage);
		
		assertThrows(StrategyFailure.class, () -> strategy.perform(),
				"Exception should be thrown when reading package description fails.");
		verify(storage, times(1)).removeFileIfExists(uploadedFile);
		verify(storage, times(1)).removeFileIfExists(extracted);
	}
	
	@Test
	public void createSubmission_shouldFailAndRemovePackageSource_whenValidationFails() throws Exception {
		FileInputStream fis = new FileInputStream(new File(TEST_PACKAGE_PATH));
		byte[] packageBytes = fis.readAllBytes();
		fis.close();
		
		User requester = UserTestFixture.GET_REGULAR_USER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		MockMultipartFile multipartFile = new MockMultipartFile(TEST_PACKAGE_FILENAME, 
				TEST_PACKAGE_FILENAME, TEST_PACKAGE_CONTENTTYPE, packageBytes);
		File uploadedFile = new File(TEST_PACKAGE_PATH);
		File extracted = new File(TEST_PACKAGE_EXTRACTED);
		boolean generateManual = true;
		boolean replace = false;
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repository, generateManual, replace);
		
		when(storage.writeToWaitingRoom(multipartFile, repository))
			.thenReturn(uploadedFile);
		when(storage.extractTarGzPackageFile(uploadedFile))
			.thenReturn(extracted);
		when(storage.getPropertiesFromExtractedFile(extracted))
			.thenReturn(new RDescription(new File(TEST_PACKAGE_EXTRACTED + "/DESCRIPTION")));
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(requester);
		doThrow(new PackageValidationException("invalid.property"))
			.when(packageValidator).validateUploadPackage(any(), eq(replace));
		doNothing().when(storage).removeFileIfExists(uploadedFile);
		doNothing().when(storage).removeFileIfExists(extracted);
		doNothing().when(storage).removePackageSource(any());
		
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
				storage);
		
		assertThrows(StrategyFailure.class, () -> strategy.perform(),
				"Exception should be thrown when package validation fails.");
		verify(storage, times(1)).removeFileIfExists(uploadedFile);
		verify(storage, times(1)).removeFileIfExists(extracted);
		verify(storage, times(1)).removePackageSource(any());
	}
	
	@Test
	public void createSubmission_shouldGenerateManual() throws Exception {
		//TODO
	}
}
