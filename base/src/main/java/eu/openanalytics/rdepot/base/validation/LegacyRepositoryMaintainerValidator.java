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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.service.CommonRepositoryService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;

@Deprecated
@Component
public class LegacyRepositoryMaintainerValidator extends RepositoryMaintainerValidator {

	private final UserService userService;
	
	public LegacyRepositoryMaintainerValidator(UserService userService, 
			CommonRepositoryService repositoryService,
			RepositoryMaintainerService repositoryMaintainerService) {
		super(userService, repositoryService, repositoryMaintainerService);
		this.userService = userService;
	}

	@Override
	protected void validateUser(RepositoryMaintainer maintainer, Errors errors) {
		if(maintainer.getUser() == null 
				|| userService.findById(maintainer.getUser().getId()).isEmpty()) {
			errors.rejectValue("user", RefactoredMessageCodes.EMPTY_USER);
		}

	}
}
