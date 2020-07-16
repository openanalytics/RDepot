/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import eu.openanalytics.rdepot.controller.RepositoryMaintainerController;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.CreateRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.model.EditRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryMaintainerControllerTest {
	
	@InjectMocks
	RepositoryMaintainerController repositoryMaintainerController;
	
	@Mock
	RepositoryService repositoryService;
	
	@Mock
	UserService userService;
	
	@Mock
	RoleService roleService;
	
	@Mock
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	RepositoryMaintainerValidator repositoryMaintainerValidator;

	@Mock
	MessageSource messageSource;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Locale mockLocale = Locale.ENGLISH;
		LocaleContext localeContext = Mockito.mock(LocaleContext.class);
		Mockito.when(localeContext.getLocale()).thenReturn(mockLocale);
		LocaleContextHolder.setLocaleContext(localeContext);

		Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.isNull(), Mockito.any())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				return invocation.getArgument(0);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRepositoryMaintainersPage() {
		List<RepositoryMaintainer> repositoryMaintainers = new ArrayList<>();
		User einstein = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		User tesla = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		RepositoryMaintainer maintainer1 = new RepositoryMaintainer(1, einstein, repository1, false);
		RepositoryMaintainer maintainer2 = new RepositoryMaintainer(2, tesla, repository2, false);
		repositoryMaintainers.add(maintainer1);
		repositoryMaintainers.add(maintainer2);
		
		Mockito.when(repositoryMaintainerService.findAll()).thenReturn(repositoryMaintainers);
		Model model = new ExtendedModelMap();
		repositoryMaintainerController.repositoryMaintainersPage(model);
		
		assertEquals(repositoryMaintainers, (List<RepositoryMaintainer>)model.asMap().get("repositorymaintainers"));
		assertEquals(9, (int)model.asMap().get("role"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testNewRepositoryMaintainerDialog() {	
		User einstein = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		User tesla = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(tesla);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.newRepositoryMaintainerDialog();
		
		assertTrue(result.get("repositorymaintainer") instanceof RepositoryMaintainer);
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<Repository>)result.get("repositories"));
		assertEquals(9, result.get("role"));
	}
	
	@Test
	public void testCreateNewRepositoryMaintainerWhenCurrentRoleIsRepositoryMaintainer() throws RepositoryMaintainerCreateException {
		CreateRepositoryMaintainerRequestBody requestBody = new CreateRepositoryMaintainerRequestBody(123, 234);
		
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Mockito.when(userService.findById(requestBody.getUserId())).thenReturn(user);
		
		Repository repository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(repository);
		
		User requester = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
		
		Mockito.when(repositoryMaintainerService.create(Mockito.any(), Mockito.any())).thenReturn(null);
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(requestBody, mockPrincipal);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED, (String)result.get("success"));
	}
	
