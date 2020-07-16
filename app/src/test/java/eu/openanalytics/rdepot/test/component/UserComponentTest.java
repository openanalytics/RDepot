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
package eu.openanalytics.rdepot.test.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.io.File;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.spring.integration.test.annotation.SpringAnnotationConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import eu.openanalytics.rdepot.controller.UserController;
import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.RepositoryStorageException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;
import eu.openanalytics.rdepot.repository.EventRepository;
import eu.openanalytics.rdepot.repository.PackageEventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.repository.RepositoryEventRepository;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.UserEventRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.test.config.MockRepositoryBeansConfig;
import eu.openanalytics.rdepot.test.config.TestPrincipal;
import eu.openanalytics.rdepot.test.config.WebApplicationTestConfig;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RoleTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, UserComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class UserComponentTest extends BaseComponentTest {
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	PackageEventRepository packageEventRepository;
	
	@Autowired
	PackageMaintainerRepository packageMaintainerRepository;
	
	@Autowired
	PackageRepository packageRepository;
	
	@Autowired
	RepositoryEventRepository repositoryEventRepository;
	
	@Autowired
	RepositoryMaintainerRepository repositoryMaintainerRepository;
	
	@Autowired
	RepositoryRepository repositoryRepository;

	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	UserEventRepository userEventRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PackageMaintainerService packageMaintainerService;
	
	@Autowired
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Autowired
	RepositoryStorage repositoryStorage;
	
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
	
	@Autowired
	private UserController userController;
	
	private int placeholder = 9;
	
	@Test
	public void shouldUserControllerBeNotNull() {
		assertNotNull(userController);
	}
	
	@Test
	public void shouldReturnUsersPage() { //line 86
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(1, 3, 2);
		
		when(userRepository.findByDeleted(eq(false), any())).thenReturn(testUsers);
		
		Model testModel = new ExtendedModelMap();
		
		String users = userController.usersPage(testModel);
		
		assertEquals(testUsers, testModel.asMap().get("users"));
		assertEquals(placeholder, testModel.asMap().get("role"));
		assertEquals("users", users);
	}
	
	@Test
	public void shouldReturnUsers() { //line 96
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(3, 2, 1);
		
		when(userRepository.findByDeleted(eq(false), any())).thenReturn(testUsers);
		
		List<User> users = userController.users();
		
		assertEquals(testUsers, users);
	}
	
	@Test
	public void shouldReturnRoles() { //line 104
		List<Role> testRoles = RoleTestFixture.GET_FIXTURE_ROLES(1, 3, 2, 5);
		
		when(roleRepository.findAll()).thenReturn(testRoles);
		
		List<Role> roles = userController.getRoles();
		
		assertEquals(testRoles, roles);
	}
	
	@Test
	public void shouldNotReturnUserDetailsWithNullRequester() { //line 111
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		String login = "user";
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(login, false)).thenReturn(testAdmin);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = userController.userDetails(login, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotReturnUserDetailsWithNullUser() { //line 111
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		String login = "user";
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(login, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = userController.userDetails(login, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotReturnUserDetailsWithRepositorymaintainerCredentialsAndDifferentLogin() { //line 111
		String login = "user";
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(login, false)).thenReturn(testRepositoryMaintainer);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		HashMap<String, Object> result = userController.userDetails(login, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldReturnUserDetailsWithRepositorymaintainerCredentialsAndSameLogin() { //line 111
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		String login = testRepositoryMaintainer.getLogin();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(login, false)).thenReturn(testRepositoryMaintainer);
		
		UserEvent testEvent1 = new UserEvent();
		testEvent1.setId(1);
		when(userEventRepository.findByUserAndEvent_Value(testRepositoryMaintainer, "create")).thenReturn(testEvent1);
		UserEvent testEvent2 = new UserEvent();
		testEvent2.setId(2);
		when(userEventRepository.findLastByUserAndChangedVariable(testRepositoryMaintainer, "last logged in")).thenReturn(testEvent2);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		HashMap<String, Object> result = userController.userDetails(login, testPrincipal); 
		
		assertEquals(testEvent1, result.get("created"));
		assertEquals(testEvent2, result.get("lastloggedin"));
		assertEquals(testRepositoryMaintainer, result.get("user"));
	}

	@Test
	public void shouldReturnUserDetailsWithAdminCredentials() { //line 111
		String login = "admin";
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(login, false)).thenReturn(testAdmin);
		
		UserEvent testEvent1 = new UserEvent();
		testEvent1.setId(1);
		when(userEventRepository.findByUserAndEvent_Value(testAdmin, "create")).thenReturn(testEvent1);
		UserEvent testEvent2 = new UserEvent();
		testEvent2.setId(2);
		when(userEventRepository.findLastByUserAndChangedVariable(testAdmin, "last logged in")).thenReturn(testEvent2);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = userController.userDetails(login, testPrincipal); 
		
		assertEquals(testEvent1, result.get("created"));
		assertEquals(testEvent2, result.get("lastloggedin"));
		assertEquals(testAdmin, result.get("user"));
	}
	
	@Test
	public void shouldNotEditUserWithNullRequester() { //line 135
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testAdmin.getId();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> result = userController.editUser(id, testAdmin, bindingResult, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldNotEditUserWithNonAdminCredentials() { //line 135
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		int id = testUser.getId();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> result = userController.editUser(id, testUser, bindingResult, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldNotEditUserWithDifferentId() { //line 135
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		int id = 135;
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> result = userController.editUser(id, testAdmin, bindingResult, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotEditUserWithAdminCredentialsAndBindingErrors() { //line 135
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testUser.getId();
		testUser.setEmail("testestest");
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		List<Role> testRoles = RoleTestFixture.GET_FIXTURE_ROLES(1, 2, 3, 4);
		
		when(roleRepository.findAll()).thenReturn(testRoles);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testUser, "admin");
		
		HashMap<String, Object> result = userController.editUser(id, testUser, bindingResult, testPrincipal);
		
		assertEquals(testUser, result.get("user"));
		assertEquals(MessageCodes.ERROR_FORM_INVALID_EMAIL, result.get("error"));
		assertEquals(testRoles, result.get("roles"));
		assertEquals(bindingResult, result.get("org.springframework.validation.BindingResult.user"));
	}
	
	@Test
	public void shouldEditUserWithAdminAndTestUpdatedUserAdmin() { //line 135
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testUser.getId();
		testUser.setActive(false);
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUpdatedUser.setActive(true);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		
		Event testEvent = new Event();
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testUpdatedUser);	
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester).thenReturn(null);

		when(userRepository.findByEmailAndDeleted(testUser.getEmail(), false)).thenReturn(null);	
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testUser, "admin");
		
		HashMap<String, Object> result = userController.editUser(id, testUser, bindingResult, testPrincipal);
		
		assertFalse(testUserEvents.isEmpty());
		assertNull(result.get("error"));
	}
	
	@Test
	public void shouldEditUserWithAdminAndDifferentUpdatedUserUser() { //line 135 
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testUser.getId();
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUpdatedUser.setActive(true);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		
		Event testEvent = new Event();
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testUpdatedUser);
		when(userRepository.findByEmailAndDeleted(testUser.getEmail(), false)).thenReturn(null);	
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester).thenReturn(null);;
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testUser, "admin");
		
		HashMap<String, Object> result = userController.editUser(id, testUser, bindingResult, testPrincipal);
		
		assertFalse(testUserEvents.isEmpty());
		assertNull(result.get("error"));
	}
	
	@Test
	public void shouldEditUserWithAdminAndDifferentUpdatedUserRepositoryMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 135
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testUser.getId();
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		testUpdatedUser.setActive(true);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		

		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
		
		testUpdatedUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testUpdatedUser);	
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester).thenReturn(null);

		when(userRepository.findByEmailAndDeleted(testUser.getEmail(), false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testDeletedRepositoryMaintainer, testOldRepositoryMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testUser, "admin");
		
		HashMap<String, Object> result = userController.editUser(id, testUser, bindingResult, testPrincipal);
		
		assertFalse(testPackageEvents.isEmpty());
		assertFalse(testRepositoryEvents.isEmpty());
		assertFalse(testUserEvents.isEmpty());
		assertNull(result.get("error"));
	}
	
	@Test
	public void shouldEditUserWithAdminAndDifferentUpdatedUserPackageMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 135
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		testUser.setActive(true);
		testUser.setId(0);
		
		testUpdatedUser.setId(1);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(2);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		User testRepositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		testRepositoryMaintainerUser.setId(123);
		
		List<Repository> testRepositories = new ArrayList<>();
		testRepositories.add(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRepositoryMaintainerUser, testRepositories);
		testRepositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		testRepository.setPackages(new HashSet<Package> (testPackages));
		
		List<PackageMaintainer> testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testUser, testRepository, 1);
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer> (testPackageMaintainer));
		
		PackageEvent testPackageEvent = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUpdatedUser, testPackages.get(0), 1, 1).get(0);
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		List<PackageEvent> expectedPackageEvents = new ArrayList<>();
		expectedPackageEvents.add(testPackageEvent);
		
		RepositoryEvent testRepositoryEvent = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testUpdatedUser, testRepository, 1).get(0);
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		//TODO: PackageMaintainerEvent
		
		List<RepositoryEvent> expectedRepositoryEvents = new ArrayList<>();
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester).thenReturn(null);
		
		when(userRepository.findByEmailAndDeleted(testUser.getEmail(), false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		
		when(eventRepository.findByValue("delete")).thenReturn(deleteEvent);
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.get(0).getId(), false)).thenReturn(testPackageMaintainer.get(0));
		
		when(repositoryMaintainerRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testRepositoryMaintainers);
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				testPackageEvents.add(testPackageEvent);
				return testPackageEvent;
			}
		});
		
		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				testRepositoryEvents.add(testRepositoryEvent);
				return testRepositoryEvent;
			}
		});
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(testPackages), any(), eq(testFile), eq(testRepository));
	
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testUser, "admin");
		
		HashMap<String, Object> result = userController.editUser(testUser.getId(), testUser, bindingResult, testPrincipal);
		
		assertEquals(expectedPackageEvents, testPackageEvents);
		assertEquals(expectedRepositoryEvents, testRepositoryEvents);
		assertNull(result.get("error"));
	}
	
	@Test
	public void shouldEditUserWithAdminAndDifferentUpdatedUserAdmin() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 135
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(0);
		testUser.setActive(true);
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		Set<User> testAdmins = new HashSet<>();
		for(int i = 0; i < 3; i++) {
			User tempAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
			tempAdmin.setId(i+10);
			testAdmins.add(tempAdmin);
		}
			

		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		testRole.setUsers(testAdmins);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		User testRepositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<Repository> testRepositories = new ArrayList<>();
		testRepositories.add(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRepositoryMaintainerUser, testRepositories);
		testRepositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		testRepository.setPackages(new HashSet<Package> (testPackages));
		
		List<PackageMaintainer> testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testUser, testRepository, 1);
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer> (testPackageMaintainer));
		
		PackageEvent testPackageEvent = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUpdatedUser, testPackages.get(0), 1, 1).get(0);
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		List<PackageEvent> expectedPackageEvents = new ArrayList<>();
		expectedPackageEvents.add(testPackageEvent);
		
		RepositoryEvent testRepositoryEvent = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testUpdatedUser, testRepository, 1).get(0);
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		//TODO: PackageMaintainerEvent
		
		List<RepositoryEvent> expectedRepositoryEvents = new ArrayList<>();
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester).thenReturn(null);
		
		when(userRepository.findByEmailAndDeleted(testUser.getEmail(), false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		
		when(eventRepository.findByValue("delete")).thenReturn(deleteEvent);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(eq(testPackages.get(0).getName()), eq(testRepository), eq(false))).thenReturn(null);
		
		when(userRepository.findByRoleAndDeleted(testRole, false)).thenReturn(new ArrayList<User>(testAdmins));
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				testPackageEvents.add(testPackageEvent);
				return testPackageEvent;
			}
		});
		
		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				testRepositoryEvents.add(testRepositoryEvent);
				return testRepositoryEvent;
			}
		});
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(testPackages), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testUser, "admin");
		
		HashMap<String, Object> result = userController.editUser(testUser.getId(), testUser, bindingResult, testPrincipal);
		
		assertEquals(expectedPackageEvents, testPackageEvents);
		assertEquals(expectedRepositoryEvents, testRepositoryEvents);
		assertNull(result.get("error"));
	}

	@Test
	public void shouldReturnActivatedUserWithNullRequester() { //line 170
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testAdmin.getId();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testAdmin);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotAtivatUserWithNonAdmninCredentials() { //170
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		int id = testRepositoryMaintainer.getId();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testRepositoryMaintainer);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldNotActivatUserWithNullUser() { //line 170
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testAdmin.getId();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(null);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldReturnWarningWhenActivatingUserAndAdminIsActive() { //line 170
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(true);
		int id1 = testUser.getId();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
		
		assertEquals(MessageCodes.WARNING_USER_ALREADY_ACTIVATED, result.get("warning"));
	}
	
	@Test
	public void shouldActivatUserWithAdminAndUpdatedUserAdmin() { //line 170
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(false);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUpdatedUser.setActive(false);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		Event testEvent = new Event();
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
 	
		assertFalse(testUserEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_USER_ACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldNotActivateUserWithAdminAndNullEvent() { //line 170
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(false);
		int id1 = testUser.getId();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(null);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
 	
		assertEquals(MessageCodes.ERROR_EVENT_NOT_FOUND, result.get("error"));
	}

	@Test
	public void shouldActivateUserWithAdminAndDifferentUpdatedUserUser() { //line 170
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUpdatedUser.setActive(false);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		Event testEvent = new Event();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
		
		assertFalse(testUserEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_USER_ACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldActivateUserWithAdminAndDifferentUpdatedUserRepositoryMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 170
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(1, 0, 0, 0).get(0);
		
		testUser.setActive(false);
		testUser.setId(1);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(2);
		
		List<User> testAdmins = new ArrayList<>();
		testAdmins.add(testRequester);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();	
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		testRepository.setPackages(new HashSet<Package> (testPackages));
		
		List<Repository> testRepositories = new ArrayList<>();
		testRepositories.add(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		testUpdatedUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		PackageEvent testPackageEvent = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUpdatedUser, testPackages.get(0), 1, 1).get(0);
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		List<PackageEvent> expectedPackageEvents = new ArrayList<>();
		expectedPackageEvents.add(testPackageEvent);
		
		RepositoryEvent testRepositoryEvent = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testUpdatedUser, testRepository, 1).get(0);
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		//TODO: RepositoryMaintainerEvent
		
		List<RepositoryEvent> expectedRepositoryEvents = new ArrayList<>();
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testUser, testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainers.get(0).getId(), false)).thenReturn(testRepositoryMaintainers.get(0));
		
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		when(userRepository.findByRoleAndDeleted(testRole, false)).thenReturn(testAdmins);
		
		when(eventRepository.findByValue("delete")).thenReturn(deleteEvent);
		
		when(packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(eq(testPackages.get(0).getName()), eq(testRepository), eq(false))).thenReturn(null);
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				testPackageEvents.add(testPackageEvent);
				return testPackageEvent;
			}
		});
		
		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				testRepositoryEvents.add(testRepositoryEvent);
				return testRepositoryEvent;
			}
		});
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(testPackages), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(testUser.getId(), testPrincipal); 
		
		assertEquals(expectedPackageEvents, testPackageEvents);
		assertEquals(expectedRepositoryEvents, testRepositoryEvents);
		assertEquals(MessageCodes.SUCCESS_USER_ACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldActivatUserWithAdminAndDifferentUpdatedUserPackageMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 170
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USERS(0, 1, 0).get(0);
		testUpdatedUser.setActive(false);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer.setPackage(testPackage.getName());
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
			
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), false)).thenReturn(testPackageMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackage.getName(), testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
		
		assertFalse(testPackageEvents.isEmpty());
		assertFalse(testRepositoryEvents.isEmpty());
		assertFalse(testUserEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_USER_ACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldActivateUserWithUserAndDifferentUpdatedUserAdmin() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 170
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(0);
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUpdatedUser.setActive(false);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		User testAdmin1 = UserTestFixture.GET_FIXTURE_ADMIN();
		User testAdmin2 = UserTestFixture.GET_FIXTURE_ADMIN();
		User testAdmin3 = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Set<User> testUsers = new HashSet<>();
		testUsers.add(testAdmin1);
		testUsers.add(testAdmin2);
		testUsers.add(testAdmin3);
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		
		testRole.setUsers(testUsers);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer.setPackage(testPackage.getName());
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
			
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);
	
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
	
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
	
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {
	
			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
	
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), false)).thenReturn(testPackageMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackage.getName(), testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
		
		assertFalse(testPackageEvents.isEmpty());
		assertFalse(testRepositoryEvents.isEmpty());
		assertFalse(testUserEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_USER_ACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldNotActivateUserWithUserAndDifferentUpdatedUserAdminAndNullRole() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 170
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(0);
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUpdatedUser.setActive(false);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		User testAdmin1 = UserTestFixture.GET_FIXTURE_ADMIN();
		User testAdmin2 = UserTestFixture.GET_FIXTURE_ADMIN();
		User testAdmin3 = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Set<User> testUsers = new HashSet<>();
		testUsers.add(testAdmin1);
		testUsers.add(testAdmin2);
		testUsers.add(testAdmin3);
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		
		testRole.setUsers(testUsers);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer.setPackage(testPackage.getName());
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
			
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(roleRepository.findByName("admin")).thenReturn(null);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), false)).thenReturn(testPackageMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackage.getName(), testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));

		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
		
		assertTrue(testPackageEvents.isEmpty());
		assertTrue(testRepositoryEvents.isEmpty());
		assertTrue(testUserEvents.isEmpty());
		assertEquals(MessageCodes.ERROR_ADMIN_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotActivateUserWithUserAndDifferentUpdatedUserAdminAndNullOneAdminLeft() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 170
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(0);
		testUser.setActive(false);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUpdatedUser.setActive(false);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		User testAdmin1 = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Set<User> testUsers = new HashSet<>();
		testUsers.add(testAdmin1);
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		
		testRole.setUsers(testUsers);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer.setPackage(testPackage.getName());
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
			
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), false)).thenReturn(testPackageMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackage.getName(), testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));

		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.activateUser(id1, testPrincipal); 
		
		assertTrue(testPackageEvents.isEmpty());
		assertTrue(testRepositoryEvents.isEmpty());
		assertTrue(testUserEvents.isEmpty());
		assertEquals(MessageCodes.ERROR_ADMIN_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotDeactivateUserWithNullRequester() { //line 204
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testAdmin.getId();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testAdmin);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotDeactivateUserWithNonAdmninCredentials() { //line 204
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		int id = testRepositoryMaintainer.getId();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(testRepositoryMaintainer);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldReturnDeactivatedUserWithNullUser() { //line 204
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		int id = testAdmin.getId();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(null);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id, testPrincipal); 
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldReturnWarningWhenDeactivatingUserAndAdminIsNotActive() { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(false);
		int id1 = testUser.getId();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(false);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
		
		assertEquals(MessageCodes.WARNING_USER_ALREADY_DEACTIVATED, result.get("warning"));
	}
	
	@Test
	public void shouldDeactivateUserWithAdminAndUpdatedUserAdmin() { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(true);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUpdatedUser.setActive(true);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		Event testEvent = new Event();
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
 	
		assertFalse(testUserEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_USER_DEACTIVATED, result.get("success"));
	}
		
	@Test
	public void shouldNotDeactivateUserWithAdminAndNullUpdatedUser() { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(true);
		int id1 = testUser.getId();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		Event testEvent = new Event();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser).thenReturn(null);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
 	
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotDeactivateUserWithAdminAndNullEvent() { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(true);
		int id1 = testUser.getId();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(null);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
 	
		assertEquals(MessageCodes.ERROR_EVENT_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldDeactivateUserWithAdminAndDifferentUpdatedUserUser() { //line 204
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(true);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		Event testEvent = new Event();
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);		
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
		
		assertFalse(testUserEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_USER_DEACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldDeactivateUserWithAdminAndDifferentUpdatedUserRepositoryMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(1, 0, 0, 0).get(0);
		
		testUser.setActive(true);
		testUser.setId(1);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(2);
		
		List<User> testAdmins = new ArrayList<>();
		testAdmins.add(testRequester);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();	
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		testRepository.setPackages(new HashSet<Package> (testPackages));
		
		List<Repository> testRepositories = new ArrayList<>();
		testRepositories.add(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		testUpdatedUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		PackageEvent testPackageEvent = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUpdatedUser, testPackages.get(0), 1, 1).get(0);
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		List<PackageEvent> expectedPackageEvents = new ArrayList<>();
		expectedPackageEvents.add(testPackageEvent);
		
		RepositoryEvent testRepositoryEvent = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testUpdatedUser, testRepository, 1).get(0);
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		//TODO: RepositoryMaintainerEvent
		
		List<RepositoryEvent> expectedRepositoryEvents = new ArrayList<>();
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testUser, testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainers.get(0).getId(), false)).thenReturn(testRepositoryMaintainers.get(0));
		
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		when(userRepository.findByRoleAndDeleted(testRole, false)).thenReturn(testAdmins);
		
		when(eventRepository.findByValue("delete")).thenReturn(deleteEvent);
		
		when(packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(eq(testPackages.get(0).getName()), eq(testRepository), eq(false))).thenReturn(null);
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				testPackageEvents.add(testPackageEvent);
				return testPackageEvent;
			}
		});
		
		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				testRepositoryEvents.add(testRepositoryEvent);
				return testRepositoryEvent;
			}
		});
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(testPackages), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(testUser.getId(), testPrincipal); 
		
		assertEquals(expectedPackageEvents, testPackageEvents);
		assertEquals(expectedRepositoryEvents, testRepositoryEvents);
		assertEquals(MessageCodes.SUCCESS_USER_DEACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldDeactivateUserWithAdminAndDifferentUpdatedUserPackageMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		testUser.setActive(true);
		testUser.setId(1);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(2);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		User testRepositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<Repository> testRepositories = new ArrayList<>();
		testRepositories.add(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRepositoryMaintainerUser, testRepositories);
		testRepositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		testRepository.setPackages(new HashSet<Package> (testPackages));
		
		List<PackageMaintainer> testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testUser, testRepository, 1);
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer> (testPackageMaintainer));
		
		PackageEvent testPackageEvent = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUpdatedUser, testPackages.get(0), 1, 1).get(0);
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		List<PackageEvent> expectedPackageEvents = new ArrayList<>();
		expectedPackageEvents.add(testPackageEvent);
		
		RepositoryEvent testRepositoryEvent = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testUpdatedUser, testRepository, 1).get(0);
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		//TODO: PackageMaintainerEvent
		
		List<RepositoryEvent> expectedRepositoryEvents = new ArrayList<>();
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testUser, testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		
		when(eventRepository.findByValue("delete")).thenReturn(deleteEvent);
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.get(0).getId(), false)).thenReturn(testPackageMaintainer.get(0));
		
		when(repositoryMaintainerRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testRepositoryMaintainers);
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				testPackageEvents.add(testPackageEvent);
				return testPackageEvent;
			}
		});
		
		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				testRepositoryEvents.add(testRepositoryEvent);
				return testRepositoryEvent;
			}
		});
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(testPackages), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(testUser.getId(), testPrincipal); 
		
		assertEquals(expectedPackageEvents, testPackageEvents);
		assertEquals(expectedRepositoryEvents, testRepositoryEvents);
		assertEquals(MessageCodes.SUCCESS_USER_DEACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldDeactivateUserWithUserAndDifferentUpdatedUserAdmin() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 204
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(13);
		testUser.setActive(true);
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		Set<User> testAdmins = new HashSet<>();
		for(int i = 0; i < 3; i++) 
			testAdmins.add(UserTestFixture.GET_FIXTURE_ADMIN());

		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		testRole.setUsers(testAdmins);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		User testRepositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<Repository> testRepositories = new ArrayList<>();
		testRepositories.add(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRepositoryMaintainerUser, testRepositories);
		testRepositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		testRepository.setPackages(new HashSet<Package> (testPackages));
		
		List<PackageMaintainer> testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testUser, testRepository, 1);
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer> (testPackageMaintainer));
		
		PackageEvent testPackageEvent = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUpdatedUser, testPackages.get(0), 1, 1).get(0);
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		List<PackageEvent> expectedPackageEvents = new ArrayList<>();
		expectedPackageEvents.add(testPackageEvent);
		
		RepositoryEvent testRepositoryEvent = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testUpdatedUser, testRepository, 1).get(0);
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		//TODO: PackageMaintainerEvent
		
		List<RepositoryEvent> expectedRepositoryEvents = new ArrayList<>();
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		expectedRepositoryEvents.add(testRepositoryEvent);
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testUser, testUpdatedUser);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		
		when(eventRepository.findByValue("delete")).thenReturn(deleteEvent);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(eq(testPackages.get(0).getName()), eq(testRepository), eq(false))).thenReturn(null);
		
		when(userRepository.findByRoleAndDeleted(testRole, false)).thenReturn(new ArrayList<User>(testAdmins));
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				testPackageEvents.add(testPackageEvent);
				return testPackageEvent;
			}
		});
		
		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				testRepositoryEvents.add(testRepositoryEvent);
				return testRepositoryEvent;
			}
		});
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(testPackages), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(testUser.getId(), testPrincipal); 
		
		assertEquals(expectedPackageEvents, testPackageEvents);
		assertEquals(expectedRepositoryEvents, testRepositoryEvents);
		assertEquals(MessageCodes.SUCCESS_USER_DEACTIVATED, result.get("success"));
	}
	
	@Test
	public void shouldNotDeactivateUserWithUserAndDifferentUpdatedUserAdminAndNullRole() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 204
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(0);
		testUser.setActive(true);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		User testAdmin1 = UserTestFixture.GET_FIXTURE_ADMIN();
		User testAdmin2 = UserTestFixture.GET_FIXTURE_ADMIN();
		User testAdmin3 = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Set<User> testUsers = new HashSet<>();
		testUsers.add(testAdmin1);
		testUsers.add(testAdmin2);
		testUsers.add(testAdmin3);
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		
		testRole.setUsers(testUsers);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer.setPackage(testPackage.getName());
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
			
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(roleRepository.findByName("admin")).thenReturn(null);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), false)).thenReturn(testPackageMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackage.getName(), testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));

		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
		
		assertTrue(testPackageEvents.isEmpty());
		assertTrue(testRepositoryEvents.isEmpty());
		assertTrue(testUserEvents.isEmpty());
		assertEquals(MessageCodes.ERROR_ADMIN_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotDeactivateUserWithUserAndDifferentUpdatedUserAdminAndNullOneAdminLeft() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 204
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		testUser.setId(0);
		testUser.setActive(true);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
		User testUpdatedUser = UserTestFixture.GET_FIXTURE_ADMIN();
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester.setId(1);
		testRequester.setActive(true);
		
		User testAdmin1 = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Set<User> testUsers = new HashSet<>();
		testUsers.add(testAdmin1);
		
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(3, 0, 0, 0).get(0);
		
		testRole.setUsers(testUsers);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer.setPackage(testPackage.getName());
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testUpdatedUser.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
			
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), false)).thenReturn(testPackageMaintainer);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackage.getName(), testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));

		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
		
		assertTrue(testPackageEvents.isEmpty());
		assertTrue(testRepositoryEvents.isEmpty());
		assertTrue(testUserEvents.isEmpty());
		assertEquals(MessageCodes.ERROR_ADMIN_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotDeactivateUserWithUserAndDifferentUpdatedUserRepositoryMaintainerNullRepositoryMaintainer() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 204
		Date testDate =  new Date();
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testUser.setId(0);
		testUser.setActive(true);
		testUser.setLastLoggedInOn(testDate);
		int id1 = testUser.getId();
		
 		User testUpdatedUser = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		testRequester .setId(1);
		testRequester .setActive(true);
		
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUpdatedUser, 2);
		
		Package testPackage = testPackages.get(0);
		Package testUpdatedPackage = testPackages.get(1);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		testUpdatedRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUpdatedUser, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testUpdatedUser, testRepositories);
		
		RepositoryMaintainer testDeletedRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testOldRepositoryMaintainer = testRepositoryMaintainers.get(1);
		testOldRepositoryMaintainer.setId(testDeletedRepositoryMaintainer.getId());
		
		testUpdatedUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<UserEvent> testUserEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		
		Event testEventUpdate = new Event();
		testEventUpdate.setValue("update");
		Event testEventDelete = new Event();
		testEventDelete.setValue("delete");
		Event testEventUpdatePackage = new Event();
		testEventUpdatePackage.setValue("update");
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		File testFile = new File("testFile");
		
		when(userRepository.findByIdAndDeleted(id1, false)).thenReturn(testUser, testUpdatedUser);

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester .getLogin(), false)).thenReturn(testRequester );
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdatePackage, testEventUpdateRepository, testEventUpdate);		
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent packageToSave = invocation.getArgument(0);
				testPackageEvents.add(packageToSave);
				return packageToSave;
			}
		});
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
		
		});
		
		when(userEventRepository.save(any())).thenAnswer(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
		});
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testDeletedRepositoryMaintainer.getId(), false)).thenReturn(null);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDelete);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testUpdatedPackage);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testUpdatedRepository);
		
		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerService.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		when(repositoryMaintainerRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryMaintainerToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryMaintainerToSave);
				return repositoryMaintainerToSave;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = userController.deactivateUser(id1, testPrincipal); 
		
		assertTrue(testPackageEvents.isEmpty());
		assertTrue(testRepositoryEvents.isEmpty());
		assertTrue(testUserEvents.isEmpty());
		assertEquals(MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND, result.get("error"));
	}
}
