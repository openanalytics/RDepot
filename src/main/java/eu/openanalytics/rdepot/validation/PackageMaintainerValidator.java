/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;

@Component
public class PackageMaintainerValidator implements Validator 
{
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PackageMaintainerService packageMaintainerService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return PackageMaintainer.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		PackageMaintainer packageMaintainer = (PackageMaintainer) target;
		
		ValidationUtils.rejectIfEmpty(errors, "package", MessageCodes.ERROR_FORM_EMPTY_PACKAGE);
		
		if(packageMaintainer.getUser() != null)
		{
			if(userService.findById(packageMaintainer.getUser().getId()) == null)
				errors.rejectValue("user", MessageCodes.ERROR_USER_NOT_FOUND);
		}
		else
			errors.rejectValue("user", MessageCodes.ERROR_USER_NOT_FOUND);
		
		if(packageMaintainer.getRepository() != null)
		{
			if(repositoryService.findById(packageMaintainer.getRepository().getId()) == null)
				errors.rejectValue("repository", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		}
		else
			errors.rejectValue("repository", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		
		// Disallow multiple package maintainers for 1 package in 1 repository
		PackageMaintainer duplicateCheck = packageMaintainerService.findByPackageAndRepository(packageMaintainer.getPackage(), packageMaintainer.getRepository());
		if(packageMaintainer.getId() <= 0)
		{
			if(duplicateCheck != null)
				errors.rejectValue("package", MessageCodes.ERROR_PACKAGE_ALREADY_MAINTAINED);
		}
		else
		{
			if(duplicateCheck != null && duplicateCheck.getId() != packageMaintainer.getId())
				errors.rejectValue("package", MessageCodes.ERROR_PACKAGE_ALREADY_MAINTAINED);
		}
		
	}

}
