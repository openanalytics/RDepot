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
package eu.openanalytics.rdepot.base.validation;

import eu.openanalytics.rdepot.base.entities.Submission;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.service.PackageService;

/**
 * Validates if package object is correct.
 * @param <T>
 */
public interface PackageValidator<T extends Package> {
	/**
	 * Validates package after its creation.
	 * @param packageBag package to validate
	 * @param replace if the package is supposed to replace another one
	 */
	void validate(T packageBag, boolean replace, DataSpecificValidationResult<Submission> errors);
	
	void validateUploadPackage(
			T packageBag, boolean replace,
			DataSpecificValidationResult<Submission> validationResult
	);

	/**
	 * Validates R package file right after upload, before it gets passed further.
	 */
	void validate(MultipartFile multipartFile, ValidationResult validationResult);
}
