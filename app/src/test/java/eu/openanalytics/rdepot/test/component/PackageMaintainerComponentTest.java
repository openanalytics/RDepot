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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import eu.openanalytics.rdepot.controller.PackageMaintainerController;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.CreatePackageMaintainerRequestBody;
import eu.openanalytics.rdepot.model.EditPackageMaintainerRequestBody;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.test.config.MockRepositoryBeansConfig;
import eu.openanalytics.rdepot.test.config.TestPrincipal;
import eu.openanalytics.rdepot.test.config.WebApplicationTestConfig;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RoleTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, RepositoryComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class PackageMaintainerComponentTest extends BaseComponentTest {

	@Autowired
	PackageRepository packageRepository;
	
	@Autowired
	PackageMaintainerRepository packageMaintainerRepository;
	
	@Autowired
	RepositoryRepository repositoryRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PackageMaintainerService packageMaintainerService;
	
	@Autowired
	RepositoryService repositoryService;
	
	@Autowired
	UserService userService;
	
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
	private PackageMaintainerController packageMaintainerController;
	
	@Test
	public void shouldRepositoryControllerBeNotNull() {
		assertNotNull(packageMaintainerController);
	}
	
	@Test
	public void shouldReturnPackageMaintainerPage() { //line 101
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testRequester, testRepository, 3);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(packageMaintainerRepository.findByDeleted(eq(false), any())).thenReturn(testPackageMaintainers);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		Model testModel = new ExtendedModelMap();
		
		String result = packageMaintainerController.packageMaintainersPage(testModel, testPrincipal);
		
		assertEquals(testPackageMaintainers, testModel.asMap().get("packagemaintainers"));
		assertEquals("packagemaintainers", result);
	}
	
	@Test
	public void shouldReturnPackageMaintainersWithAdminCredentials() { //line 112
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testRequester, testRepository, 3);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(packageMaintainerRepository.findByDeleted(eq(false), any())).thenReturn(testPackageMaintainers);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		List<PackageMaintainer> result = packageMaintainerController.packageMaintainers(testPrincipal);
		
		assertEquals(testPackageMaintainers, result);
	}
	
	@Test
	public void shouldReturnPackageMaintainersWithRepositoryManagerCredentials() { //line 112
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		testRepositoryMaintainers.get(0).setRepository(testRepository);
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testRequester, testRepository, 3);
		
		testRequester.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(packageMaintainerRepository.findByRepository(testRepository)).thenReturn(testPackageMaintainers);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		List<PackageMaintainer> result = packageMaintainerController.packageMaintainers(testPrincipal);
		
		assertEquals(testPackageMaintainers, result);
	}
	
	@Test
	public void shouldReturndeletedPackageMaintainers() { //line 121
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testRequester, testRepository, 3);
		
		when(packageMaintainerRepository.findByDeleted(eq(true), any())).thenReturn(testPackageMaintainers);
		
		List<PackageMaintainer> result = packageMaintainerController.deletedPackageMaintainers();
		
		assertEquals(testPackageMaintainers, result);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnNewPackageMaintainerDialogWithAdminCredentials() { //line 128
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		List<User> testPackageMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(0, 3, 0);
		
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		Role testPackageMaintainerRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 1, 0).get(0);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		
		List<Repository> testOneRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testOneRepository.get(0), testRequester, 3);
		
		testOneRepository.get(0).setPackages(new HashSet<Package>(testPackages));
		
		List<User> users = new ArrayList<>();
		users.addAll(testUsers);
		users.addAll(testPackageMaintainerUsers);
		Collections.sort(users, new UserComparator());
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleRepository.findByName("user")).thenReturn(testUserRole);
		when(userRepository.findByRoleAndDeleted(testUserRole, false)).thenReturn(testUsers);
		
		when(roleRepository.findByName("packagemaintainer")).thenReturn(testPackageMaintainerRole);
		when(userRepository.findByRoleAndDeleted(testPackageMaintainerRole, false)).thenReturn(testPackageMaintainerUsers);
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories, testOneRepository);
		
		when(packageRepository.findByRepositoryAndDeleted(testOneRepository.get(0), false)).thenReturn(testPackages);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = packageMaintainerController.newPackageMaintainerDialog(testPrincipal);
		
		assertEquals(users, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
	@Test
	public void shouldReturnNewPackageMaintainerDialogWithRepositoryMaintainerCredentials() { //line 128
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
		List<User> testPackageMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(0, 3, 0);
		
		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
		Role testPackageMaintainerRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 1, 0).get(0);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		
		List<Repository> testOneRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testOneRepository.get(0), testRequester, 3);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		for(int i = 0; i < testRepositories.size(); i++) {
			testRepositoryMaintainers.get(i).setRepository(testRepositories.get(i));
		}
		
		testRequester.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		testOneRepository.get(0).setPackages(new HashSet<Package>(testPackages));
		
		List<User> users = new ArrayList<>();
		users.addAll(testUsers);
		users.addAll(testPackageMaintainerUsers);
		Collections.sort(users, new UserComparator());
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(roleRepository.findByName("user")).thenReturn(testUserRole);
		when(userRepository.findByRoleAndDeleted(testUserRole, false)).thenReturn(testUsers);
		
		when(roleRepository.findByName("packagemaintainer")).thenReturn(testPackageMaintainerRole);
		when(userRepository.findByRoleAndDeleted(testPackageMaintainerRole, false)).thenReturn(testPackageMaintainerUsers);
		
		when(packageRepository.findByRepositoryAndDeleted(testOneRepository.get(0), false)).thenReturn(testPackages);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, Object> result = packageMaintainerController.newPackageMaintainerDialog(testPrincipal);
		
		assertEquals(users, result.get("users"));
		assertEquals(testRepositories, result.get("repositories"));
	}
	
