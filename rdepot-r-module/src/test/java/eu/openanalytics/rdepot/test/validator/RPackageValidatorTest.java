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
package eu.openanalytics.rdepot.test.validator;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RPackageValidatorTest {
	
	@Mock
	RPackageService rPackageService;

	@Mock
	SubmissionService submissionService;
	
	@Mock
	Environment env;
	
	private PackageValidator<RPackage> packageValidator;
	private Optional<User> user;
	private RRepository repository;
	private RPackage packageBag;
	private RPackage updatedPackageBag;
	private DataSpecificValidationResult<Submission> errors;
	
	@BeforeEach
	public void initEach() {
		user = UserTestFixture.GET_FIXTURE_ADMIN();
		repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		packageBag = RPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user.get());
		packageBag.setTitle("someTitle");
		packageBag.setVersion("1");
		packageBag.setName("someName");
		updatedPackageBag = new RPackage(packageBag);
		packageValidator = new RPackageValidator(submissionService, rPackageService, env);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageName() throws Exception {
		updatedPackageBag.setName("newName");
		prepareTest();
		verify(errors, times(1)).error("name", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageDescription() throws Exception {
		updatedPackageBag.setDescription("newDesc");
		prepareTest();
		verify(errors, times(1)).error("description", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageVersion() throws Exception {
		updatedPackageBag.setVersion("100");
		prepareTest();
		verify(errors, times(1)).error("version", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageAuthor() throws Exception {
		updatedPackageBag.setAuthor("newAuthor");
		prepareTest();
		verify(errors, times(1)).error("author", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageLicense() throws Exception {
		updatedPackageBag.setLicense("newLicense");
		prepareTest();
		verify(errors, times(1)).error("license", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageTitle() throws Exception {
		updatedPackageBag.setTitle("newTitle");
		prepareTest();
		verify(errors, times(1)).error("title", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageUrl() throws Exception {
		updatedPackageBag.setUrl("newUrl");
		prepareTest();
		verify(errors, times(1)).error("url", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageSubmission() throws Exception {
		updatedPackageBag.setSubmission(SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), updatedPackageBag));
		prepareTest();
		verify(errors, times(1)).error("submission", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageSource() throws Exception {
		updatedPackageBag.setSource("newSource");
		prepareTest();
		verify(errors, times(1)).error("source", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageMaintainer() throws Exception {
		updatedPackageBag.setUser(UserTestFixture.GET_FIXTURE_USER());
		prepareTest();
		verify(errors, times(1)).error("user", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageRepository() throws Exception {
		updatedPackageBag.setRepository(new RRepository());
		prepareTest();
		verify(errors, times(1)).error("repository", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageMd5sum() throws Exception {
		updatedPackageBag.setMd5sum("newSum");
		prepareTest();
		verify(errors, times(1)).error("md5sum", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageDepends() throws Exception {
		updatedPackageBag.setDepends("newDepends");
		prepareTest();
		verify(errors, times(1)).error("depends", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageImports() throws Exception {
		updatedPackageBag.setImports("newImports");
		prepareTest();
		verify(errors, times(1)).error("imports", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageSuggests() throws Exception {
		updatedPackageBag.setSuggests("newSuggests");
		prepareTest();
		verify(errors, times(1)).error("suggests", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageSystemRequirements() throws Exception {
		updatedPackageBag.setSystemRequirements("newSystemRequirements");
		prepareTest();
		verify(errors, times(1)).error("systemRequirements", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	@Test
	public void updatePackage_shouldNotAllowChangingPackageGenerateManuals() throws Exception {
		updatedPackageBag.setGenerateManuals(true);
		prepareTest();
		verify(errors, times(1)).error("generateManuals", MessageCodes.FORBIDDEN_UPDATE);
	}
	
	private void prepareTest() throws PackageValidationException, PackageDuplicateWithReplaceOff {
		errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
		
		when(rPackageService.findOneNonDeleted(packageBag.getId()))
			.thenReturn((Optional.of(packageBag)));
		when(env.getProperty("package.version.max-numbers", "10")).thenReturn("1");
		
		packageValidator.validate(updatedPackageBag, true, errors);
	}
}
