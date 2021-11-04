/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;

@Component
public class RepositoryMaintainerValidator implements Validator 
{
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Override
	public boolean supports(Class<?> clazz) 
	{
		return RepositoryMaintainer.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		RepositoryMaintainer repositoryMaintainer = (RepositoryMaintainer) target;
		
		if(repositoryMaintainer.getUser() != null)
		{
			if(userService.findById(repositoryMaintainer.getUser().getId()) == null)
				errors.rejectValue("user", MessageCodes.ERROR_USER_NOT_FOUND);
		}
		else
			errors.rejectValue("user", MessageCodes.ERROR_USER_NOT_FOUND);
		
		if(repositoryMaintainer.getRepository() != null)
		{
			if(repositoryService.findById(repositoryMaintainer.getRepository().getId()) == null)
				errors.rejectValue("repository", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		}
		else
			errors.rejectValue("repository", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);	
		if(repositoryMaintainer.getId() <= 0)
		{
			RepositoryMaintainer duplicateCheck = repositoryMaintainerService.findByUserAndRepository(repositoryMaintainer.getUser(), repositoryMaintainer.getRepository());
			if(duplicateCheck != null)
				errors.rejectValue("repository", MessageCodes.ERROR_REPOSITORYMAINTAINER_DUPLICATE);
		}
		//Allow multiple repository maintainers for one repository
	}

}
