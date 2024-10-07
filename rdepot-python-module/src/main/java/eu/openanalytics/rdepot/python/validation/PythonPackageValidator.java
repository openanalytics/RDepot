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
package eu.openanalytics.rdepot.python.validation;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validates Python packages.
 */
@Component
public class PythonPackageValidator implements PackageValidator<PythonPackage> {

    private final Environment env;
    private final SubmissionService submissionService;
    private final PythonPackageService packageService;

    public PythonPackageValidator(
            SubmissionService submissionService, PythonPackageService pythonPackageService, Environment environment) {
        this.env = environment;
        this.packageService = pythonPackageService;
        this.submissionService = submissionService;
    }

    private void validateNotEmpty(
            @NonNull String propertyName,
            String property,
            String messageCode,
            DataSpecificValidationResult<Submission> validationResult) {
        if (StringUtils.isBlank(property)) {
            validationResult.error(propertyName, messageCode);
        }
    }

    public void validateUploadPackage(
            PythonPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> validationResult) {
        validateName(packageBag.getName(), validationResult);
        validateNotEmpty("license", packageBag.getLicense(), MessageCodes.EMPTY_LICENSE, validationResult);
        validateNotEmpty("hash", packageBag.getHash(), PythonMessageCodes.EMPTY_HASH, validationResult);
        validateVersion(packageBag, replace, validationResult);
    }

    private void validateName(final String name, final DataSpecificValidationResult<Submission> validationResult) {
        validateNotEmpty("name", name, MessageCodes.EMPTY_NAME, validationResult);
        if (!StringUtils.isAsciiPrintable(name) || !name.matches("^[A-Za-z][A-Za-z-_\\d.]+(?<!\\.)$"))
            validationResult.error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    public void validate(PythonPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> errors) {
        validateUploadPackage(packageBag, replace, errors);
        if (packageBag.getId() > 0) {
            Optional<PythonPackage> exsitingPackageOptional = packageService.findById(packageBag.getId());
            if (exsitingPackageOptional.isEmpty()) {
                errors.error("id", MessageCodes.NO_SUCH_PACKAGE_ERROR);
            } else {
                PythonPackage existingPackage = exsitingPackageOptional.get();
                validatePropertyChange(existingPackage, packageBag, errors);
            }
        }
    }

    private void validatePropertyChange(
            PythonPackage existingPackage,
            PythonPackage uploadedPackage,
            DataSpecificValidationResult<Submission> errors) {
        if (existingPackage.getName() != null && !existingPackage.getName().equals(uploadedPackage.getName()))
            errors.error("name", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getVersion() != null
                && !existingPackage.getVersion().equals(uploadedPackage.getVersion()))
            errors.error("version", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getLicense() != null
                && !existingPackage.getLicense().equals(uploadedPackage.getLicense()))
            errors.error("license", MessageCodes.FORBIDDEN_UPDATE);
        if (uploadedPackage.getSource() != null && !uploadedPackage.getSource().equals(existingPackage.getSource()))
            errors.error("source", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getUser() != null && !existingPackage.getUser().equals(uploadedPackage.getUser()))
            errors.error("user", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getRepository() != null
                && !existingPackage.getRepository().equals(uploadedPackage.getRepository()))
            errors.error("repository", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getHash() != null && !existingPackage.getHash().equals(uploadedPackage.getHash()))
            errors.error("hash", MessageCodes.FORBIDDEN_UPDATE);
    }

    private void validateVersion(
            PythonPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> validationResult) {
        String packageVersion = packageBag.getVersion();
        final String version = "version";
        validateNotEmpty(version, packageVersion, MessageCodes.EMPTY_VERSION, validationResult);

        String[] tokens = packageVersion.split("-|\\.");
        String name = packageBag.getName();
        PythonRepository repository = packageBag.getRepository();
        int maxLength = Integer.parseInt(env.getProperty("package.version.max-numbers", "10"));

        if (tokens.length > maxLength) {
            validationResult.error(version, MessageCodes.INVALID_VERSION);
        }

        Optional<PythonPackage> sameVersionOpt =
                packageService.findByNameAndVersionAndRepositoryAndDeleted(name, packageVersion, repository, false);

        if (sameVersionOpt.isPresent() && packageBag.getId() <= 0) {
            Submission submission = submissionService
                    .findByPackage(sameVersionOpt.get())
                    .orElseThrow(() -> new IllegalStateException(
                            "There is a package without submission which is not allowed: " + sameVersionOpt.get()));
            validationResult.warning(
                    version,
                    replace ? MessageCodes.DUPLICATE_VERSION_REPLACE_ON : MessageCodes.DUPLICATE_VERSION_REPLACE_OFF,
                    submission);
        }
    }

    public void validate(MultipartFile multipartFile, ValidationResult validationResult) {
        validateContentType(multipartFile, validationResult);
        validateSize(multipartFile, validationResult);
        validateFilename(multipartFile, validationResult);
    }

    private void validateFilename(MultipartFile multipartFile, ValidationResult validationResult) {
        String name = StringUtils.substringBeforeLast(multipartFile.getOriginalFilename(), "-");
        if (!(name != null && !name.isEmpty() && !name.trim().equals(""))) {
            validationResult.error("MULTIPART-FILE", MessageCodes.INVALID_FILENAME);
        }
    }

    private void validateSize(MultipartFile multipartFile, ValidationResult validationResult) {
        if (multipartFile.getSize() <= 0) {
            validationResult.error("MULTIPART-FILE", MessageCodes.ERROR_EMPTY_FILE);
        }
    }

    private void validateContentType(MultipartFile multipartFile, ValidationResult validationResult) {
        if (!(Objects.equals(multipartFile.getContentType(), "application/gzip")
                || Objects.equals(multipartFile.getContentType(), "application/x-gzip"))) {
            validationResult.error("CONTENT-TYPE", MessageCodes.INVALID_CONTENTTYPE);
        }
    }
}