//	@Test
//	public void shouldNotReturnCreatedNewPackageMaintainerWhenUserIsNotAuthorized() { //line 143
//		User testRequester = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
//		
//		CreatePackageMaintainerRequestBody testRequestBody = new CreatePackageMaintainerRequestBody(testPackage.getName(), testRequester.getId(), testRepository.getId());
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, Object> result = packageMaintainerController.createNewPackageMaintainer(testRequestBody, testPrincipal);
//		
//		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
//	}
	
//	@Test
//	public void shouldNotReturnCreatedNewPackageMaintainerWithPackageAlreadyMaintainedError() { //line 143
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 3);
//		List<User> testPackageMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(0, 3, 0);
//		
//		Role testUserRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 0, 1).get(0);
//		Role testPackageMaintainerRole = RoleTestFixture.GET_FIXTURE_ROLES(0, 0, 1, 0).get(0);
//		
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
//		
//		Repository testRepository = testRepositories.get(0);
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
//		
//		List<User> users = new ArrayList<>();
//		users.addAll(testUsers);
//		users.addAll(testPackageMaintainerUsers);
//		Collections.sort(users, new UserComparator());
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
//		
//		when(userService.findById(testRequester.getId())).thenReturn(testRequester);
//		
//		when(repositoryService.findById(testRepository.getId())).thenReturn(testRepository);
//		
//		when(packageMaintainerService.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
//		
//		when(roleRepository.findByName("user")).thenReturn(testUserRole);
//		when(userRepository.findByRoleAndDeleted(testUserRole, false)).thenReturn(testUsers);
//		
//		when(roleRepository.findByName("packagemaintainer")).thenReturn(testPackageMaintainerRole);
//		when(userRepository.findByRoleAndDeleted(testPackageMaintainerRole, false)).thenReturn(testPackageMaintainerUsers);
//		
//		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
//		
//		CreatePackageMaintainerRequestBody testRequestbody = new CreatePackageMaintainerRequestBody(testPackage.getName(), testRequester.getId(), testRepository.getId());
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, Object> result = packageMaintainerController.createNewPackageMaintainer(testRequestbody, testPrincipal);
//		
//		assertEquals(MessageCodes.ERROR_PACKAGE_ALREADY_MAINTAINED, result.get("error"));
//		assertEquals(users, result.get("users"));
//		assertEquals(testRepositories, result.get("repositories"));
//	}
	
	@Test
	public void shouldReturnCreatedNewPageMaintainerWithRepositoryMaintainerCredentials() {
		
	}
	
//	@Test
//	public void shouldNotReturnUpdatedPackageMaintainerWhenDifferentId() { //line 201
//		int requestedId = 201;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
//		
//		Repository testRepository = testRepositories.get(0);
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(packageMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testPackageMaintainer);
//		
//		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
//		
//		EditPackageMaintainerRequestBody testRequestBody = new EditPackageMaintainerRequestBody(testPackage.getName(), testRepository.getId());
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, Object> result = packageMaintainerController.updatePackageMaintainer(testRequestBody, testPrincipal, requestedId);
//		
//		assertEquals(MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND, result.get("error"));
//	}
	
//	@Test
//	public void shouldNotReturnUpdatedPackageMaintainerWhenUserIsNotAuthorized() { //line 201
//		int requestedId = 0;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
//		
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
//		
//		Repository testRepository = testRepositories.get(0);
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(packageMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testPackageMaintainer);
//		
//		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
//		
//		EditPackageMaintainerRequestBody testRequestBody = new EditPackageMaintainerRequestBody(testPackage.getName(), testRepository.getId());
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, Object> result = packageMaintainerController.updatePackageMaintainer(testRequestBody, testPrincipal, requestedId);
//		
//		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
//	}
	
//	@Test
//	public void shouldNotReturnUpdatedPackageMaintainerWithPackageAlreadyMaintainedError() { //line 201
//		int requestedId = 0;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
//		
//		Repository testRepository = testRepositories.get(0);
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(packageMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testPackageMaintainer);
//		
//		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
//		
//		when(userRepository.findByIdAndDeleted(testRequester.getId(), false)).thenReturn(testRequester);
//		
//		when(packageMaintainerRepository.findByPackageAndRepository(testPackage.getName(), testRepository)).thenReturn(testPackageMaintainer);
//		
//		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
//		
//		EditPackageMaintainerRequestBody testRequestBody = new EditPackageMaintainerRequestBody(testPackage.getName(), testRepository.getId());
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, Object> result = packageMaintainerController.updatePackageMaintainer(testRequestBody, testPrincipal, requestedId);
//		
//		assertEquals(MessageCodes.ERROR_PACKAGE_ALREADY_MAINTAINED, result.get("error"));
//		assertEquals(testPackageMaintainer, result.get("packagemaintainer"));
//		assertEquals(testRepositories, result.get("repositories"));
//	}
	
	@Test
	public void shouldReturnUpdatedPackageMaintainerWithRepositoryMaintainerCredentials() {
		
	}
	
	@Test
	public void shouldNotReturnDeletedPackageMaintainerWithNullPackageMaintainer() { //line 249
		int requestedId = 249;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(packageMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = packageMaintainerController.deletePackageMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotReturnDeletedPackageMaintainerWhenUserIsNotAuthorized() { //line 249
		int requestedId = 0;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(packageMaintainerRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testPackageMaintainer);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = packageMaintainerController.deletePackageMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldReturnDeletedPackageMaintainer() {
		
	}
	
	@Test
	public void shouldReturnShiftDeletedPackageMaintainer() {
		
	}
}
