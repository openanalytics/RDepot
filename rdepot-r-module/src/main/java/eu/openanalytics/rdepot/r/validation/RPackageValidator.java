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
package eu.openanalytics.rdepot.r.validation;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validates R packages.
 */
@Component
public class RPackageValidator implements PackageValidator<RPackage> {

    private final Environment env;
    private final SubmissionService submissionService;
    private final RPackageService packageService;

    public RPackageValidator(
            SubmissionService submissionService, RPackageService rPackageService, Environment environment) {
        this.env = environment;
        this.submissionService = submissionService;
        this.packageService = rPackageService;
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
            RPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> validationResult) {
        validateName(packageBag.getName(), validationResult);
        validateNotEmpty("description", packageBag.getDescription(), MessageCodes.EMPTY_DESCRIPTION, validationResult);
        validateNotEmpty("author", packageBag.getAuthor(), MessageCodes.EMPTY_AUTHOR, validationResult);
        validateNotEmpty("license", packageBag.getLicense(), MessageCodes.EMPTY_LICENSE, validationResult);
        validateNotEmpty("title", packageBag.getTitle(), MessageCodes.EMPTY_TITLE, validationResult);
        validateNotEmpty("md5sum", packageBag.getMd5sum(), MessageCodes.EMPTY_MD5SUM, validationResult);
        validateVersion(packageBag, replace, validationResult);
    }

    private void validateName(final String name, final DataSpecificValidationResult<Submission> validationResult) {
        validateNotEmpty("name", name, MessageCodes.EMPTY_NAME, validationResult);
        if (!StringUtils.isAsciiPrintable(name) || !name.matches("^[A-Za-z][A-Za-z\\d.]+(?<!\\.)$"))
            validationResult.error("name", MessageCodes.INVALID_PACKAGE_NAME);
    }

    public void validate(RPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> errors) {
        validateUploadPackage(packageBag, replace, errors);
        if (packageBag.getId() > 0) {
            Optional<RPackage> exsitingPackageOptional = packageService.findOneNonDeleted(packageBag.getId());
            if (exsitingPackageOptional.isEmpty()) {
                errors.error("id", MessageCodes.NO_SUCH_PACKAGE_ERROR);
            } else {
                RPackage existingPackage = exsitingPackageOptional.get();
                if (existingPackage.getName() != null
                        && !existingPackage.getName().equals(packageBag.getName()))
                    errors.error("name", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getDescription() != null
                        && !existingPackage.getDescription().equals(packageBag.getDescription()))
                    errors.error("description", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getVersion() != null
                        && !existingPackage.getVersion().equals(packageBag.getVersion()))
                    errors.error("version", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getAuthor() != null
                        && !existingPackage.getAuthor().equals(packageBag.getAuthor()))
                    errors.error("author", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getLicense() != null
                        && !existingPackage.getLicense().equals(packageBag.getLicense()))
                    errors.error("license", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getTitle() != null
                        && !existingPackage.getTitle().equals(packageBag.getTitle()))
                    errors.error("title", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getUrl() != null
                        && !existingPackage.getUrl().equals(packageBag.getUrl()))
                    errors.error("url", MessageCodes.FORBIDDEN_UPDATE);
                if (packageBag.getSubmission() != null
                        && !(existingPackage.getSubmission() == packageBag.getSubmission()))
                    errors.error("submission", MessageCodes.FORBIDDEN_UPDATE);
                if (packageBag.getSource() != null && !packageBag.getSource().equals(existingPackage.getSource()))
                    errors.error("source", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getUser() != null
                        && !existingPackage.getUser().equals(packageBag.getUser()))
                    errors.error("user", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getRepository() != null
                        && !existingPackage.getRepository().equals(packageBag.getRepository()))
                    errors.error("repository", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getMd5sum() != null
                        && !existingPackage.getMd5sum().equals(packageBag.getMd5sum()))
                    errors.error("md5sum", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getDepends() != null
                        && !existingPackage.getDepends().equals(packageBag.getDepends()))
                    errors.error("depends", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getImports() != null
                        && !existingPackage.getImports().equals(packageBag.getImports()))
                    errors.error("imports", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getSuggests() != null
                        && !existingPackage.getSuggests().equals(packageBag.getSuggests()))
                    errors.error("suggests", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getSystemRequirements() != null
                        && !existingPackage.getSystemRequirements().equals(packageBag.getSystemRequirements()))
                    errors.error("systemRequirements", MessageCodes.FORBIDDEN_UPDATE);
                if (existingPackage.getGenerateManuals() != null
                        && packageBag.getGenerateManuals() != null
                        && !existingPackage.getGenerateManuals().equals(packageBag.getGenerateManuals()))
                    errors.error("generateManuals", MessageCodes.FORBIDDEN_UPDATE);
            }
        }
    }

    private void validateVersion(
            RPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> validationResult) {
        String version = packageBag.getVersion();
        validateNotEmpty("version", version, MessageCodes.EMPTY_VERSION, validationResult);

        String[] tokens = version.split("[-.]");
        String name = packageBag.getName();
        RRepository repository = packageBag.getRepository();
        int maxLength = Integer.parseInt(env.getProperty("package.version.max-numbers", "10"));

        if (tokens.length > maxLength) {
            validationResult.error("version", MessageCodes.INVALID_VERSION);
        }

        Optional<RPackage> sameVersionOpt =
                packageService.findByNameAndVersionAndRepositoryAndDeleted(name, version, repository, false);
        if (sameVersionOpt.isPresent() && packageBag.getId() <= 0) {
            // TODO: #32883 It is better to fetch submission immediately but it probably requires a custom SQL query
            Submission submission = submissionService
                    .findByPackage(sameVersionOpt.get())
                    .orElseThrow(() -> new IllegalStateException(
                            "There is a package without submission which is not allowed: " + sameVersionOpt.get()));
            validationResult.warning(
                    "version",
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
        boolean valid = false;

        String[] tokens =
                Objects.requireNonNull(multipartFile.getOriginalFilename()).split("_");

        if (tokens.length >= 2) {
            String name = tokens[0];
            if (name != null && !name.isEmpty() && !name.trim().isEmpty()) {
                valid = true;
            }
        }

        if (!valid) validationResult.error("MULTIPART-FILE", MessageCodes.INVALID_FILENAME);
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
