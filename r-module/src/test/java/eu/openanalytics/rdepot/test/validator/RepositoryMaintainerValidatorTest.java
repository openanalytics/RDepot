/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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

import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.service.CommonRepositoryService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RepositoryMaintainerTestFixture;

@ExtendWith(MockitoExtension.class)
public class RepositoryMaintainerValidatorTest {

	@Mock
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	UserService userService;
	
	@Mock
	CommonRepositoryService repositoryService;
	
	private RepositoryMaintainerValidator repositoryMaintainerValidator;
	
	@Test
	public void validateRepositoryMaintainer_NullUserAndNullRepository() throws Exception {
		repositoryMaintainerValidator = new RepositoryMaintainerValidator(userService, repositoryService, repositoryMaintainerService);
		
		RepositoryMaintainer repositoryMaintainer = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(null);
		repositoryMaintainer.setUser(null);
		
		DataBinder dataBinder = new DataBinder(repositoryMaintainer);
		dataBinder.setValidator(repositoryMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		
		repositoryMaintainerValidator.validate(repositoryMaintainer, errors);
		verify(errors, times(1)).rejectValue("user", RefactoredMessageCodes.EMPTY_USER);
		verify(errors, times(1)).rejectValue("repository", RefactoredMessageCodes.EMPTY_REPOSITORY);
		verify(errors, times(0)).rejectValue("repository", RefactoredMessageCodes.REPOSITORYMAINTAINER_DUPLICATE);
	}
	
	@Test
	public void validateRepositoryMaintainer_emptyUserAndEmptyRepository() throws Exception {
		repositoryMaintainerValidator = new RepositoryMaintainerValidator(userService, repositoryService, repositoryMaintainerService);
		
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		RepositoryMaintainer repositoryMaintainer = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(repository);
		
		DataBinder dataBinder = new DataBinder(repositoryMaintainer);
		dataBinder.setValidator(repositoryMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		
		when(userService.findById(repositoryMaintainer.getUser().getId()))
			.thenReturn(Optional.ofNullable(null));
		
		when(repositoryService.findById(repositoryMaintainer.getRepository().getId()))
			.thenReturn(Optional.ofNullable(null));
		
		repositoryMaintainerValidator.validate(repositoryMaintainer, errors);
		verify(errors, times(1)).rejectValue("user", RefactoredMessageCodes.EMPTY_USER);
		verify(errors, times(1)).rejectValue("repository", RefactoredMessageCodes.EMPTY_REPOSITORY);
		verify(errors, times(0)).rejectValue("repository", RefactoredMessageCodes.REPOSITORYMAINTAINER_DUPLICATE);
	}
	
	@Test
	public void validateRepositoryMaintainer_duplicateNewMaintainer() throws Exception {
		repositoryMaintainerValidator = new RepositoryMaintainerValidator(userService, repositoryService, repositoryMaintainerService);
		
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		RepositoryMaintainer repositoryMaintainer = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(repository);
		repositoryMaintainer.setId(0);
		
		DataBinder dataBinder = new DataBinder(repositoryMaintainer);
		dataBinder.setValidator(repositoryMaintainerValidator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		
		when(userService.findById(repositoryMaintainer.getUser().getId()))
			.thenReturn(Optional.ofNullable(repositoryMaintainer.getUser()));
		
		Mockito.doReturn(Optional.of(repository)).when(repositoryService).findById(repositoryMaintainer.getRepository().getId());
		
		when(repositoryMaintainerService
					.findByRepositoryAndUserAndDeleted(repositoryMaintainer.getRepository(), repositoryMaintainer.getUser(), false))
			.thenReturn(Optional.of(repositoryMaintainer));
		
		repositoryMaintainerValidator.validate(repositoryMaintainer, errors);
		verify(errors, times(0)).rejectValue("user", RefactoredMessageCodes.EMPTY_USER);
		verify(errors, times(0)).rejectValue("repository", RefactoredMessageCodes.EMPTY_REPOSITORY);
		verify(errors, times(1)).rejectValue("repository", RefactoredMessageCodes.REPOSITORYMAINTAINER_DUPLICATE);
	}	
}
