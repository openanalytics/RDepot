/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.validation;

import java.util.Optional;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryService;

public abstract class RepositoryValidator<R extends Repository<R, ?>> 
	implements Validator {
	
	private final RepositoryService<R> repositoryService;
	
	public RepositoryValidator(RepositoryService<R> repositoryService) {
		this.repositoryService = repositoryService;
	}

	protected void validate(R repository, Errors errors) {
		if(repository.getId() > 0) {
			Optional<R> exsitingRepositoyOptional = repositoryService.findById(repository.getId()); 
			if(exsitingRepositoyOptional.isEmpty()) {
				errors.rejectValue("id", RefactoredMessageCodes.REPOSITORY_NOT_FOUND);
			} else {
				R existingRepository = exsitingRepositoyOptional.get();
				if(existingRepository.getVersion() != repository.getVersion())
					errors.rejectValue("version", RefactoredMessageCodes.FORBIDDEN_UPDATE);
			}
		}
		
		if(repository.getName() == null || repository.getName().isBlank()) {
			errors.rejectValue("name", RefactoredMessageCodes.EMPTY_NAME);
		} else {
			Optional<R> duplicateName = repositoryService.findByName(repository.getName());
			if(duplicateName.isPresent() && duplicateName.get().getId() != repository.getId()) {
				errors.rejectValue("name", RefactoredMessageCodes.ERROR_DUPLICATE_NAME);
			}
		}
		if(repository.getPublicationUri() == null || repository.getPublicationUri().isBlank()) {
			errors.rejectValue("publicationUri", RefactoredMessageCodes.EMPTY_PUBLICATIONURI);
		} else {
			Optional<R> duplicatePublicationUri = 
					repositoryService.findByPublicationUri(repository.getPublicationUri());
			if(duplicatePublicationUri.isPresent() 
					&& duplicatePublicationUri.get().getId() != repository.getId()) {
				errors.rejectValue("publicationUri", RefactoredMessageCodes.ERROR_DUPLICATE_PUBLICATIONURI);
			}
		}
		if(repository.getServerAddress() == null || repository.getServerAddress().isBlank()) {
			errors.rejectValue("serverAddress", RefactoredMessageCodes.EMPTY_SERVERADDRESS);
		} else {
			Optional<R> duplicateServerAddress = 
					repositoryService.findByServerAddress(repository.getServerAddress());
			if(duplicateServerAddress.isPresent() 
					&& duplicateServerAddress.get().getId() != repository.getId()) {
				errors.rejectValue("serverAddress", RefactoredMessageCodes.DUPLICATE_SERVERADDRESS);
			}
		}
	}
}
