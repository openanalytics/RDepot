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
package eu.openanalytics.rdepot.r.validation;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.exceptions.MultipartFileValidationException;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicatedToSilentlyIgnore;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;

/**
 * Validates R packages.
 */
@Component
public class RPackageValidator extends PackageValidator<RPackage> {

	private final Environment env;
	
	public RPackageValidator(RPackageService packageService, Environment environment) {
		super(packageService);
		this.env = environment;
	}
	
	private void validateNotEmpty(String property, String messageCode) throws PackageValidationException {
		if(property == null || property.isEmpty() || property.trim().equals("")) {
			throw new PackageValidationException(messageCode);
		}
	}
	
	
	public void validateUploadPackage(RPackage packageBag, boolean replace) throws PackageValidationException, PackageDuplicatedToSilentlyIgnore{
		validateName(packageBag.getName(), RefactoredMessageCodes.INVALID_PACKAGE_NAME);
		validateNotEmpty(packageBag.getDescription(), RefactoredMessageCodes.EMPTY_DESCRIPTION);
		validateNotEmpty(packageBag.getAuthor(), RefactoredMessageCodes.EMPTY_AUTHOR);
		validateNotEmpty(packageBag.getLicense(), RefactoredMessageCodes.EMPTY_LICENSE);
		validateNotEmpty(packageBag.getTitle(), RefactoredMessageCodes.EMPTY_TITLE);
		validateNotEmpty(packageBag.getMd5sum(), RefactoredMessageCodes.EMPTY_MD5SUM);
		validateVersion(packageBag, replace);
	}
	
	private void validateName(final String name, final String messageCode) throws PackageValidationException {
		validateNotEmpty(name, RefactoredMessageCodes.EMPTY_NAME);
		if(!StringUtils.isAsciiPrintable(name) || !name.matches("^[A-Za-z][A-Za-z\\d.]+(?<!\\.)$"))
			throw new PackageValidationException(messageCode);
	}

	public void validate(RPackage packageBag, boolean replace, Errors errors) throws PackageValidationException, PackageDuplicatedToSilentlyIgnore {
		validateUploadPackage(packageBag, replace);
		if(packageBag.getId() > 0) {
			Optional<RPackage> exsitingPackageOptional = packageService.findById(packageBag.getId()); 
			if(exsitingPackageOptional.isEmpty()) {
				errors.rejectValue("id", RefactoredMessageCodes.NO_SUCH_PACKAGE_ERROR);
			} else {
				RPackage existingPackage = exsitingPackageOptional.get();
				if(existingPackage.getName() != null && !existingPackage.getName().equals(packageBag.getName())) 
					errors.rejectValue("name", RefactoredMessageCodes.FORBIDDEN_UPDATE);					
				if(existingPackage.getDescription() != null && !existingPackage.getDescription().equals(packageBag.getDescription()))
					errors.rejectValue("description", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getVersion() != null && !existingPackage.getVersion().equals(packageBag.getVersion()))
					errors.rejectValue("version", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getAuthor() != null && !existingPackage.getAuthor().equals(packageBag.getAuthor()))
					errors.rejectValue("author", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getLicense() != null && !existingPackage.getLicense().equals(packageBag.getLicense()))
					errors.rejectValue("license", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getTitle() != null && !existingPackage.getTitle().equals(packageBag.getTitle()))
					errors.rejectValue("title", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getUrl() != null && !existingPackage.getUrl().equals(packageBag.getUrl()))
					errors.rejectValue("url", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(packageBag.getSubmission() != null && !(existingPackage.getSubmission() == packageBag.getSubmission()))
					errors.rejectValue("submission", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(packageBag.getSource() != null && !packageBag.getSource().equals(existingPackage.getSource()))
					errors.rejectValue("source", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getUser() != null && !existingPackage.getUser().equals(packageBag.getUser()))
					errors.rejectValue("user", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getRepository() != null && !existingPackage.getRepository().equals(packageBag.getRepository()))
					errors.rejectValue("repository", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getMd5sum() != null && !existingPackage.getMd5sum().equals(packageBag.getMd5sum()))
					errors.rejectValue("md5sum", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getDepends() != null && !existingPackage.getDepends().equals(packageBag.getDepends()))
					errors.rejectValue("depends", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getImports() != null && !existingPackage.getImports().equals(packageBag.getImports()))
					errors.rejectValue("imports", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getSuggests() != null && !existingPackage.getSuggests().equals(packageBag.getSuggests()))
					errors.rejectValue("suggests", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getSystemRequirements() != null && !existingPackage.getSystemRequirements().equals(packageBag.getSystemRequirements()))
					errors.rejectValue("systemRequirements", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(existingPackage.getGenerateManuals() != null && packageBag.getGenerateManuals() != null && !existingPackage.getGenerateManuals() == packageBag.getGenerateManuals())
					errors.rejectValue("generateManuals", RefactoredMessageCodes.FORBIDDEN_UPDATE);
			}
		}
	}

	private void validateVersion(RPackage packageBag, boolean replace) throws PackageValidationException, PackageDuplicatedToSilentlyIgnore {
		String version = packageBag.getVersion();
		validateNotEmpty(version, RefactoredMessageCodes.EMPTY_VERSION);
		
		String[] tokens = version.split("-|\\.");
		String name = packageBag.getName();
		RRepository repository = packageBag.getRepository();
		int maxLength = Integer.valueOf(env.getProperty("package.version.max-numbers", "10"));
		
		if(tokens.length > maxLength) {
			throw new PackageValidationException(RefactoredMessageCodes.INVALID_VERSION);
		}
		if(packageBag.getId() <= 0 && !replace
				&& !packageService.findByNameAndVersionAndRepository(name, version, repository).isEmpty()) {
			throw new PackageDuplicatedToSilentlyIgnore(RefactoredMessageCodes.DUPLICATE_VERSION);
		}
		
	}
	
	public void validate(MultipartFile multipartFile) throws MultipartFileValidationException {
		validateContentType(multipartFile);
		validateSize(multipartFile);
		validateFilename(multipartFile);
	}

	private void validateFilename(MultipartFile multipartFile) throws MultipartFileValidationException {
		boolean valid = false;
		
		String[] tokens = multipartFile.getOriginalFilename().split("_");
		
		if(tokens.length >= 2) {
			String name = tokens[0];
			if(name != null && !name.isEmpty() && !name.trim().equals("")) {
				valid = true;
			}
		}
		
		if(!valid)
			throw new MultipartFileValidationException(RefactoredMessageCodes.INVALID_FILENAME);
	}

	private void validateSize(MultipartFile multipartFile) throws MultipartFileValidationException {
		if(multipartFile.getSize() <= 0) {
			throw new MultipartFileValidationException(RefactoredMessageCodes.ERROR_EMPTY_FILE);
		}
	}

	private void validateContentType(MultipartFile multipartFile) throws MultipartFileValidationException {
		if(!(Objects.equals(multipartFile.getContentType(), "application/gzip") || 
			Objects.equals(multipartFile.getContentType(), "application/x-gzip"))) {
			throw new MultipartFileValidationException(RefactoredMessageCodes.INVALID_CONTENTTYPE + ": " + multipartFile.getContentType());
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return RPackage.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate((RPackage)target, errors);
	}

}
