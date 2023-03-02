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
package eu.openanalytics.rdepot.r.legacy.security.authorization;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediatorImpl;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.SubmissionV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.UserV1Dto;
import eu.openanalytics.rdepot.r.services.RPackageService;

@Component
@Deprecated(since = "1.8.0")
public class SecurityMediatorImplV1 extends SecurityMediatorImpl {

	private final RPackageService packageService;
	
	public SecurityMediatorImplV1(RepositoryMaintainerService repositoryMaintainerService,
			PackageMaintainerService packageMaintainerService, UserService userService, Environment environment,
			RoleService roleService, RPackageService packageService) {
		super(repositoryMaintainerService, packageMaintainerService, userService, environment, roleService);
		this.packageService = packageService;
	}
		
	public boolean isAuthorizedToAccept(SubmissionV1Dto submission, UserV1Dto user) {
		RPackage packageBag = packageService.findById(submission.getPackage().getId()).get();
		User userEntity = userService.findById(user.getId()).get();
		return isAuthorizedToEdit(packageBag, userEntity);
	}
	
	public boolean isAuthorizedToCancel(SubmissionV1Dto submission, UserV1Dto user) {
		if(submission.getUser().getId() == user.getId())
			return true;
		RPackage packageBag = packageService.findById(submission.getPackage().getId()).get();
		User userEntity = userService.findById(user.getId()).get();
		return isAuthorizedToEdit(packageBag, userEntity);
	}

}
