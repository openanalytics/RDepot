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
package eu.openanalytics.rdepot.test.unit.mediator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RoleTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;

public class BestMaintainerChooserTest extends UnitTest {
	
	@Mock
	PackageMaintainerService packageMaintainerService;
	
	@Mock
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	UserService userService;
	
	@Mock
	RoleService roleService;
	
	@InjectMocks
	BestMaintainerChooser bestMaintainerChooser;
	
	private static Role adminRole = RoleTestFixture.ROLE.ADMIN;
	
	@Test
	public void chooseBestMaintainer_throwsException_withNoSuitableUsers() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final User user = UserTestFixture.GET_FIXTURE_USER();
		final RPackage packageBag = RPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		
		when(packageMaintainerService.findAllByPackageAndRepositoryAndNonDeleted(packageBag.getName(), repository))
			.thenReturn(Collections.emptyList());
		when(repositoryMaintainerService.findByRepositoryNonDeleted(repository)).thenReturn(List.of());
		when(roleService.findByValue(adminRole.getValue())).thenReturn(Optional.of(adminRole));
		when(userService.findByRole(adminRole)).thenReturn(List.of());
		
		assertThrows(NoSuitableMaintainerFound.class, 
				() -> bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
	}
	
	@Test
	public void chooseBestMaintainer_whenThereIsAPackageMaintainerInTheGivenRepository() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final User user = UserTestFixture.GET_FIXTURE_USER();
		final User packageMaintainerUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		final RPackage packageBag = RPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		final PackageMaintainer packageMaintainer = PackageMaintainerTestFixture
				.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
		packageMaintainer.setUser(packageMaintainerUser);
		packageMaintainer.setPackageName(packageBag.getName());
		
		when(packageMaintainerService
				.findAllByPackageAndRepositoryAndNonDeleted(packageBag.getName(), repository))
			.thenReturn(List.of(packageMaintainer));
		
		assertEquals(
				packageMaintainerUser, 
				bestMaintainerChooser.chooseBestPackageMaintainer(packageBag), 
				"Incorrect package maintainer user returned."
		);
	}
	
	@Test
	public void chooseBestMaintainer_whenThereIsARepositoryMaintainerInAGivenRepositoryButNoPackageMaintainer() 
			throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final User user = UserTestFixture.GET_FIXTURE_USER();
		final User repositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		final RPackage packageBag = RPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		final RepositoryMaintainer repositoryMaintainer = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(repository);
		repositoryMaintainer.setUser(repositoryMaintainerUser);

		when(packageMaintainerService
				.findAllByPackageAndRepositoryAndNonDeleted(packageBag.getName(), repository))
			.thenReturn(Collections.emptyList());
		when(repositoryMaintainerService.findByRepositoryNonDeleted(repository))
			.thenReturn(List.of(repositoryMaintainer));
		assertEquals(
				repositoryMaintainerUser, 
				bestMaintainerChooser.chooseBestPackageMaintainer(packageBag), 
				"Incorrect repository maintainer user returned."
		);
	}
	
	@Test
	public void chooseBestMaintainer_whenThereIsNoRepositoryMaintaienrAndNoPackageMaintaienerInAGivenRepo() 
			throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN().get();
		final RPackage packageBag = RPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		when(packageMaintainerService
				.findAllByPackageAndRepositoryAndNonDeleted(packageBag.getName(), repository))
			.thenReturn(Collections.emptyList());
		when(repositoryMaintainerService.findByRepositoryNonDeleted(repository))
			.thenReturn(List.of());
		when(roleService.findByValue(adminRole.getValue())).thenReturn(Optional.of(adminRole));
		when(userService.findByRole(adminRole)).thenReturn(List.of(admin));
		
		assertEquals(
				admin, 
				bestMaintainerChooser.chooseBestPackageMaintainer(packageBag), 
				"Incorrect admin maintainer user returned."
		);
	}
	
}
