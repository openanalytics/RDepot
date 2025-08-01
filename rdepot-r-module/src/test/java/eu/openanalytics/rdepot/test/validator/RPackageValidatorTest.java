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
import eu.openanalytics.rdepot.r.config.RBinaryProperties;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.messaging.RMessageCodes;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RSubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
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
    private User user;
    private RPackage packageBag;
    private RPackage updatedPackageBag;
    private RPackage duplicatedPackageBag;
    private RPackage binaryPackage;
    private RPackage duplicatedBinaryPackage;
    private Submission submission;
    private Submission binarySubmission;
    private DataSpecificValidationResult<Submission> errors;

    @BeforeEach
    public void initEach() {
        user = UserTestFixture.GET_ADMIN();
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setTitle("someTitle");
        packageBag.setVersion("1");
        packageBag.setName("someName");
        updatedPackageBag = new RPackage(packageBag);
        duplicatedPackageBag = new RPackage(packageBag);
        binaryPackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        binaryPackage.setVersion("1");
        duplicatedBinaryPackage = new RPackage(binaryPackage);
        submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, duplicatedPackageBag);
        binarySubmission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, duplicatedBinaryPackage);
        packageValidator =
                new RPackageValidator(submissionService, rPackageService, env, setAllowedRBinaryProperties());
    }

    @Test
    public void validateUploadPackage_shouldSucceed() {
        prepareTest();
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
    }

    @Test
    public void validateUploadPackageWithDashInName_shouldFail() {
        prepareTest();
        packageBag.setName("accrued-smth");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithUnderscoreInName_shouldFail() {
        prepareTest();
        packageBag.setName("accrued_smth");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        log.warn(errors.getErrors().toString());
        assertTrue(errors.hasErrors(), "Validation should return package name error");
        verify(errors, times(1)).error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    @Test
    public void validateUploadPackageWithNonAsciiChar_shouldFail() {
        prepareTest();
        packageBag.setName("accrued€");
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
    public void validateUploadPackageWithEmptyMd5sum_shouldFail() {
        prepareTest();
        packageBag.setMd5sum("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return md5sum error");
        verify(errors, times(1)).error("md5sum", RMessageCodes.EMPTY_MD5SUM);
    }

    @Test
    public void validateUploadPackageWithEmptyDescription_shouldFail() {
        prepareTest();
        packageBag.setDescription("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return description error");
        verify(errors, times(1)).error("description", MessageCodes.EMPTY_DESCRIPTION);
    }

    @Test
    public void validateUploadPackageWithEmptyAuthor_shouldFail() {
        prepareTest();
        packageBag.setAuthor("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return author error");
        verify(errors, times(1)).error("author", RMessageCodes.EMPTY_AUTHOR);
    }

    @Test
    public void validateUploadPackageWithEmptyTitle_shouldFail() {
        prepareTest();
        packageBag.setTitle("");
        packageValidator.validateUploadPackage(packageBag, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return title error");
        verify(errors, times(1)).error("title", RMessageCodes.EMPTY_TITLE);
    }

    @Test
    public void validateUploadBinaryPackage_shouldSucceed() {
        prepareTest();
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard binary package");
    }

    @Test
    public void validateUploadBinaryPackageWithoutBuilt_shouldFail() {
        prepareTest();
        binaryPackage.setBuilt("");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return built error");
        verify(errors, times(1)).error("built", RMessageCodes.EMPTY_BUILT);
    }
    //    "3.6", "4.0", "4.1.0", "4.1.1", "4.2"
    @Test
    public void validateUploadBinaryPackageWithDifferentRVersions_shouldSucceed() {
        prepareTest();
        binaryPackage.setRVersion("4.2.1");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertFalse(errors.hasErrors(), "Validation should be empty for more specific rVersion than in built");
    }

    @Test
    public void validateUploadBinaryPackageWithDifferentRVersions_shouldFail() {
        prepareTest();
        binaryPackage.setRVersion("4.3");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return rVersion error");
        verify(errors, times(1)).error("rVersion", RMessageCodes.INVALID_R_VERSION);
    }

    @Test
    public void validateUploadBinaryPackageWhenRVersionNotAllowed_shouldFail() {
        prepareTest();
        binaryPackage.setRVersion("4.3");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return rVersion error");
        verify(errors, times(1)).error("rVersion", RMessageCodes.R_VERSION_NOT_ALLOWED);
    }

    @Test
    public void validateUploadBinaryPackageWhenMajorVersionsAreDifferentLengthAndSimilarBeginning_shouldFail() {
        prepareTest();
        binaryPackage.setRVersion("4.23");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return rVersion error");
        verify(errors, times(1)).error("rVersion", RMessageCodes.R_VERSION_NOT_ALLOWED);
    }

    @Test
    public void validateUploadBinaryPackageWithDifferentArchitectures_shouldFail() {
        prepareTest();
        binaryPackage.setArchitecture("x86");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return architecture error");
        verify(errors, times(1)).error("architecture", RMessageCodes.INVALID_ARCHITECTURE);
    }

    @Test
    public void validateUploadBinaryPackageWhenArchitectureNotAllowed_shouldFail() {
        prepareTest();
        binaryPackage.setArchitecture("x86_32");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return architecture error");
        verify(errors, times(1)).error("architecture", RMessageCodes.ARCHITECTURE_NOT_ALLOWED);
    }

    @Test
    public void validateUploadBinaryPackageWhenDistributionNotAllowed_shouldFail() {
        prepareTest();
        binaryPackage.setDistribution("opensuse155");
        packageValidator.validateUploadPackage(binaryPackage, true, errors);
        assertTrue(errors.hasErrors(), "Validation should return distribution error");
        verify(errors, times(1)).error("distribution", RMessageCodes.DISTRIBUTION_NOT_ALLOWED);
    }

    @Test
    public void validateUploadPackageThatExistsWithReplaceSetToTrue_shouldWarn() {
        prepareTest();
        packageBag.setId(-1);
        when(rPackageService.findByNameAndVersionAndRepositoryAndDeletedAndBinary(
                        packageBag.getName(),
                        packageBag.getVersion(),
                        packageBag.getRepository(),
                        false,
                        false,
                        null,
                        null,
                        null))
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
        when(rPackageService.findByNameAndVersionAndRepositoryAndDeletedAndBinary(
                        packageBag.getName(),
                        packageBag.getVersion(),
                        packageBag.getRepository(),
                        false,
                        false,
                        null,
                        null,
                        null))
                .thenReturn(Optional.of(duplicatedPackageBag));
        when(submissionService.findByPackage(duplicatedPackageBag)).thenReturn(Optional.of(submission));

        packageValidator.validateUploadPackage(packageBag, false, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
        assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
        verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_OFF, submission);
    }

    @Test
    public void validateUploadBinaryPackageThatExistForDifferentBinaryParameters_shouldSucceed() {
        prepareTest();
        binaryPackage.setId(-1);
        duplicatedBinaryPackage.setRVersion("4.5");

        packageValidator.validateUploadPackage(binaryPackage, false, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard binary package");
    }

    @Test
    public void validateUploadBinaryPackageThatExistForTheSameBinaryParametersAndReplaceSetToFalse_shouldWarn() {
        prepareTest();
        binaryPackage.setId(-1);
        when(rPackageService.findByNameAndVersionAndRepositoryAndDeletedAndBinary(
                        binaryPackage.getName(),
                        binaryPackage.getVersion(),
                        binaryPackage.getRepository(),
                        false,
                        binaryPackage.isBinary(),
                        binaryPackage.getRVersion(),
                        binaryPackage.getArchitecture(),
                        binaryPackage.getDistribution()))
                .thenReturn(Optional.of(duplicatedBinaryPackage));

        when(submissionService.findByPackage(duplicatedBinaryPackage)).thenReturn(Optional.of(binarySubmission));

        packageValidator.validateUploadPackage(binaryPackage, false, errors);

        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
        assertTrue(errors.hasWarnings(), "Validation should return duplications warning");
        verify(errors, times(1)).warning("version", MessageCodes.DUPLICATE_VERSION_REPLACE_OFF, binarySubmission);
    }

    @Test
    public void validateUploadNotExistingPackage_shouldFail() {
        prepareTest();
        updatedPackageBag.setId(100);
        packageValidator.validate(updatedPackageBag, true, errors);
        verify(errors, times(1)).error("id", MessageCodes.NO_SUCH_PACKAGE_ERROR);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageName() {
        prepareTest();
        updatedPackageBag.setName("newName");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("name", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageDescription() {
        prepareTest();
        updatedPackageBag.setDescription("newDesc");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("description", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageVersion() {
        prepareTest();
        updatedPackageBag.setVersion("100");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("version", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageAuthor() {
        prepareTest();
        updatedPackageBag.setAuthor("newAuthor");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("author", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageLicense() {
        prepareTest();
        updatedPackageBag.setLicense("newLicense");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("license", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageTitle() {
        prepareTest();
        updatedPackageBag.setTitle("newTitle");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("title", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageUrl() {
        prepareTest();
        updatedPackageBag.setUrl("newUrl");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("url", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSubmission() {
        prepareTest();
        updatedPackageBag.setSubmission(RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, updatedPackageBag));
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("submission", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSource() {
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
    public void updatePackage_shouldNotAllowChangingPackageRepository() {
        prepareTest();
        updatedPackageBag.setRepository(new RRepository());
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("repository", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageMd5sum() {
        prepareTest();
        updatedPackageBag.setMd5sum("newSum");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("md5sum", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageDepends() {
        prepareTest();
        updatedPackageBag.setDepends("newDepends");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("depends", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageImports() {
        prepareTest();
        updatedPackageBag.setImports("newImports");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("imports", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSuggests() {
        prepareTest();
        updatedPackageBag.setSuggests("newSuggests");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("suggests", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageSystemRequirements() {
        prepareTest();
        updatedPackageBag.setSystemRequirements("newSystemRequirements");
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("systemRequirements", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void updatePackage_shouldNotAllowChangingPackageGenerateManuals() {
        prepareTest();
        updatedPackageBag.setGenerateManuals(true);
        validateUpdatedPackageBag();
        verify(errors, times(1)).error("generateManuals", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void validateUploadMultipartFile_shouldSucceed() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart =
                new MockMultipartFile("accrued_1.2.tar.gz", "accrued_1.2.tar.gz", "application/gzip", fileContent);
        packageValidator.validate(multipart, errors);
        assertFalse(errors.hasErrors(), "Validation results should be empty for a standard package");
    }

    @Test
    public void validateUploadMultipartFileWithWrongContentType_shouldFail() throws Exception {
        errors = Mockito.spy(ValidationResultImpl.createDataSpecificResult());
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
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
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
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
        when(rPackageService.findOneNonDeleted(packageBag.getId())).thenReturn(Optional.of(packageBag));
        packageValidator.validate(updatedPackageBag, true, errors);
    }

    private RBinaryProperties setAllowedRBinaryProperties() {
        RBinaryProperties binaryProperties = new RBinaryProperties();
        binaryProperties.setRVersions(Arrays.asList("3.6", "4.0", "4.1.0", "4.1.1", "4.2"));
        binaryProperties.setArchitectures(Arrays.asList("x86_64", "x86"));
        binaryProperties.setDistributions(Arrays.asList("centos7", "rhel9", "focal"));
        return binaryProperties;
    }
}