//	@Test
//	public void testCreateNewRepositoryMaintainerWhenCurrentRoleIsUser() throws RepositoryMaintainerCreateException, UserEditException {
//		CreateRepositoryMaintainerRequestBody requestBody = new CreateRepositoryMaintainerRequestBody(123, 234);
//		
//		User user = new User(234, new Role(2, 0, "user", "User"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
//		Mockito.when(userService.findById(requestBody.getUserId())).thenReturn(user);
//		
//		Repository repository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
//		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(repository);
//		
//		User requester = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
//		Principal mockPrincipal = Mockito.mock(Principal.class);
//		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
//		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
//		
//		Mockito.when(repositoryMaintainerService.create(Mockito.any(), Mockito.any())).thenReturn(null);
//		
//		Role repositoryMaintainerRole = new Role(12, 2, "repositorymaintainer", "RepositoryMaintainer");
//		Mockito.when(roleService.findByName("repositorymaintainer")).thenReturn(repositoryMaintainerRole);
//		Mockito.when(userService.update(Mockito.any(), Mockito.any())).thenReturn(null);
//		
//		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(requestBody, mockPrincipal);
//		
//		assertEquals("repositorymaintainer", user.getRole().getName());
//		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED, (String)result.get("success"));
//	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateNewRepositoryMaintainerWhenRequesterIsNull() throws RepositoryMaintainerCreateException {
		CreateRepositoryMaintainerRequestBody requestBody = new CreateRepositoryMaintainerRequestBody(123, 234);
		
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Mockito.when(userService.findById(requestBody.getUserId())).thenReturn(user);
		
		Repository repository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(repository);
		
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(null);
		
		User einstein = new User(456, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(user);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(requestBody, mockPrincipal);
		
		assertEquals(user, ((RepositoryMaintainer)result.get("repositorymaintainer")).getUser());
		//test validation?
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, (String)result.get("error"));
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<User>)result.get("repositories"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateNewRepositoryMaintainerWhenRequesterIsNotAuthorized() throws RepositoryMaintainerCreateException {
		CreateRepositoryMaintainerRequestBody requestBody = new CreateRepositoryMaintainerRequestBody(123, 234);
		
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Mockito.when(userService.findById(requestBody.getUserId())).thenReturn(user);
		
		Repository repository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(repository);
		
		User requester = new User(123, new Role(1, 1, "packagemaintainer", "Package Maintainer"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
		
		User einstein = new User(456, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(user);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(requestBody, mockPrincipal);
		
		assertEquals(user, ((RepositoryMaintainer)result.get("repositorymaintainer")).getUser());
		//test validation?
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, (String)result.get("error"));
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<User>)result.get("repositories"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateNewRepositoryMaintainerWhenUserIsNotCapable() throws RepositoryMaintainerCreateException {
		CreateRepositoryMaintainerRequestBody requestBody = new CreateRepositoryMaintainerRequestBody(123, 234);
		
		User user = new User(234, new Role(2, 1, "packagemaintainer", "Package Maintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Mockito.when(userService.findById(requestBody.getUserId())).thenReturn(user);
		
		Repository repository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(repository);
		
		User requester = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
		
		User einstein = new User(456, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(user);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(requestBody, mockPrincipal);
		
		assertEquals(user, ((RepositoryMaintainer)result.get("repositorymaintainer")).getUser());
		//test validation?
		assertEquals(MessageCodes.ERROR_USER_NOT_CAPABLE, (String)result.get("error"));
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<User>)result.get("repositories"));
	}
	
	@Test
	public void testUpdateRepositoryMaintainer() throws RepositoryMaintainerEditException {
		Repository newRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		EditRepositoryMaintainerRequestBody requestBody = new EditRepositoryMaintainerRequestBody(123);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(newRepository);
		
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Repository oldRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		RepositoryMaintainer maintainer = new RepositoryMaintainer(234, user, oldRepository, false);
		Mockito.when(repositoryMaintainerService.findById(234)).thenReturn(maintainer);
		
		User requester = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
		
		Mockito.when(repositoryMaintainerService.update(Mockito.any(), Mockito.any())).thenReturn(null);
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(requestBody, 234, mockPrincipal);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, (String)result.get("success"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateRepositoryMaintainerWhenRepositoryMaintainerIsNotFound() throws RepositoryMaintainerEditException {
		Repository newRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		EditRepositoryMaintainerRequestBody requestBody = new EditRepositoryMaintainerRequestBody(123);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(newRepository);
		
		RepositoryMaintainer maintainer = new RepositoryMaintainer();
		Mockito.when(repositoryMaintainerService.findById(234)).thenReturn(maintainer);
		
		User requester = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
				
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		User einstein = new User(456, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(user);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(requestBody, 234, mockPrincipal);

		assertEquals(MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND, (String)result.get("error"));
		assertEquals(maintainer, (RepositoryMaintainer)result.get("repositorymaintainer"));
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<User>)result.get("repositories"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateRepositoryMaintainerWhenRequesterIsNull() throws RepositoryMaintainerEditException {
		Repository newRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		EditRepositoryMaintainerRequestBody requestBody = new EditRepositoryMaintainerRequestBody(123);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(newRepository);
		
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Repository oldRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		RepositoryMaintainer maintainer = new RepositoryMaintainer(234, user, oldRepository, false);
		Mockito.when(repositoryMaintainerService.findById(234)).thenReturn(maintainer);
		
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(null);
				
		User einstein = new User(456, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(user);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(requestBody, 234, mockPrincipal);

		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, (String)result.get("error"));
		assertEquals(maintainer, (RepositoryMaintainer)result.get("repositorymaintainer"));
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<User>)result.get("repositories"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateRepositoryMaintainerWhenRequesterIsNotAuthorized() throws RepositoryMaintainerEditException {
		Repository newRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		EditRepositoryMaintainerRequestBody requestBody = new EditRepositoryMaintainerRequestBody(123);
		Mockito.when(repositoryService.findById(requestBody.getRepositoryId())).thenReturn(newRepository);
		
		User user = new User(234, new Role(2, 2, "repositorymaintainer", "RepositoryMaintainer"), "Nicola Tesla", "tesla@example.org", "tesla", true, false);
		Repository oldRepository = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		RepositoryMaintainer maintainer = new RepositoryMaintainer(234, user, oldRepository, false);
		Mockito.when(repositoryMaintainerService.findById(234)).thenReturn(maintainer);
		
		User requester = new User(123, new Role(1, 1, "packagemaintainer", "Package Maintainer"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
				
		User einstein = new User(456, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		List<User> users = new ArrayList<>();
		users.add(einstein);
		users.add(user);
		Mockito.when(userService.findEligibleRepositoryMaintainers()).thenReturn(users);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "127.0.0.1", true, false);
		Repository repository2 = new Repository(234, "someotheruri", "repo2", "127.0.0.2", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(requestBody, 234, mockPrincipal);

		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, (String)result.get("error"));
		assertEquals(maintainer, (RepositoryMaintainer)result.get("repositorymaintainer"));
		assertEquals(users, (List<User>)result.get("users"));
		assertEquals(repositories, (List<User>)result.get("repositories"));
	}
	
	@Test
	public void testDeleteRepositoryMaintainer() throws RepositoryMaintainerDeleteException {
		User requester = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
		
		Mockito.when(repositoryMaintainerService.delete(Mockito.eq(123), Mockito.any())).thenReturn(null);
		
		HashMap<String, String> result = repositoryMaintainerController.deleteRepositoryMaintainer(123, mockPrincipal);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, result.get("success"));
	}
	
	@Test
	public void testDeleteRepositoryMaintainerWhenRequesterIsNull() throws RepositoryMaintainerDeleteException {
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(null);
		
		HashMap<String, String> result = repositoryMaintainerController.deleteRepositoryMaintainer(123, mockPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void testDeleteRepositoryMaintainerWhenRequesterIsNotAuthorized() throws RepositoryMaintainerDeleteException {
		User requester = new User(123, new Role(1, 1, "packagemaintainer", "Package Maintainer"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(requester);
		
		HashMap<String, String> result = repositoryMaintainerController.deleteRepositoryMaintainer(123, mockPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void testShiftDeleteRepositoryMaintainer() throws RepositoryMaintainerNotFound {
		Mockito.when(repositoryMaintainerService.shiftDelete(123)).thenReturn(null);
		
		HashMap<String, String> result = repositoryMaintainerController.shiftDeleteRepositoryMaintainer(123);
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, result.get("success"));
	}
	
	@Test
	public void testShiftDeleteRepositoryMaintainerWhenRepositoryMaintainerIsNotFound() throws RepositoryMaintainerNotFound {
		RepositoryMaintainerNotFound exception = new RepositoryMaintainerNotFound();
		Mockito.when(repositoryMaintainerService.shiftDelete(123)).thenThrow(exception);
		
		HashMap<String, String> result = repositoryMaintainerController.shiftDeleteRepositoryMaintainer(123);
		assertEquals(MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND, result.get("error"));
	}
}
