/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.validation;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.exception.PackageValidationException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.warning.PackageValidationWarning;

@Component
public class PackageValidator
{
	
	@Resource
	private Environment environment;
	
	@Autowired
	private PackageService packageService;
	
	//TODO: implement this using Spring validator?
	// There's no need for "rejectValue", can use "reject" and check for error code, then throw error or warning?
	
	public void validate(Package target, boolean replace) throws PackageValidationException, PackageValidationWarning
	{
		//validateRepository!!
		//validateSource!!
		validateName(target);
		validateVersion(target, replace);
		validateDescription(target);
		validateAuthor(target);
		validateLicense(target);
		validateTitle(target);
		validateMd5Sum(target);
	}
	
	private void validateMd5Sum(Package target) throws PackageValidationException 
	{
		String md5sum = target.getMd5sum();
		if(md5sum == null || md5sum.isEmpty() || md5sum.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_EMPTY_MD5SUM, target);
	}

	private void validateName(Package target) throws PackageValidationException
	{
		String name = target.getName();
		if(name == null || name.isEmpty() || name.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_EMPTY_NAME, target);
	}
	
	private void validateVersion(Package target, boolean replace) throws PackageValidationException, PackageValidationWarning
	{
		String version = target.getVersion();
		String[] splitted = version.split("-|\\.");
		String name = target.getName();
		Repository repository = target.getRepository();
		if(version == null || version.isEmpty() || version.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_INVALID_VERSION, target);

		int maxLength = Integer.valueOf(environment.getProperty("package.version.max-numbers", "10"));
		if(splitted.length > maxLength) {
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_INVALID_VERSION, target);
		}
		
		if(target.getId() <= 0 && !replace)
		{
			Package checkSameVersion = packageService.findByNameAndVersionAndRepository(name, version, repository);
			if(checkSameVersion != null)
			{
				throw new PackageValidationWarning(MessageCodes.WARNING_PACKAGE_DUPLICATE, target);
			}
		}
	}
	
	private void validateDescription(Package target) throws PackageValidationException
	{
		String description = target.getDescription();
		if(description == null || description.isEmpty() || description.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_EMPTY_DESCRIPTION, target);
	}
	
	private void validateAuthor(Package target) throws PackageValidationException
	{
		String author = target.getAuthor();
		if(author == null || author.isEmpty() || author.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_EMPTY_AUTHOR, target);
	}
	
	private void validateLicense(Package target) throws PackageValidationException
	{
		String license = target.getLicense();
		if(license == null || license.isEmpty() || license.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_EMPTY_LICENSE, target);
	}
	
	private void validateTitle(Package target) throws PackageValidationException
	{
		String title = target.getTitle();
		if(title == null || title.isEmpty() || title.trim().equals(""))
			throw new PackageValidationException(MessageCodes.ERROR_PACKAGE_EMPTY_TITLE, target);
	}
}
