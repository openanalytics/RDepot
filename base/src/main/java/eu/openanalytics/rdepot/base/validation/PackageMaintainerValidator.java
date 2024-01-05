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

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.service.CommonRepositoryService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;

@Component
public class PackageMaintainerValidator implements Validator {

	private final UserService userService;
	private final CommonRepositoryService repositoryService;
	private final PackageMaintainerService packageMaintainerService;
	
	public PackageMaintainerValidator(UserService userService,
			CommonRepositoryService commonRepositoryService, 
			PackageMaintainerService packageMaintainerService) {
		this.userService = userService;
		this.repositoryService = commonRepositoryService;
		this.packageMaintainerService = packageMaintainerService;
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(PackageMaintainer.class);
	}
	
	protected void validateUser(PackageMaintainer maintainer, Errors errors) {
		Optional<User> userOpt = Optional.empty();
		if(maintainer.getUser() != null) {
			userOpt = userService.findById(maintainer.getUser().getId());
		}
		if(userOpt.isEmpty()) {
			errors.rejectValue("user", RefactoredMessageCodes.EMPTY_USER);
		} else if(userOpt.get().getRole().getValue() < Role.VALUE.PACKAGEMAINTAINER) {
			errors.rejectValue("user", RefactoredMessageCodes.USER_PERMISSIONS_NOT_SUFFICIENT);
		}
	}

	@Override
	public void validate(Object target, Errors errors) {
		PackageMaintainer maintainer = (PackageMaintainer)target;
		
		ValidationUtils.rejectIfEmpty(errors, "packageName", RefactoredMessageCodes.EMPTY_PACKAGE);		
		
		validateUser(maintainer, errors);
		
		if(maintainer.getRepository() == null || 
				repositoryService.findById(maintainer.getRepository().getId()).isEmpty()) {
			errors.rejectValue("repository", RefactoredMessageCodes.REPOSITORY_NOT_FOUND);
		}
		
		Optional<PackageMaintainer> duplicateCheck = 
				packageMaintainerService.findByPackageAndRepositoryAndNonDeleted(
						maintainer.getPackageName(), maintainer.getRepository());
		
		if(duplicateCheck.isPresent() 
				&& (maintainer.getId() <= 0 || duplicateCheck.get().getId() != maintainer.getId())) {
			errors.rejectValue("packageName", RefactoredMessageCodes.PACKAGE_ALREADY_MAINTAINED);
		}
	}

}
