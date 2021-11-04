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
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.service.RepositoryService;

@Component
public class RepositoryValidator implements Validator {
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Repository.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		Repository repository = (Repository) target;
		ValidationUtils.rejectIfEmpty(errors, "name", MessageCodes.ERROR_FORM_EMPTY_NAME);
		ValidationUtils.rejectIfEmpty(errors, "publicationUri", MessageCodes.ERROR_FORM_EMPTY_PUBLICATIONURI);
		ValidationUtils.rejectIfEmpty(errors, "serverAddress", MessageCodes.ERROR_FORM_EMPTY_SERVERADDRESS);
		if(repository.getId() == 0)
		{
			Repository nameCheck = repositoryService.findByName(repository.getName());
			if(nameCheck != null)
				errors.rejectValue("name", MessageCodes.ERROR_FORM_DUPLICATE_NAME);
			Repository publicationUriCheck = repositoryService.findByPublicationUri(repository.getPublicationUri());
			if(publicationUriCheck != null)
				errors.rejectValue("publicationUri", MessageCodes.ERROR_FORM_DUPLICATE_PUBLICATIONURI);
			Repository serverAddressCheck = repositoryService.findByServerAddress(repository.getServerAddress());
			if(serverAddressCheck != null)
				errors.rejectValue("serverAddress", MessageCodes.ERROR_FORM_DUPLICATE_SERVERADDRESS);
		}
		else
		{
			Repository originalRepository = repositoryService.findById(repository.getId());
			if(originalRepository == null)
			{
				errors.rejectValue("id", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			}
			else
			{
				Repository nameCheck = repositoryService.findByName(repository.getName());
				if(nameCheck != null && nameCheck.getId() != originalRepository.getId())
					errors.rejectValue("name", MessageCodes.ERROR_FORM_DUPLICATE_NAME);
				Repository publicationUriCheck = repositoryService.findByPublicationUri(repository.getPublicationUri());
				if(publicationUriCheck != null && publicationUriCheck.getId() != originalRepository.getId())
					errors.rejectValue("publicationUri", MessageCodes.ERROR_FORM_DUPLICATE_PUBLICATIONURI);
				Repository serverAddressCheck = repositoryService.findByServerAddress(repository.getServerAddress());
				if(serverAddressCheck != null && serverAddressCheck.getId() != originalRepository.getId())
					errors.rejectValue("serverAddress", MessageCodes.ERROR_FORM_DUPLICATE_SERVERADDRESS);
			}
		}
	}
}
