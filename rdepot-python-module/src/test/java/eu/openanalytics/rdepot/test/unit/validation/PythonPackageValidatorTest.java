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
package eu.openanalytics.rdepot.test.unit.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.validation.PythonPackageValidator;
import eu.openanalytics.rdepot.test.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonSubmissionTestFixture;
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
public class PythonPackageValidatorTest {

    @Mock
    Environment env;

    @Mock
    SubmissionService submissionService;

    @Mock
    PythonPackageService packageService;

    private PackageValidator<PythonPackage> packageValidator;
    private PythonPackage packageBag;
    private Submission submission;
    private PythonPackage updatedPackageBag;
    private PythonPackage duplicatedPackageBag;
    private DataSpecificValidationResult<Submission> errors;

    @BeforeEach
    public void initEach() {
        User user = UserTestFixture.GET_ADMIN();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        packageBag = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setTitle("someTitle");
        packageBag.setVersion("21");
        packageBag.setName("accelerated");
        packageBag.setHash("somehash");
        duplicatedPackageBag = new PythonPackage(packageBag);
        updatedPackageBag = new PythonPackage(packageBag);
        updatedPackageBag.setLicense(packageBag.getLicense());
        updatedPackageBag.setHash(packageBag.getHash());
        submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, duplicatedPackageBag);
        packageValidator = new PythonPackageValidator(submissionService, packageService, env);
    }

    @Test
    public void validateUploadPackage_shouldSucceed() {
        prepareTest();
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
    }

    @Test
    public void validateUploadPackageWithDashInName_shouldSucceed() {
        prepareTest();
        packageBag.setName("accelerated-numpy");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a package with dash in the name");
    }

    @Test
    public void validateUploadPackageWithUnderscoreInName_shouldSucceed() {
        prepareTest();
        packageBag.setName("accelerated_numpy");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());

        assertFalse(errors.hasErrors(), "Validation results should be empty for a package with underscore in the name");
    }

    @Test
    public void validateUploadPackageWithNonAsciiChar_shouldFail() {
        prepareTest();
        packageBag.setName("accelerated€");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageEndingWithDot_shouldFail() {
        prepareTest();
        packageBag.setName("newPackage.");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageStartingWithNonLetter_shouldFail() {
        prepareTest();
        packageBag.setName("1newPackage");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithEmptyName_shouldFail() {
        prepareTest();
        packageBag.setName("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithEmptyLicense_shouldFail() {
        prepareTest();
        packageBag.setLicense("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return license error");
        verify(errors, times(1)).error("license", MessageCodes.EMPTY_LICENSE);
    }

    @Test
    public void validateUploadPackageWithEmptyHash_shouldFail() {
        prepareTest();
        packageBag.setHash("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return hash error");
        verify(errors, times(1)).error("hash", PythonMessageCodes.EMPTY_HASH);
    }

    @Test
    public void validateUploadPackageThatExistsWithReplaceSetToTrue_shouldWarn() {
        prepareTest();
        packageBag.setId(-1);
        when(packageService.findByNameAndVersionAndRepositoryAndDeleted(
                        packageBag.getName(), packageBag.getVersion(), packageBag.getRepository(), false))
                .thenReturn(Optional.of(duplicatedPackageBag));
        when(submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

        packageValidator.validateUploadPackage(packageBag, true, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
        assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
        verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_ON, submission);
    }

    @Test
    public void validateUploadPackageThatExistsWithReplaceSetToFalse_shouldWarn() {
        prepareTest();
        packageBag.setId(-1);
        when(packageService.findByNameAndVersionAndRepositoryAndDeleted(
                        packageBag.getName(), packageBag.getVersion(), packageBag.getRepository(), false))
                .thenReturn(Optional.of(duplicatedPackageBag));
        when(submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

        packageValidator.validateUploadPackage(packageBag, false, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
        assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
        verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_OFF, submission);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageName() {
        prepareTest();
        updatedPackageBag.setName("newName");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("name", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingVersion() {
        prepareTest();
        updatedPackageBag.setVersion("100");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("version", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingLicense() {
        prepareTest();
        updatedPackageBag.setLicense("newLicense");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("license", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingSource() {
        prepareTest();
        updatedPackageBag.setSource("newSource");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("source", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageMaintainer() {
        prepareTest();
        updatedPackageBag.setUser(UserTestFixture.GET_REGULAR_USER());
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("user", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingRepository() {
        prepareTest();
        updatedPackageBag.setRepository(new PythonRepository());
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("repository", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingHash() {
        prepareTest();
        updatedPackageBag.setHash("newHash");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("hash", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void validateUploadMultipartFile_shouldSucceed() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        final File newPackage = new File("src/test/resources/unit/test_packages/testrepo1/boto3/boto3-1.26.156.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "boto3-1.26.156.tar.gz", "boto3-1.26.156.tar.gz", "application/gzip", fileContent);
        packageValidator.validate(multipart, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
    }

    @Test
    public void validateUploadMultipartFileWithWrongContentType_shouldFail() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        final File newPackage = new File("src/test/resources/unit/test_packages/testrepo1/boto3/boto3-1.26.156.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "boto3-1.26.156.tar.gz",
                "boto3-1.26.156.tar.gz",
                ContentType.MULTIPART_FORM_DATA.toString(),
                fileContent);
        packageValidator.validate(multipart, errors);
        assertTrue(errors.hasErrors(), "Validation should return content type error");
        verify(errors, times(1)).error("CONTENT-TYPE", MessageCodes.INVALID_CONTENTTYPE);
    }

    @Test
    public void validateUploadMultipartFileWithNoContentType_shouldFail() {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        final MultipartFile multipart = new MockMultipartFile("boto3-1.26.156.tar.gz", new byte[] {});
        packageValidator.validate(multipart, errors);
        assertTrue(errors.hasErrors(), "Validation should return empty file error");
        verify(errors, times(1)).error("MULTIPART-FILE", MessageCodes.ERROR_EMPTY_FILE);
    }

    @Test
    public void validateUploadMultipartFileWithEmptyName_shouldFail() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        final File newPackage = new File("src/test/resources/unit/test_packages/testrepo1/boto3/boto3-1.26.156.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "boto3-1.26.156.tar.gz", "", ContentType.MULTIPART_FORM_DATA.toString(), fileContent);
        packageValidator.validate(multipart, errors);
        assertTrue(errors.hasErrors(), "Validation should return invalid file name error");
        verify(errors, times(1)).error("MULTIPART-FILE", MessageCodes.INVALID_FILENAME);
    }

    private void prepareTest() {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        when(env.getProperty("package.version.max-numbers", "10")).thenReturn("1");
    }

    private void validateUpdatedPackageBag() {
        when(packageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        packageValidator.validate(updatedPackageBag, true, errors);
    }

    @Test
    public void validateUploadNotExistingPackage_shouldFail() {
        prepareTest();
        updatedPackageBag.setId(100);
        packageValidator.validate(updatedPackageBag, true, errors);
        verify(errors, times(1)).error("id", MessageCodes.NO_SUCH_PACKAGE_ERROR);
    }
}
