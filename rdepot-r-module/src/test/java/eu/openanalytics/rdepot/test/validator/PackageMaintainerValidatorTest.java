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
package eu.openanalytics.rdepot.test.validator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@ExtendWith(MockitoExtension.class)
public class PackageMaintainerValidatorTest {

	@Mock
	PackageMaintainerService packageMaintainerService;
	
	@Mock
	UserService userService;
	
	@Mock
	RepositoryService<Repository> repositoryService;
	
	private PackageMaintainerValidator packageMaintainerValidator;
	
	@Test
	public void validatePackageMaintainer_nullPackageAndNullUserAndNullRepository() throws Exception {
		packageMaintainerValidator = new PackageMaintainerValidator(userService, repositoryService, packageMaintainerService);
		
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(null);
		packageMaintainer.setPackageName(null);
		packageMaintainer.setUser(null);
		
		DataBinder dataBinder = new DataBinder(packageMaintainer);
		dataBinder.setValidator(packageMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		
		packageMaintainerValidator.validate(packageMaintainer, errors);
		verify(errors, times(1)).rejectValue("packageName", MessageCodes.EMPTY_PACKAGE, null, null);
		verify(errors, times(1)).rejectValue("user", MessageCodes.EMPTY_USER);
		verify(errors, times(1)).rejectValue("repository", MessageCodes.REPOSITORY_NOT_FOUND);
	}
	
	@Test
	public void validatePackageMaintainer_emptyPackageAndEmptyUserAndEmptyRepository() throws Exception {
		packageMaintainerValidator = new PackageMaintainerValidator(userService, repositoryService, packageMaintainerService);

		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
		packageMaintainer.setPackageName("");
		
		DataBinder dataBinder = new DataBinder(packageMaintainer);
		dataBinder.setValidator(packageMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());

		when(userService.findById(packageMaintainer.getUser().getId()))
				.thenReturn(Optional.ofNullable(null));
		
		when(repositoryService.findById(packageMaintainer.getRepository().getId()))
			.thenReturn(Optional.ofNullable(null));		
		
		packageMaintainerValidator.validate(packageMaintainer, errors);
		verify(errors, times(1)).rejectValue("packageName", MessageCodes.EMPTY_PACKAGE, null, null);
		verify(errors, times(1)).rejectValue("user", MessageCodes.EMPTY_USER);
		verify(errors, times(1)).rejectValue("repository", MessageCodes.REPOSITORY_NOT_FOUND);
	}
	
	@Test
	public void validatePackageMaintainer_duplicateNewMaintainer() throws Exception {
		packageMaintainerValidator = new PackageMaintainerValidator(userService, repositoryService, packageMaintainerService);

		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
		packageMaintainer.setId(0);
		
		DataBinder dataBinder = new DataBinder(packageMaintainer);
		dataBinder.setValidator(packageMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());

		when(userService.findById(packageMaintainer.getUser().getId()))
				.thenReturn(Optional.ofNullable(packageMaintainer.getUser()));
		
		Mockito.doReturn(Optional.of(repository)).when(repositoryService).findById(packageMaintainer.getRepository().getId());
		
		when(packageMaintainerService.existsByUserAndPackageNameAndRepositoryAndNonDeleted(
				packageMaintainer.getUser(), packageMaintainer.getPackageName(), packageMaintainer.getRepository()))
		.thenReturn(true);
		
		packageMaintainerValidator.validate(packageMaintainer, errors);
		verify(errors, times(0)).rejectValue("packageName", MessageCodes.EMPTY_PACKAGE, null, null);
		verify(errors, times(0)).rejectValue("user", MessageCodes.EMPTY_USER);
		verify(errors, times(0)).rejectValue("repository", MessageCodes.REPOSITORY_NOT_FOUND);
		verify(errors, times(1)).rejectValue("packageName", MessageCodes.PACKAGE_ALREADY_MAINTAINED);
	}
	

	@Test
	public void validatePackageMaintainer_roleNotSufficient() throws Exception {
		packageMaintainerValidator = new PackageMaintainerValidator(userService, repositoryService, packageMaintainerService);

		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
		User user = UserTestFixture.GET_FIXTURE_USER();
		
		packageMaintainer.setUser(user);
		packageMaintainer.setId(0);
		
		DataBinder dataBinder = new DataBinder(packageMaintainer);
		dataBinder.setValidator(packageMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());

		when(userService.findById(packageMaintainer.getUser().getId()))
				.thenReturn(Optional.ofNullable(packageMaintainer.getUser()));
		
		Mockito.doReturn(Optional.of(repository)).when(repositoryService).findById(packageMaintainer.getRepository().getId());
		
		when(packageMaintainerService.existsByUserAndPackageNameAndRepositoryAndNonDeleted(
				packageMaintainer.getUser(), packageMaintainer.getPackageName(), packageMaintainer.getRepository()))
		.thenReturn(false);
		
		packageMaintainerValidator.validate(packageMaintainer, errors);
		verify(errors, times(0)).rejectValue("packageName", MessageCodes.EMPTY_PACKAGE, null, null);
		verify(errors, times(1)).rejectValue("user", MessageCodes.USER_PERMISSIONS_NOT_SUFFICIENT, null, null);
		verify(errors, times(0)).rejectValue("user", MessageCodes.EMPTY_USER);
		verify(errors, times(0)).rejectValue("repository", MessageCodes.REPOSITORY_NOT_FOUND);
		verify(errors, times(0)).rejectValue("packageName", MessageCodes.PACKAGE_ALREADY_MAINTAINED);
	}
}
