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
package eu.openanalytics.rdepot.test.unit.validation;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.test.strategy.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.python.test.strategy.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.python.validation.PythonPackageValidator;
import eu.openanalytics.rdepot.test.fixture.PythonSubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PythonPackageValidatorTest {

	@Mock
	Environment env;
	@Mock
	SubmissionService submissionService;
	@Mock
	PythonPackageService packageService;

	private PackageValidator<PythonPackage> packageValidator;
	private Optional<User> user;
	private PythonRepository repository;
	private PythonPackage packageBag;
	private Submission submission;
	private PythonPackage duplicatedPackageBag;
	private DataSpecificValidationResult<Submission> errors;

	@BeforeEach
	public void initEach() throws Exception {
		user = UserTestFixture.GET_FIXTURE_ADMIN();
		repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		packageBag = PythonPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user.get());
		packageBag.setTitle("someTitle");
		packageBag.setVersion("21");
		packageBag.setName("accelerated");
		packageBag.setHash("somehash");
		duplicatedPackageBag = new PythonPackage(packageBag);
		packageBag.setId(-1);
		submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), duplicatedPackageBag);
		packageValidator = new PythonPackageValidator(submissionService, packageService, env);
		prepareTest();
	}

	@Test
	public void validateUploadPackage_shouldSucceed() {
		packageValidator.validateUploadPackage(packageBag, true, errors);
		assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
	}

	@Test
	public void validateUploadPackageWithDashInName_shouldSucceed(){
		packageBag.setName("accelerated-numpy");
		packageValidator.validateUploadPackage(packageBag, true, errors);
		assertFalse(errors.hasErrors(), "Validation results should be empty for a package with dash in the name");
	}
	@Test
	public void validateUploadPackageWithUnderscoreInName_shouldSucceed(){
		packageBag.setName("accelerated_numpy");
		packageValidator.validateUploadPackage(packageBag, true, errors);
		log.warn(errors.getErrors().toString());

		assertFalse(errors.hasErrors(), "Validation results should be empty for a package with underscore in the name");
	}

	@Test
	public void validateUploadPackageWithNonAsciiChar_shouldFail(){
		packageBag.setName("accelerated€");
		packageValidator.validateUploadPackage(packageBag, true, errors);
		log.warn(errors.getErrors().toString());
		assertTrue(errors.hasErrors(), "Validation should return package name error");
		verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
	}

	@Test
	public void validateUploadPackageWithEmptyName_shouldFail() {
		packageBag.setName("");
		packageValidator.validateUploadPackage(packageBag, true, errors);
		log.warn(errors.getErrors().toString());
		assertTrue(errors.hasErrors(), "Validation should return package name error");
		verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
	}

	@Test
	public void validateUploadPackageWithEmptyLicense_shouldFail() {
		packageBag.setLicense("");
		packageValidator.validateUploadPackage(packageBag, true, errors);
		assertTrue(errors.hasErrors(), "Validation should return license error");
		verify(errors, times(1)).error("license", MessageCodes.EMPTY_LICENSE);
	}

	@Test
	public void validateUploadPackageWithEmptyHash_shouldFail() {
		packageBag.setHash("");
		packageValidator.validateUploadPackage(packageBag, true, errors);
		assertTrue(errors.hasErrors(), "Validation should return hash error");
		verify(errors, times(1)).error("hash", MessageCodes.EMPTY_MD5SUM);
	}

	@Test
	public void validateUploadPackageThatExistsWithReplaceSetToTrue_shouldWarn() {
		when(packageService
				.findByNameAndVersionAndRepositoryAndDeleted(packageBag.getName(), packageBag.getVersion(), packageBag.getRepository(), false)).thenReturn(Optional.of(duplicatedPackageBag));
		when( submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

		packageValidator.validateUploadPackage(packageBag, true, errors);

		assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
		assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
		verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_ON, submission);
	}

	@Test
	public void validateUploadPackageThatExistsWithReplaceSetToFalse_shouldWarn() {
		when(packageService
				.findByNameAndVersionAndRepositoryAndDeleted(packageBag.getName(), packageBag.getVersion(), packageBag.getRepository(), false)).thenReturn(Optional.of(duplicatedPackageBag));
		when( submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

		packageValidator.validateUploadPackage(packageBag, false, errors);

		assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
		assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
		verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_OFF, submission);
	}

	private void prepareTest() throws PackageValidationException, PackageDuplicateWithReplaceOff {
		errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
		when(env.getProperty("package.version.max-numbers", "10")).thenReturn("1");
	}
}
