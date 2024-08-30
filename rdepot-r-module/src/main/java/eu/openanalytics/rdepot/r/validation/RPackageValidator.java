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
import eu.openanalytics.rdepot.r.config.RBinaryProperties;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import java.util.List;
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
    private final RBinaryProperties rBinaryProperties;

    public RPackageValidator(
            SubmissionService submissionService,
            RPackageService rPackageService,
            Environment environment,
            RBinaryProperties rBinaryProperties) {
        this.env = environment;
        this.submissionService = submissionService;
        this.packageService = rPackageService;
        this.rBinaryProperties = rBinaryProperties;
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

        if (packageBag.isBinary()) validateBinaryProperties(packageBag, validationResult);
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
                validatePropertyChange(existingPackage, packageBag, errors);
            }
        }
    }

    private void validatePropertyChange(
            RPackage existingPackage, RPackage uploadedPackage, DataSpecificValidationResult<Submission> errors) {
        if (existingPackage.getName() != null && !existingPackage.getName().equals(uploadedPackage.getName()))
            errors.error("name", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getDescription() != null
                && !existingPackage.getDescription().equals(uploadedPackage.getDescription()))
            errors.error("description", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getVersion() != null
                && !existingPackage.getVersion().equals(uploadedPackage.getVersion()))
            errors.error("version", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getAuthor() != null && !existingPackage.getAuthor().equals(uploadedPackage.getAuthor()))
            errors.error("author", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getLicense() != null
                && !existingPackage.getLicense().equals(uploadedPackage.getLicense()))
            errors.error("license", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getTitle() != null && !existingPackage.getTitle().equals(uploadedPackage.getTitle()))
            errors.error("title", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getUrl() != null && !existingPackage.getUrl().equals(uploadedPackage.getUrl()))
            errors.error("url", MessageCodes.FORBIDDEN_UPDATE);
        if (uploadedPackage.getSubmission() != null
                && !(existingPackage.getSubmission().equals(uploadedPackage.getSubmission())))
            errors.error("submission", MessageCodes.FORBIDDEN_UPDATE);
        if (uploadedPackage.getSource() != null && !uploadedPackage.getSource().equals(existingPackage.getSource()))
            errors.error("source", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getUser() != null && !existingPackage.getUser().equals(uploadedPackage.getUser()))
            errors.error("user", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getRepository() != null
                && !existingPackage.getRepository().equals(uploadedPackage.getRepository()))
            errors.error("repository", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getMd5sum() != null && !existingPackage.getMd5sum().equals(uploadedPackage.getMd5sum()))
            errors.error("md5sum", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getDepends() != null
                && !existingPackage.getDepends().equals(uploadedPackage.getDepends()))
            errors.error("depends", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getImports() != null
                && !existingPackage.getImports().equals(uploadedPackage.getImports()))
            errors.error("imports", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getSuggests() != null
                && !existingPackage.getSuggests().equals(uploadedPackage.getSuggests()))
            errors.error("suggests", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getSystemRequirements() != null
                && !existingPackage.getSystemRequirements().equals(uploadedPackage.getSystemRequirements()))
            errors.error("systemRequirements", MessageCodes.FORBIDDEN_UPDATE);
        if (existingPackage.getGenerateManuals() != null
                && uploadedPackage.getGenerateManuals() != null
                && !existingPackage.getGenerateManuals().equals(uploadedPackage.getGenerateManuals()))
            errors.error("generateManuals", MessageCodes.FORBIDDEN_UPDATE);
    }

    private void validateVersion(
            RPackage packageBag, boolean replace, DataSpecificValidationResult<Submission> validationResult) {
        String packageVersion = packageBag.getVersion();
        final String version = "version";
        validateNotEmpty(version, packageVersion, MessageCodes.EMPTY_VERSION, validationResult);

        String[] tokens = packageVersion.split("[-.]");
        String name = packageBag.getName();
        RRepository repository = packageBag.getRepository();
        int maxLength = Integer.parseInt(env.getProperty("package.version.max-numbers", "10"));

        if (tokens.length > maxLength) {
            validationResult.error(version, MessageCodes.INVALID_VERSION);
        }

        Optional<RPackage> sameVersionOpt =
                packageService.findByNameAndVersionAndRepositoryAndDeleted(name, packageVersion, repository, false);
        if (sameVersionOpt.isPresent() && packageBag.getId() <= 0) {
            // TODO: #32883 It is better to fetch submission immediately but it probably requires a custom SQL query
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

    public void validateBinaryProperties(RPackage binaryPackage, DataSpecificValidationResult<Submission> result) {
        validateNotEmpty("built", binaryPackage.getBuilt(), MessageCodes.EMPTY_BUILT, result);
        if (!result.getErrors().isEmpty()
                && result.getErrors()
                        .get(result.getErrors().size() - 1)
                        .messageCode()
                        .equals(MessageCodes.EMPTY_BUILT)) return;
        validateRVersion(binaryPackage.getRVersion(), binaryPackage.getBuilt(), result);
        validateArchitecture(binaryPackage.getArchitecture(), binaryPackage.getBuilt(), result);
        validateDistribution(binaryPackage.getDistribution(), result);
    }

    private void validateRVersion(
            final String rVersion,
            final String built,
            final DataSpecificValidationResult<Submission> validationResult) {
        if (!compareRVersions(rBinaryProperties.getRVersions(), rVersion))
            validationResult.error("rVersion", MessageCodes.R_VERSION_NOT_ALLOWED);

        String builtRVersion = built.split(";")[0].replace("R", "").trim();
        if (!compareRVersions(builtRVersion, rVersion))
            validationResult.error("rVersion", MessageCodes.INVALID_R_VERSION);
    }

    private boolean compareRVersions(List<String> allowedRVersions, final String rVersion) {

        if (allowedRVersions.contains(rVersion)) return true;

        boolean versionAccepted = false;
        for (String allowedVersion : allowedRVersions) {

            versionAccepted = compareRVersions(allowedVersion, rVersion);
            if (versionAccepted) break;
        }

        return versionAccepted;
    }

    private boolean compareRVersions(String allowedVersion, final String rVersion) {

        if (allowedVersion.length() > rVersion.length()) return false;

        for (int i = 0; i < allowedVersion.length(); i++) {
            if (Character.isDigit(allowedVersion.charAt(i)) && allowedVersion.charAt(i) != rVersion.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    private void validateArchitecture(
            final String architecture,
            final String built,
            final DataSpecificValidationResult<Submission> validationResult) {
        if (!rBinaryProperties.getArchitectures().contains(architecture))
            validationResult.error("architecture", MessageCodes.ARCHITECTURE_NOT_ALLOWED);

        String builtOSinfo = built.split(";")[1].trim();
        if (!builtOSinfo.isEmpty()) {
            String builtArch = builtOSinfo.split("-")[0];
            if (!builtArch.equals(architecture))
                validationResult.error("architecture", MessageCodes.INVALID_ARCHITECTURE);
        }
    }

    private void validateDistribution(
            final String distribution, final DataSpecificValidationResult<Submission> validationResult) {
        if (!rBinaryProperties.getDistributions().contains(distribution))
            validationResult.error("distribution", MessageCodes.DISTRIBUTION_NOT_ALLOWED);
    }
}
