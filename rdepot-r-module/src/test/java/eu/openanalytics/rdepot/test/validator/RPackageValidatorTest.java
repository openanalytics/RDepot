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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RSubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    private RPackage duplicatedPackageBag;
    private Submission submission;
    private DataSpecificValidationResult<Submission> errors;

    @BeforeEach
    public void initEach() {
        user = Optional.of(UserTestFixture.GET_ADMIN());
        repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get());
        packageBag.setTitle("someTitle");
        packageBag.setVersion("1");
        packageBag.setName("someName");
        updatedPackageBag = new RPackage(packageBag);
        duplicatedPackageBag = new RPackage(packageBag);
        submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), duplicatedPackageBag);
        packageValidator = new RPackageValidator(submissionService, rPackageService, env);
    }

    @Test
    public void validateUploadPackage_shouldSucceed() throws Exception {
        prepareTest();
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
    }

    @Test
    public void validateUploadPackageWithDashInName_shouldFail() throws Exception {
        prepareTest();
        packageBag.setName("accrued-smth");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithUnderscoreInName_shouldFail() throws Exception {
        prepareTest();
        packageBag.setName("accrued_smth");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithNonAsciiChar_shouldFail() throws Exception {
        prepareTest();
        packageBag.setName("accrued€");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageEndingWithDot_shouldFail() throws Exception {
        prepareTest();
        packageBag.setName("newPackage.");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageStartingWithNonLetter_shouldFail() throws Exception {
        prepareTest();
        packageBag.setName("1newPackage");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithEmptyName_shouldFail() throws Exception {
        prepareTest();
        packageBag.setName("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithEmptyLicense_shouldFail() throws Exception {
        prepareTest();
        packageBag.setLicense("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return license error");
        verify(errors, times(1)).error("license", MessageCodes.EMPTY_LICENSE);
    }

    @Test
    public void validateUploadPackageWithEmptyMd5sum_shouldFail() throws Exception {
        prepareTest();
        packageBag.setMd5sum("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return hash error");
        verify(errors, times(1)).error("md5sum", MessageCodes.EMPTY_MD5SUM);
    }

    @Test
    public void validateUploadPackageWithEmptyDescription_shouldFail() throws Exception {
        prepareTest();
        packageBag.setDescription("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return hash error");
        verify(errors, times(1)).error("description", MessageCodes.EMPTY_DESCRIPTION);
    }

    @Test
    public void validateUploadPackageWithEmptyAuthor_shouldFail() throws Exception {
        prepareTest();
        packageBag.setAuthor("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return hash error");
        verify(errors, times(1)).error("author", MessageCodes.EMPTY_AUTHOR);
    }

    @Test
    public void validateUploadPackageWithEmptyTitle_shouldFail() throws Exception {
        prepareTest();
        packageBag.setTitle("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return hash error");
        verify(errors, times(1)).error("title", MessageCodes.EMPTY_TITLE);
    }

    @Test
    public void validateUploadPackageThatExistsWithReplaceSetToTrue_shouldWarn() throws Exception {
        prepareTest();
        packageBag.setId(-1);
        when(rPackageService.findByNameAndVersionAndRepositoryAndDeleted(
                        packageBag.getName(), packageBag.getVersion(), packageBag.getRepository(), false))
                .thenReturn(Optional.of(duplicatedPackageBag));
        when(submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

        packageValidator.validateUploadPackage(packageBag, true, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
        assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
        verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_ON, submission);
    }

    @Test
    public void validateUploadPackageThatExistsWithReplaceSetToFalse_shouldWarn() throws Exception {
        prepareTest();
        packageBag.setId(-1);
        when(rPackageService.findByNameAndVersionAndRepositoryAndDeleted(
                        packageBag.getName(), packageBag.getVersion(), packageBag.getRepository(), false))
                .thenReturn(Optional.of(duplicatedPackageBag));
        when(submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

        packageValidator.validateUploadPackage(packageBag, false, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
        assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
        verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_OFF, submission);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageName() throws Exception {
        prepareTest();
        updatedPackageBag.setName("newName");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("name", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageDescription() throws Exception {
        prepareTest();
        updatedPackageBag.setDescription("newDesc");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("description", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageVersion() throws Exception {
        prepareTest();
        updatedPackageBag.setVersion("100");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("version", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageAuthor() throws Exception {
        prepareTest();
        updatedPackageBag.setAuthor("newAuthor");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("author", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageLicense() throws Exception {
        prepareTest();
        updatedPackageBag.setLicense("newLicense");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("license", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageTitle() throws Exception {
        prepareTest();
        updatedPackageBag.setTitle("newTitle");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("title", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageUrl() throws Exception {
        prepareTest();
        updatedPackageBag.setUrl("newUrl");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("url", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSubmission() throws Exception {
        prepareTest();
        updatedPackageBag.setSubmission(RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), updatedPackageBag));
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("submission", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSource() throws Exception {
        prepareTest();
        updatedPackageBag.setSource("newSource");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("source", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageMaintainer() throws Exception {
        prepareTest();
        updatedPackageBag.setUser(UserTestFixture.GET_REGULAR_USER());
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("user", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageRepository() throws Exception {
        prepareTest();
        updatedPackageBag.setRepository(new RRepository());
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("repository", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageMd5sum() throws Exception {
        prepareTest();
        updatedPackageBag.setMd5sum("newSum");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("md5sum", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageDepends() throws Exception {
        prepareTest();
        updatedPackageBag.setDepends("newDepends");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("depends", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageImports() throws Exception {
        prepareTest();
        updatedPackageBag.setImports("newImports");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("imports", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSuggests() throws Exception {
        prepareTest();
        updatedPackageBag.setSuggests("newSuggests");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("suggests", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSystemRequirements() throws Exception {
        prepareTest();
        updatedPackageBag.setSystemRequirements("newSystemRequirements");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("systemRequirements", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageGenerateManuals() throws Exception {
        prepareTest();
        updatedPackageBag.setGenerateManuals(true);
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("generateManuals", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void validateUploadMultipartFile_shouldSucced() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart =
                new MockMultipartFile("accrued_1.2.tar.gz", "accrued_1.2.tar.gz", "application/gzip", fileContent);
        packageValidator.validate(multipart, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
    }

    @Test
    public void validateUploadMultipartFileWithWrongContentType_shouldFail() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "boto3-1.26.156.tar.gz",
                "boto3-1.26.156.tar.gz",
                ContentType.MULTIPART_FORM_DATA.toString(),
                fileContent);
        packageValidator.validate(multipart, errors);
        assertTrue(errors.hasErrors(), "Validation should return contetnt type error");
        verify(errors, times(1)).error("CONTENT-TYPE", MessageCodes.INVALID_CONTENTTYPE);
    }

    @Test
    public void validateUploadMultipartFileWithNoContentType_shouldFail() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
        final MultipartFile multipart = new MockMultipartFile("boto3-1.26.156.tar.gz", new byte[] {});
        packageValidator.validate(multipart, errors);
        assertTrue(errors.hasErrors(), "Validation should return empty file error");
        verify(errors, times(1)).error("MULTIPART-FILE", MessageCodes.ERROR_EMPTY_FILE);
    }

    @Test
    public void validateUploadMultipartFileWithEmptyName_shouldFail() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "boto3-1.26.156.tar.gz", "", ContentType.MULTIPART_FORM_DATA.toString(), fileContent);
        packageValidator.validate(multipart, errors);
        assertTrue(errors.hasErrors(), "Validation should return invalid file name error");
        verify(errors, times(1)).error("MULTIPART-FILE", MessageCodes.INVALID_FILENAME);
    }

    private void prepareTest() throws PackageValidationException, PackageDuplicateWithReplaceOff {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult(Submission.class));
        when(env.getProperty("package.version.max-numbers", "10")).thenReturn("1");
    }

    private void validateUpdatedPackageBag() {
        when(rPackageService.findOneNonDeleted(packageBag.getId())).thenReturn(Optional.of(packageBag));
        packageValidator.validate(updatedPackageBag, true, errors);
    }
}
