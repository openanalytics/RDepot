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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.io.File;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import eu.openanalytics.rdepot.comparator.UserComparator;
import eu.openanalytics.rdepot.controller.RepositoryMaintainerController;
import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.RepositoryStorageException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.CreateRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.model.EditRepositoryMaintainerRequestBody;
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
import eu.openanalytics.rdepot.repository.RepositoryMaintainerEventRepository;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.SubmissionRepository;
import eu.openanalytics.rdepot.repository.UserEventRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.test.config.MockRepositoryBeansConfig;
import eu.openanalytics.rdepot.test.config.TestPrincipal;
import eu.openanalytics.rdepot.test.config.WebApplicationTestConfig;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RoleTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, RepositoryMaintainerComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class RepositoryMaintainerComponentTest extends BaseComponentTest {

	@Autowired
	EventRepository eventRepository;
	
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
	SubmissionRepository submissionRepository;
	
	@Autowired
	UserRepository userRepository;	
	
	@Autowired
	PackageService packageService;
	
	@Autowired
	RoleService roleService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserEventRepository userEventRepository;
	
	@Autowired
	PackageMaintainerRepository packageMaintainerRepository;
	
	@Autowired
	PackageEventRepository packageEventRepository;

	@Autowired
	RepositoryMaintainerEventRepository repositoryMaintainerEventRepository;


	@Autowired
	RepositoryStorage repositoryStorage;
	
	@Autowired
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
	
	@Autowired
	private RepositoryMaintainerController repositoryMaintainerController;	
	
	private int placeholder = 9;
	
	@Test
	public void shouldRepositoryMaintainerControllerBeNotNull() {
		assertNotNull(repositoryMaintainerController);
	}
	
	@Test
	public void shouldReturnRepositoryMaintainersPage() { //line 96
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		Model testModel = new ExtendedModelMap();
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5) ;
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
			testRepositoryMaintainer,
			testRepositories);
		
		when(repositoryMaintainerRepository.findByDeleted(eq(false), any())).thenReturn(testRepositoryMaintainers);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		String page = repositoryMaintainerController.repositoryMaintainersPage(testModel);
		
		assertEquals(testRepositoryMaintainers,testModel.asMap().get("repositorymaintainers"));
		assertEquals(placeholder, testModel.asMap().get("role"));
		assertEquals("repositorymaintainers", page);
	}
	
	@Test
	public void shouldReturnNewRepositoryMaintainerDialog() { //line 104
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		HashMap<String, Object> result = repositoryMaintainerController.newRepositoryMaintainerDialog();
		
		
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
		assertEquals(placeholder, result.get("role"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithNullRequester() { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testAdmin);
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithNonAuthorizedUser() { //line 114
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USERS(1, 0, 0).get(0);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testRepositoryMaintainer.getId(), testRepository.getId());
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testRepositoryMaintainer);
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		
		Principal testPrincipalUser = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithNullUserError() { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(null);
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(null);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithNullRepositoryError() { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testAdmin);
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(null);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(null);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithRepositoryMaintainerDuplicateError() { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
		
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testAdmin);
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		RepositoryMaintainer testCheckDuplicate = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testAdmin, testRepositories).get(0);
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(testCheckDuplicate);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_REPOSITORYMAINTAINER_DUPLICATE, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldReturnCreatedNewRepositoryMaintainerWithUserRole() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		File testFile = new File("test");
		
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = new RepositoryMaintainer(0, testUser, testRepository, false);
		
		List<Package> testPackagesList = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		Set<Package> testPackages =  new HashSet<Package>(testPackagesList);
		testRepository.setPackages(testPackages);
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
		
		PackageMaintainer testPackageMaintainer = new PackageMaintainer(0, testUser, testRepository, testPackage.getName(), false);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
	
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		Event testEvent = new Event();
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testUser);
		
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);

		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(null);
		
		when(roleRepository.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);
		
		when(eventRepository.findByValue("create")).thenReturn(testEvent);
		
		when(repositoryMaintainerRepository.save(any())).thenReturn(testRepositoryMaintainer);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainer.getId(), false)).thenReturn(testRepositoryMaintainer);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		when(repositoryEventRepository.save(any())).then(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		when(packageEventRepository.save(any())).then(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackagesList);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackagesList, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED, result.get("success"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithUserRoleAndUpdatedEventNotFound() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		File testFile = new File("test");
		
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = new RepositoryMaintainer(0, testUser, testRepository, false);
		
		List<Package> testPackagesList = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		Set<Package> testPackages =  new HashSet<Package>(testPackagesList);
		testRepository.setPackages(testPackages);
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
		
		PackageMaintainer testPackageMaintainer = new PackageMaintainer(0, testUser, testRepository, testPackage.getName(), false);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
	
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		Event testEvent = new Event();
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testUser);
		
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);

		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(null);
		
		when(roleRepository.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		
		when(eventRepository.findByValue("update")).thenReturn(null);
		
		when(eventRepository.findByValue("create")).thenReturn(testEvent);
		
		when(repositoryMaintainerRepository.save(any())).thenReturn(testRepositoryMaintainer);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainer.getId(), false)).thenReturn(testRepositoryMaintainer);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		when(repositoryEventRepository.save(any())).then(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		when(packageEventRepository.save(any())).then(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackagesList);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackagesList, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_EVENT_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnCreatedNewRepositoryMaintainerWithUserRoleAndCreatedEventNotFound() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		File testFile = new File("test");
		
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = new RepositoryMaintainer(0, testUser, testRepository, false);
		
		List<Package> testPackagesList = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
		Set<Package> testPackages =  new HashSet<Package>(testPackagesList);
		testRepository.setPackages(testPackages);
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
		
		PackageMaintainer testPackageMaintainer = new PackageMaintainer(0, testUser, testRepository, testPackage.getName(), false);
		
		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
	
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainers);
		
		Collections.sort(allUsers, new UserComparator());
		
		Event testEvent = new Event();
		List<UserEvent> testUserEvents = new ArrayList<>();
		
		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testUser);
		
		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);

		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(null);
		
		when(roleRepository.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		
		when(eventRepository.findByValue("update")).thenReturn(testEvent);
		
		when(eventRepository.findByValue("create")).thenReturn(null);
		
		when(repositoryMaintainerRepository.save(any())).thenReturn(testRepositoryMaintainer);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainer.getId(), false)).thenReturn(testRepositoryMaintainer);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		
		when(repositoryEventRepository.save(any())).then(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		when(packageEventRepository.save(any())).then(new Answer<UserEvent>() {

			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				UserEvent userToSave = invocation.getArgument(0);
				testUserEvents.add(userToSave);
				return userToSave;
			}
			
		});
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackagesList);
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackagesList, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
		
		assertEquals(MessageCodes.ERROR_EVENT_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
//	@Test
//	public void shouldNotReturnCreatedNewRepositoryMaintainerWithUserRoleAndPackageNotFound() throws RepositoryStorageException { //line 114
//		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		File testFile = new File("test");
//		
//		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1).get(0);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1).get(0);
//		
//		RepositoryMaintainer testRepositoryMaintainer = new RepositoryMaintainer(0, testUser, testRepository, false);
//		
//		List<Package> testPackagesList = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1);
//		Set<Package> testPackages =  new HashSet<Package>(testPackagesList);
//		testRepository.setPackages(testPackages);
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
//		
//		PackageMaintainer testPackageMaintainer = new PackageMaintainer(0, testUser, testRepository, testPackage.getName(), false);
//		
//		CreateRepositoryMaintainerRequestBody testRequestBody = new CreateRepositoryMaintainerRequestBody(testAdmin.getId(), testRepository.getId());
//	
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(5);
//		
//		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
//		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
//		
//		List<User> testRepositoryMaintainers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
//		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
//		
//		List<User> allUsers = new ArrayList<>();
//		allUsers.addAll(testUsers);
//		allUsers.addAll(testRepositoryMaintainers);
//		
//		Collections.sort(allUsers, new UserComparator());
//		
//		Event testEvent = new Event();
//		List<UserEvent> testUserEvents = new ArrayList<>();
//		
//		when(userRepository.findByIdAndDeleted(testRequestBody.getUserId(), false)).thenReturn(testUser);
//		
//		when(repositoryRepository.findByIdAndDeleted(testRequestBody.getRepositoryId(), false)).thenReturn(testRepository);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
//		
//		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(null);
//
//		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testAdmin, testRepository, false)).thenReturn(null);
//		
//		when(roleRepository.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
//		
//		when(eventRepository.findByValue("update")).thenReturn(testEvent);
//		
//		when(eventRepository.findByValue("create")).thenReturn(testEvent);
//		
//		when(repositoryMaintainerRepository.save(any())).thenReturn(testRepositoryMaintainer);
//		
//		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainer.getId(), false)).thenReturn(testRepositoryMaintainer);
//		
//		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
//		
//		when(roleService.findByName("user")).thenReturn(testUserRole);
//		
//		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
//		
//		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
//		
//		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainers);
//		
//		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
//		
//		when(repositoryEventRepository.save(any())).then(new Answer<UserEvent>() {
//
//			@Override
//			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
//				UserEvent userToSave = invocation.getArgument(0);
//				testUserEvents.add(userToSave);
//				return userToSave;
//			}
//			
//		});
//		
//		when(packageEventRepository.save(any())).then(new Answer<UserEvent>() {
//
//			@Override
//			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
//				UserEvent userToSave = invocation.getArgument(0);
//				testUserEvents.add(userToSave);
//				return userToSave;
//			}
//			
//		});
//		
//		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackagesList);
//		
//		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
//		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
//		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackagesList, testRepository, dateStamp);
//		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
//		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
//		
//		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
//		
//		HashMap<String, Object> result = repositoryMaintainerController.createNewRepositoryMaintainer(testRequestBody, testPrincipalUser);
//		
//		assertEquals(MessageCodes.ERROR_PACKAGE_NOT_FOUND, result.get("error"));
//		assertEquals(allUsers, result.get("users"));
//		assertEquals(testRepositories, result.get("repositories"));
//	}

	@Test
	public void shouldNotReturnUpdatedRepositoryMaintainerWithDiffrentId() { //line 159
		int requestedId = 159;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories).get(0);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		EditRepositoryMaintainerRequestBody testRequestBody = new EditRepositoryMaintainerRequestBody(testRepository.getId()); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(testRequestBody, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnUpdatedRepositoryMaintainerWithNullRequester() { //line 159
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories).get(0);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(null);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		EditRepositoryMaintainerRequestBody testRequestBody = new EditRepositoryMaintainerRequestBody(testRepository.getId()); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(testRequestBody, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnUpdatedRepositoryMaintainerWhenUserIsNotAuthorized() { //line 159
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories).get(0);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		EditRepositoryMaintainerRequestBody testRequestBody = new EditRepositoryMaintainerRequestBody(testRepository.getId()); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(testRequestBody, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnUpdatedRepositoryMaintainerWithRepositoryMaintainerDuplicateError() { //line 159
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		
		RepositoryMaintainer testRepositoryMaintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories).get(0);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testRequester, testRepository, false)).thenReturn(testRepositoryMaintainer);
			
		EditRepositoryMaintainerRequestBody testRequestBody = new EditRepositoryMaintainerRequestBody(testRepository.getId()); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(testRequestBody, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORYMAINTAINER_DUPLICATE, result.get("error"));
		assertEquals(allUsers, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}

	@Test
	public void shouldReturnUpdatedRepositoryMaintainerWithAdminCredentials() { //line 159
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		
		RepositoryMaintainer testRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testUpdatedRepositoryMaintainer = testRepositoryMaintainers.get(1);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 2);
		
		Package testPackage = testPackages.get(0);
		testPackage.setActive(true);
		Package testUpdatedPackage = testPackages.get(1);
		testUpdatedPackage.setActive(true);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer2.setPackage(testPackage.getName());
		testPackageMaintainer2.setId(testPackageMaintainer.getId());
		
		Set<PackageMaintainer> testPackageMaintainers = new HashSet<PackageMaintainer>();
		testPackageMaintainers.add(testPackageMaintainer);
		testPackageMaintainers.add(testPackageMaintainer2);
		
		testRepository.setPackageMaintainers(testPackageMaintainers);
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testRequester.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		Event testUpdateEvent = new Event();
		testUpdateEvent.setValue("update");
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer, testUpdatedRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testRequester, testRepository, false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(testUpdateEvent);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerRepository.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer2);
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
			
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}

		});
					
		when(repositoryMaintainerEventRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		});
					
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent repositoryToSave = invocation.getArgument(0);
				testPackageEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		});
			
		EditRepositoryMaintainerRequestBody testRequestBody = new EditRepositoryMaintainerRequestBody(testRepository.getId()); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = repositoryMaintainerController.updateRepositoryMaintainer(testRequestBody, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, result.get("success"));
		assertNull(result.get("users"));
		assertNull(result.get("repositories"));
	}
	
	@Test
	public void shouldNotReturnDeletedRepositoryMaintainerWithNullRequester() { //line 221
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		
		RepositoryMaintainer testRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testUpdatedRepositoryMaintainer = testRepositoryMaintainers.get(1);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 2);
		
		Package testPackage = testPackages.get(0);
		testPackage.setActive(true);
		Package testUpdatedPackage = testPackages.get(1);
		testUpdatedPackage.setActive(true);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer2.setPackage(testPackage.getName());
		testPackageMaintainer2.setId(testPackageMaintainer.getId());
		
		Set<PackageMaintainer> testPackageMaintainers = new HashSet<PackageMaintainer>();
		testPackageMaintainers.add(testPackageMaintainer);
		testPackageMaintainers.add(testPackageMaintainer2);
		
		testRepository.setPackageMaintainers(testPackageMaintainers);
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testRequester.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		Event testUpdateEvent = new Event();
		testUpdateEvent.setValue("update");
		
		Event testDeleteEvent = new Event();
		testUpdateEvent.setValue("delete");
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer, testUpdatedRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(null);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testRequester, testRepository, false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(testUpdateEvent);
		when(eventRepository.findByValue("delete")).thenReturn(testDeleteEvent);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerRepository.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer2);
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
			
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}

		});
					
		when(repositoryMaintainerEventRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		});
					
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent repositoryToSave = invocation.getArgument(0);
				testPackageEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		}); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = repositoryMaintainerController.deleteRepositoryMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotReturnDeletedRepositoryMaintainerWhenUserIsNotAuthorized() { //line 221
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		
		RepositoryMaintainer testRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testUpdatedRepositoryMaintainer = testRepositoryMaintainers.get(1);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 2);
		
		Package testPackage = testPackages.get(0);
		testPackage.setActive(true);
		Package testUpdatedPackage = testPackages.get(1);
		testUpdatedPackage.setActive(true);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer2.setPackage(testPackage.getName());
		testPackageMaintainer2.setId(testPackageMaintainer.getId());
		
		Set<PackageMaintainer> testPackageMaintainers = new HashSet<PackageMaintainer>();
		testPackageMaintainers.add(testPackageMaintainer);
		testPackageMaintainers.add(testPackageMaintainer2);
		
		testRepository.setPackageMaintainers(testPackageMaintainers);
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testRequester.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		Event testUpdateEvent = new Event();
		testUpdateEvent.setValue("update");
		
		Event testDeleteEvent = new Event();
		testUpdateEvent.setValue("delete");
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer, testUpdatedRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testRequester, testRepository, false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(testUpdateEvent);
		when(eventRepository.findByValue("delete")).thenReturn(testDeleteEvent);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerRepository.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer2);
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
			
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}

		});
					
		when(repositoryMaintainerEventRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		});
					
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent repositoryToSave = invocation.getArgument(0);
				testPackageEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		}); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = repositoryMaintainerController.deleteRepositoryMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldReturnDeletedRepositoryMaintainerWithAdminCredentials() { //line 221
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Role testRepositoryRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 1, 0, 0).get(0);
		
		List<User> allUsers = new ArrayList<>();
		allUsers.addAll(testUsers);
		allUsers.addAll(testRepositoryMaintainerUsers);
		
		Collections.sort(allUsers, new UserComparator());
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setVersion(1);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		
		RepositoryMaintainer testRepositoryMaintainer = testRepositoryMaintainers.get(0);
		RepositoryMaintainer testUpdatedRepositoryMaintainer = testRepositoryMaintainers.get(1);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 2);
		
		Package testPackage = testPackages.get(0);
		testPackage.setActive(true);
		Package testUpdatedPackage = testPackages.get(1);
		testUpdatedPackage.setActive(true);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		
		testUpdatedPackage.setId(testPackage.getId());
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		PackageMaintainer testPackageMaintainer2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		
		testPackageMaintainer.setPackage(testUpdatedPackage.getName());
		testPackageMaintainer2.setPackage(testPackage.getName());
		testPackageMaintainer2.setId(testPackageMaintainer.getId());
		
		Set<PackageMaintainer> testPackageMaintainers = new HashSet<PackageMaintainer>();
		testPackageMaintainers.add(testPackageMaintainer);
		testPackageMaintainers.add(testPackageMaintainer2);
		
		testRepository.setPackageMaintainers(testPackageMaintainers);
		
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		packageMaintainers.add(testPackageMaintainer);
		packageMaintainers.add(testPackageMaintainer2);
		
		testRequester.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		
		Event testUpdateEvent = new Event();
		testUpdateEvent.setValue("update");
		
		Event testDeleteEvent = new Event();
		testUpdateEvent.setValue("delete");
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = new ArrayList<>();
		List<PackageEvent> testPackageEvents = new ArrayList<>();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepositoryMaintainer, testUpdatedRepositoryMaintainer);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleService.findByName("user")).thenReturn(testUserRole);
		when(userService.findByRole(testUserRole)).thenReturn(testUsers);
		
		when(roleService.findByName("repositorymaintainer")).thenReturn(testRepositoryRole);
		when(userService.findByRole(testRepositoryRole)).thenReturn(testRepositoryMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
		
		when(repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(testRequester, testRepository, false)).thenReturn(null);
		
		when(eventRepository.findByValue("update")).thenReturn(testUpdateEvent);
		when(eventRepository.findByValue("delete")).thenReturn(testDeleteEvent);
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), false)).thenReturn(testPackage);
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
		when(packageMaintainerRepository.findByPackageAndRepository(testUpdatedPackage.getName(), testRepository)).thenReturn(testPackageMaintainer2);
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
			
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}

		});
					
		when(repositoryMaintainerEventRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryMaintainerEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryMaintainerEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		});
					
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent repositoryToSave = invocation.getArgument(0);
				testPackageEvents.add(repositoryToSave);
				return repositoryToSave;
			}
					
		}); 
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = repositoryMaintainerController.deleteRepositoryMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, result.get("success"));
	}
	
	@Test
	public void shouldReturnShiftDeletedRepository() {
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		RepositoryMaintainer testRepositoryMaintainer = testRepositoryMaintainers.get(0); 
		
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = RepositoryMaintainerEventTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER_EVENTS(testRequester, testRepositoryMaintainer, 4);
		
		testRepositoryMaintainer.setRepositoryMaintainerEvents(new HashSet<RepositoryMaintainerEvent>(testRepositoryMaintainerEvents));
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(requestedId, true)).thenReturn(testRepositoryMaintainer);
		
		when(repositoryMaintainerEventRepository.getOne(0)).thenReturn(testRepositoryMaintainerEvents.get(0));
		when(repositoryMaintainerEventRepository.getOne(1)).thenReturn(testRepositoryMaintainerEvents.get(1));
		when(repositoryMaintainerEventRepository.getOne(2)).thenReturn(testRepositoryMaintainerEvents.get(2));
		
		HashMap<String, String> result = repositoryMaintainerController.shiftDeleteRepositoryMaintainer(requestedId);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, result.get("success"));
	}
}
