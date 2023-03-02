/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.validation.exceptions.MultipartFileValidationException;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicatedToSilentlyIgnore;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;

/**
 * Validates if package object is correct.
 * @param <T>
 */
public abstract class PackageValidator<T extends Package<T, ?>> implements Validator {
	protected final PackageService<T> packageService;
	
	public PackageValidator(PackageService<T> packageService) {
		this.packageService = packageService;
	}
	
	/**
	 * Validates package after its creation.
	 * @param packageBag
	 * @param replace if the package is supposed to replace another one
	 * @throws PackageValidationException
	 * @throws PackageDuplicatedToSilentlyIgnore 
	 */
	public abstract void validate(T packageBag, boolean replace, Errors errors) throws PackageValidationException, PackageDuplicatedToSilentlyIgnore;
	
	public abstract void validateUploadPackage(T packageBag, boolean replace) throws PackageValidationException, PackageDuplicatedToSilentlyIgnore;

	
	/**
	 * Validates R package file right after upload, before it gets passed further.
	 * @param multipartFile
	 * @throws MultipartFileValidationException
	 */
	public abstract void validate(MultipartFile multipartFile) throws MultipartFileValidationException;
}
