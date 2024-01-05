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
package eu.openanalytics.rdepot.base.mediator;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;

/**
 * Mediator used to choose the best maintainer for a {@link Package}.
 */
@Component
@Transactional
public class BestMaintainerChooser {
	
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final PackageMaintainerService packageMaintainerService;
	private final UserService userService;
	private final RoleService roleService;	
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public BestMaintainerChooser(RepositoryMaintainerService repositoryMaintainerService,
			PackageMaintainerService packageMaintainerService, UserService userService,
			RoleService roleService, CommonPackageService packageService) {
		super();
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.packageMaintainerService = packageMaintainerService;
		this.userService = userService;
		this.roleService = roleService;		
	}
	
	/**
	 * Finds the best package maintainer for a given package.
	 * @param packageBag
	 * @return
	 */	
	public User chooseBestPackageMaintainer(Package<?, ?> packageBag) throws NoSuitableMaintainerFound {
		final Optional<PackageMaintainer> packageMaintainer = 
				packageMaintainerService.findByPackageAndRepositoryAndNonDeleted(
						packageBag.getName(), packageBag.getRepository());
		
		if(packageMaintainer.isPresent()) {
			return packageMaintainer.get().getUser();
		}
		
		final List<RepositoryMaintainer> repositoryMaintainers = 
				repositoryMaintainerService.findByRepositoryNonDeleted(packageBag.getRepository());
		if(!repositoryMaintainers.isEmpty()) {
			return repositoryMaintainers.get(0).getUser();
		} else {
			try {
				return findFirstAdmin();
			} catch (AdminNotFound e) {
				logger.error(e.getMessage(), e);
			}
		}
		throw new NoSuitableMaintainerFound();
	}
	
	/**
	 * Updates all packages in the list with the most suitable maintainer.
	 * @param packages
	 * @throws NoSuitableMaintainerFound
	 */
	public void refreshMaintainerForPackages(List<Package<?,?>> packages) throws NoSuitableMaintainerFound {
		for(Package<?,?> packageBag : packages) {
			packageBag.setUser(chooseBestPackageMaintainer(packageBag));
		}	
	}
	
	/**
	 * Finds any administrator in the system.
	 * @return
	 * @throws AdminNotFound
	 */
	public User findFirstAdmin() throws AdminNotFound {
		final List<User> admins = findAllAdmins();
		
		if(admins.isEmpty())
			throw new AdminNotFound();
		
		return admins.get(0);
	}
	
	/**
	 * Finds all administrators in the systems.
	 * @return
	 */
	public List<User> findAllAdmins() {
		final Optional<Role> adminRole = roleService.findByValue(Role.VALUE.ADMIN);
		if(adminRole.isEmpty())
			return List.of();
		
		return userService.findByRole(adminRole.get());
	}
}
