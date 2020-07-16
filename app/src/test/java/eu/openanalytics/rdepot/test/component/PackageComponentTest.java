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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import eu.openanalytics.rdepot.controller.PackageController;
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
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.EventRepository;
import eu.openanalytics.rdepot.repository.PackageEventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.repository.RepositoryEventRepository;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.test.config.MockRepositoryBeansConfig;
import eu.openanalytics.rdepot.test.config.TestPrincipal;
import eu.openanalytics.rdepot.test.config.WebApplicationTestConfig;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, PackageComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class PackageComponentTest extends BaseComponentTest {
	
	public static final String NULL_USER_ERROR = "User should not be null!";
	
	private List<PackageEvent> packageEvents = new ArrayList<>();
	private List<RepositoryEvent> repositoryEvents = new ArrayList<>();
	
//	@After
//	public void cleanEvents() {
//		packageEvents.clear();
//		repositoryEvents.clear();
//	}
	
	@Before
	public void setUpEvents() {
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				packageEvents.add(invocation.getArgument(0));
				return invocation.getArgument(0);
			}}); 
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				repositoryEvents.add(invocation.getArgument(0));
				return invocation.getArgument(0);
			}
			
		});
		
		packageEvents.clear();
		repositoryEvents.clear();
	}
	
	@Before
	public void preventFileSystemOperations() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException {
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(any(), anyString());
		doNothing().when(repositoryStorage).populateGeneratedFolder(any(), any(), anyString());
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), any(), any());
//		doNothing().when(repositoryStorage).linkCurrentFolderToGeneratedFolder(any(), anyString());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(any(), anyString())).thenReturn(null);
	}

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PackageRepository packageRepository;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	PackageEventRepository packageEventRepository;
	
	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	PackageMaintainerRepository packageMaintainerRepository;
	
	@Autowired
	RepositoryRepository repositoryRepository;
	
	@Autowired
	RepositoryEventRepository repositoryEventRepository;
	
	@Autowired
	RepositoryMaintainerRepository repositoryMaintainerRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	RepositoryStorage repositoryStorage;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		Locale mockLocale = Locale.ENGLISH;
		LocaleContext localeContext = Mockito.mock(LocaleContext.class);
		Mockito.when(localeContext.getLocale()).thenReturn(mockLocale);
		LocaleContextHolder.setLocaleContext(localeContext);

		when(messageSource.getMessage(anyString(), isNull(), any())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				return invocation.getArgument(0);
			}
		});
	}
	
	@Autowired
	private PackageController packageController;
	
	@Test
	public void shouldPackageControllerBeNotNull() {
		assertNotNull(packageController);
	}
	
	@Test
	public void shouldReturnPackagesPage() {
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), testUser.isDeleted())).thenReturn(testUser);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 3);
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		String page = packageController.packagesPage(testModel, testPrincipal);
		assertEquals("packages", page);
	}
	
	@Test
	public void shouldReturnPackageListWithAdminCredentials() {
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		List<User> testUsers = new ArrayList<>();
		testUsers.add(testAdmin);
		testUsers.addAll(UserTestFixture.GET_FIXTURE_USERS(1, 1, 0));
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepositories.get(0), testUsers.get(0), 1);
		testPackages.addAll(PackageTestFixture.GET_FIXTURE_PACKAGES(testRepositories.get(1), testUsers.get(1), 1, testPackages.size()));
		testPackages.addAll(PackageTestFixture.GET_FIXTURE_PACKAGES(testRepositories.get(0), testUsers.get(2), 1, testPackages.size()));
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(testPackages);
		
		List<Package> packages = packageController.packages(testPrincipal);
		
		assertEquals(testPackages, packages);
	}
	
	@Test
	public void shouldReturnPackageListWithRepositoryMaintainerCredentials() {
		Set<Package> expectedPackages = new LinkedHashSet<>();
		List<User> testMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(2, 0, 0);
		List<Repository> testRepositoriesToMaintain = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		User testRepositoryMaintainerUser = null;
		testRepositoryMaintainerUser = testMaintainerUsers.get(0);

		assertNotNull(NULL_USER_ERROR, testRepositoryMaintainerUser);
		
		for(Repository repository : testRepositoriesToMaintain) {
			Set<Package> packages = new LinkedHashSet<Package>(PackageTestFixture.GET_FIXTURE_PACKAGES(repository, testMaintainerUsers.get(1), 2));
			repository.setPackages(packages);
			expectedPackages.addAll(packages);
		}
		
		List<RepositoryMaintainer> testRepositoryMaintainers = 
				RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainerUser,
						testRepositoriesToMaintain);
		
		testRepositoryMaintainerUser.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainerUser.getLogin(), false)).thenReturn(testRepositoryMaintainerUser);
		when(userRepository.findByIdAndDeleted(testRepositoryMaintainerUser.getId(), false)).thenReturn(testRepositoryMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainerUser.getLogin());
		
		List<Package> packages = packageController.packages(testPrincipal);
		
		assertTrue(packages.containsAll(expectedPackages));
	}
	

	@Test
	public void shouldNotReturnDeletedPackagesWithRepositoryMaintainerCredentials() {
		final int REPOSITORY_MAINTAINER_USER_COUNT = 2;
		final int REPOSITORIES_TO_MAINTAIN_COUNT = 2;
		final int PACKAGES_COUNT = 2;
		final int DELETED_PACKAGE_NUMBER = 0;
		
		List<Package> expectedPackages = new ArrayList<>();
		List<User> testMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(REPOSITORY_MAINTAINER_USER_COUNT, 0, 0);
		List<Repository> testRepositoriesToMaintain = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(REPOSITORIES_TO_MAINTAIN_COUNT);
		
		User testRepositoryMaintainerUser = null;
		testRepositoryMaintainerUser = testMaintainerUsers.get(0);
		
		assertNotNull(NULL_USER_ERROR, testRepositoryMaintainerUser);
		
		for(Repository repository : testRepositoriesToMaintain) {
			List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, testMaintainerUsers.get(0), PACKAGES_COUNT);
			
			packages.get(DELETED_PACKAGE_NUMBER).setDeleted(true);
			
			Set<Package> packagesWithDeletedOne = new LinkedHashSet<Package>(packages);		
			
			repository.setPackages(packagesWithDeletedOne);
			
			for(Package package_ : packages) {
				if(!package_.isDeleted())
					expectedPackages.add(package_);
			}
		}
		
		List<RepositoryMaintainer> testRepositoryMaintainers = 
				RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainerUser,
						testRepositoriesToMaintain);
		
		testRepositoryMaintainerUser.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainerUser.getLogin(), false)).thenReturn(testRepositoryMaintainerUser);
		when(userRepository.findByIdAndDeleted(testRepositoryMaintainerUser.getId(), false)).thenReturn(testRepositoryMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainerUser.getLogin());
		
		List<Package> packages = packageController.packages(testPrincipal);
		assertEquals(expectedPackages.size(), packages.size());
	}
	
	@Test
	public void shouldNotReturnPackagesFromDeletedRepositoryWithRepositoryMaintainerCredentials() {
		final int REPOSITORY_MAINTAINER_USER_COUNT = 2;
		final int REPOSITORIES_TO_MAINTAIN_COUNT = 3;
		final int PACKAGES_COUNT = 2;
		final int DELETED_REPOSITORY_MAINTAINER_ID = 0;
		
		List<Package> expectedPackages = new ArrayList<>();
		List<User> testMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(REPOSITORY_MAINTAINER_USER_COUNT, 0, 0);
		List<Repository> testRepositoriesToMaintain = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(REPOSITORIES_TO_MAINTAIN_COUNT);
		
		
		User testRepositoryMaintainerUser = null;
		testRepositoryMaintainerUser = testMaintainerUsers.get(0);

		assertNotNull(NULL_USER_ERROR, testRepositoryMaintainerUser);
		
		for(Repository repository : testRepositoriesToMaintain) {
			Set<Package> packages = new LinkedHashSet<Package>(PackageTestFixture.GET_FIXTURE_PACKAGES(repository, testMaintainerUsers.get(1), PACKAGES_COUNT));
			repository.setPackages(packages);
			
			if(repository.getId() != DELETED_REPOSITORY_MAINTAINER_ID)
				expectedPackages.addAll(packages);
		}
		
		List<RepositoryMaintainer> testRepositoryMaintainers = 
				RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainerUser,
						testRepositoriesToMaintain);
		
		for(RepositoryMaintainer maintainer : testRepositoryMaintainers) {
			if(maintainer.getRepository().getId() == DELETED_REPOSITORY_MAINTAINER_ID)
				maintainer.setDeleted(true);
		}
		
		testRepositoryMaintainerUser.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainerUser.getLogin(), false)).thenReturn(testRepositoryMaintainerUser);
		when(userRepository.findByIdAndDeleted(testRepositoryMaintainerUser.getId(), false)).thenReturn(testRepositoryMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainerUser.getLogin());
		
		List<Package> packages = packageController.packages(testPrincipal);
		
		assertTrue(expectedPackages.containsAll(packages));
	}
	
	@Test
	@WithMockUser(username="testusername", roles= {"user"})
	public void shouldReturnPackageListWithPackageMaintainerCredentials() {
		List<Package> expectedPackages = new ArrayList<>();
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		List<User> testMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(0, 2, 0);
		Set<PackageMaintainer> testPackageMaintainers = new LinkedHashSet<>();
		
		User testPackageMaintainerUser = null;
		
		testPackageMaintainerUser = testMaintainerUsers.get(0);
		
		assertNotNull(NULL_USER_ERROR, testPackageMaintainerUser);
		
		int packageMaintainerCounter = 0;
		for(Repository repository : testRepositories) {
			List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, testPackageMaintainerUser, 2);
			repository.setPackages(new LinkedHashSet<Package>(packages));
			
			expectedPackages.addAll(packages);
			
			for(Package package_ : packages) {
				testPackageMaintainers.add(new PackageMaintainer(packageMaintainerCounter++, testPackageMaintainerUser, repository, package_.getName(), false));
			}
			
		}
		
		testPackageMaintainerUser.setPackageMaintainers(testPackageMaintainers);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainerUser.getLogin(), false)).thenReturn(testPackageMaintainerUser);
		when(userRepository.findByIdAndDeleted(testPackageMaintainerUser.getId(), false)).thenReturn(testPackageMaintainerUser);
		when(packageRepository.findByNameAndRepositoryAndDeleted(anyString(), any(Repository.class), eq(false))).thenAnswer(new Answer<List<Package>>() {
			
			@Override
			public List<Package> answer(InvocationOnMock invocation) throws Throwable {
				List<Package> testPackages = new ArrayList<>();
				String packageName = invocation.getArgument(0);
				Repository repository = invocation.getArgument(1);
				
				for(Package package_ : repository.getPackages()) {
					if(package_.getName() == packageName)
						testPackages.add(package_);
				}
				return testPackages;
			}
			
		});
		
		Principal testPrincipal = new TestPrincipal(testPackageMaintainerUser.getLogin());
		
		List<Package> packages = packageController.packages(testPrincipal);
		
		assertTrue(packages.containsAll(expectedPackages));
	}
	
	@Test
	public void shouldNotReturnPackageListFromDeletedPackageMaintainerWithPackageMaintainerCredentials() {
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		List<User> testMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(0, 2, 0);
		Set<PackageMaintainer> testPackageMaintainers = new LinkedHashSet<>();
		
		User testPackageMaintainerUser = null;
		
		testPackageMaintainerUser = testMaintainerUsers.get(0);
		
		assertNotNull(NULL_USER_ERROR, testPackageMaintainerUser);
		
		int packageMaintainerCounter = 0;
		for(Repository repository : testRepositories) {
			List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, testPackageMaintainerUser, 2);
			repository.setPackages(new LinkedHashSet<Package>(packages));

			for(Package package_ : packages) {
				testPackageMaintainers.add(new PackageMaintainer(packageMaintainerCounter++, testPackageMaintainerUser, repository, package_.getName(), true));
			}
			
		}
		
		testPackageMaintainerUser.setPackageMaintainers(testPackageMaintainers);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainerUser.getLogin(), false)).thenReturn(testPackageMaintainerUser);
		when(userRepository.findByIdAndDeleted(testPackageMaintainerUser.getId(), false)).thenReturn(testPackageMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testPackageMaintainerUser.getLogin());
		
		List<Package> packages = packageController.packages(testPrincipal);
		
		assertTrue(packages.isEmpty());
	}
	
	@Test
	public void shouldReturnEmptyPackageListWithUserCredentials() {
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		List<Package> packages = packageController.packages(testPrincipal);
		
		assertTrue(packages.isEmpty());
	}
	
	@Test
	public void shouldReturnDeletedPackagesWithAdminCredentials() {
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testAdmin, 3);
		
		when(packageRepository.findByDeleted(eq(true), any())).thenReturn(testPackages);
		
		List<Package> packages = packageController.deletedPackages();
		
		assertTrue(packages.containsAll(testPackages));
	}
	
	@Test
	public void shouldReturnPublishedPageOfExistingPackage() {
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(testRepository, testUser);
		
		int requestedPackageId = testPackage.getId();
		
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(testPackage);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		Model testModel = new ExtendedModelMap();
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String expectedPage = "package-published";
		String page = packageController.publishedPage(requestedPackageId, testModel, testRedirectAttributes, testPrincipal);
		
		int expectedRole = testUser.getRole().getValue();
		int role = (int)testModel.asMap().get("role");
		
		Package packageBag = (Package)testModel.asMap().get("packageBag");
		
		assertEquals(expectedPage, page);
		assertEquals(expectedRole, role);
		assertEquals(packageBag, testPackage);
	}
	
	@Test
	public void shouldReturnNoPublishedPageAndRedirectToPackagesPageIfPackageIsNullWithNonStandardUserCredentials() {
		User testUser = UserTestFixture.GET_FIXTURE_USERS(0, 1, 0).get(0);
		
		int requestedPackageId = 0;
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(null);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		Model testModel = new ExtendedModelMap();
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String expectedAddress = "redirect:/manager/packages";
		String address = packageController.publishedPage(requestedPackageId, testModel, testRedirectAttributes, testPrincipal);
		
		int expectedRole = testUser.getRole().getValue();
		int role = (int)testModel.asMap().get("role");
		
		String expectedMessage = MessageCodes.ERROR_PACKAGE_NOT_FOUND;
		String message = (String)testRedirectAttributes.getFlashAttributes().get("error");
		
		assertEquals(expectedMessage, message);
		assertEquals(expectedAddress, address);
		assertEquals(expectedRole, role);
	}
	
	@Test
	public void shouldReturnNoPublishedPageAndRedirectToManagerPageIfPackageIsNullWithStandardUserCredentials() {
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		
		int requestedPackageId = 0;
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(null);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		Model testModel = new ExtendedModelMap();
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String expectedAddress = "redirect:/manager";
		String address = packageController.publishedPage(requestedPackageId, testModel, testRedirectAttributes, testPrincipal);
		
		int expectedRole = testUser.getRole().getValue();
		int role = (int)testModel.asMap().get("role");
		String expectedMessage = MessageCodes.ERROR_PACKAGE_NOT_FOUND;
		String message = (String)testRedirectAttributes.getFlashAttributes().get("error");
		
		assertEquals(expectedMessage, message);
		assertEquals(expectedAddress, address);
		assertEquals(expectedRole, role);
	}
	
	@Test
	public void shouldReturnErrorMessageForPackageEventsWithUserCredentialsWhenUserIsNotPackageAuthor() {
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(0, 0, 2);
		User testUser = testUsers.get(0);
		User testPackageAuthor = testUsers.get(1);
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(testRepository, testPackageAuthor);
		
		int requestedPackageId = 0;
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(testPackage);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		HashMap<String, Object> response = packageController.events(requestedPackageId, testPrincipal);
		
		String expectedMessage = MessageCodes.ERROR_USER_NOT_AUTHORIZED;
		String message = (String)response.get("error");
		
		assertEquals(expectedMessage, message);
	}
	
	@Test
	public void shouldReturnErrorMessageForPackageEventsWhenRepositoryMaintainerHasNoMatchingRepositoriesMaintained() {
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(2, 0, 0);
		User testRepositoryMaintainerUser = testUsers.get(1);
		User testPackageAuthor = testUsers.get(0);
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(testRepository, testPackageAuthor);
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3, 1);
		List<RepositoryMaintainer> testRepositoryMaintainers = 
				RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRepositoryMaintainerUser, testRepositories);
		
		testRepositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedPackageId = 0;
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(testPackage);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainerUser.getLogin(), false)).thenReturn(testRepositoryMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainerUser.getLogin());
		
		HashMap<String, Object> response = packageController.events(requestedPackageId, testPrincipal);
		
		String expectedMessage = MessageCodes.ERROR_USER_NOT_AUTHORIZED;
		String message = (String)response.get("error");
		
		assertEquals(expectedMessage, message);
	}
	
	@Test
	public void shouldReturnErrorMessageForPackageEventsWhenPackageMaintainerHasNoMatchingPackagesMaintained() {
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(2, 0, 0);
		User testRepositoryMaintainerUser = testUsers.get(0);
		User testPackageAuthor = testUsers.get(1);
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testRepositoryMaintainerUser, testRepository, 1);
		
		testPackageMaintainers.get(0).setRepository(testRepository);
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(testRepository, testPackageAuthor);
		
		testRepositoryMaintainerUser.setPackageMaintainers(new HashSet<PackageMaintainer>(testPackageMaintainers));
		
		int requestedPackageId = testPackage.getId();
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(testPackage);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainerUser.getLogin(), false)).thenReturn(testRepositoryMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainerUser.getLogin());
		
		HashMap<String, Object> response = packageController.events(requestedPackageId, testPrincipal);
		
		String expectedMessage = MessageCodes.ERROR_USER_NOT_AUTHORIZED;
		String message = (String)response.get("error");
		
		assertEquals(expectedMessage, message);
	}
	
	@Test
	public void shouldReturnErrorMessageForPackageEventsWhenRepositoriesDoNotMatch() {
		final String ERROR_MESSAGE = "User was authorized or error message is not correct!";
		final int TEST_REPOSITORY_MAINTAINER_USER_COUNT = 0;
		final int TEST_PACKAGE_MAINTAINER_USER_COUNT = 2;
		final int TEST_STANDARD_USER_COUNT = 0;
		final int TEST_REPOSITORY_COUNT = 2;
		
		List<User> testUsers = UserTestFixture.GET_FIXTURE_USERS(TEST_REPOSITORY_MAINTAINER_USER_COUNT, TEST_PACKAGE_MAINTAINER_USER_COUNT, TEST_STANDARD_USER_COUNT);
		User testRepositoryMaintainerUser = testUsers.get(0);
		User testPackageAuthor = testUsers.get(1);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(TEST_REPOSITORY_COUNT);
		Repository testRepositoryOfGivenPackage = testRepositories.get(0);
		Repository testRepositoryOfGivenMaintainer = testRepositories.get(1);
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testRepositoryMaintainerUser, testRepositoryOfGivenMaintainer, 2);
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepositoryOfGivenPackage, testPackageAuthor, 1, 2).get(0);
		
		testRepositoryMaintainerUser.setPackageMaintainers(new HashSet<PackageMaintainer>(testPackageMaintainers));
		
		int requestedPackageId = testPackage.getId();
		when(packageRepository.findByIdAndDeleted(requestedPackageId, false)).thenReturn(testPackage);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainerUser.getLogin(), false)).thenReturn(testRepositoryMaintainerUser);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainerUser.getLogin());
		
		HashMap<String, Object> response = packageController.events(requestedPackageId, testPrincipal);
		
		String expectedMessage = MessageCodes.ERROR_USER_NOT_AUTHORIZED;
		String message = (String)response.get("error");
		
		assertEquals(ERROR_MESSAGE, expectedMessage, message);
	}
	
	@Test
	public void shouldReturnSortedPackageEvents() {
		final String ERROR_MESSAGE = "Events are not registered or they are not in correct order!";
		final int TEST_REPOSITORY_COUNT = 1;
		final int TEST_PACKAGE_COUNT = 1;
		final int TEST_PACKAGE_MAINTAINER_COUNT = 1;
		final int TEST_PACKAGE_MAINTAINER_USER_COUNT = 1;
		final int TEST_PACKAGE_EVENT_DAY_COUNT = 3;
		final int TEST_PACKAGE_EVENTS_PER_DAY_COUNT = 2;
		final int TEST_REPOSITORY_MAINTAINER_USER_COUNT = 0;
		final int TEST_STANDARD_USER_COUNT = 0;
		
		final int REQUESTED_PACKAGE_ID = 0;
		
		User testUser = UserTestFixture.GET_FIXTURE_USERS(TEST_REPOSITORY_MAINTAINER_USER_COUNT, TEST_PACKAGE_MAINTAINER_USER_COUNT, TEST_STANDARD_USER_COUNT).get(0);
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(TEST_REPOSITORY_COUNT).get(0);
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, TEST_PACKAGE_COUNT).get(0);
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testUser, testRepository, TEST_PACKAGE_MAINTAINER_COUNT).get(0);
		testPackageMaintainer.setPackage(testPackage.getName());
		Set<PackageMaintainer> testPackageMaintainers = new HashSet<>();
		testPackageMaintainers.add(testPackageMaintainer);
		testUser.setPackageMaintainers(testPackageMaintainers);
		
		List<PackageEvent> testPackageEvents = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUser, testPackage, TEST_PACKAGE_EVENT_DAY_COUNT, TEST_PACKAGE_EVENTS_PER_DAY_COUNT);
		
		
		
		when(packageRepository.findByIdAndDeleted(REQUESTED_PACKAGE_ID, false)).thenReturn(testPackage);
		
		when(packageEventRepository.findByPackage(testPackage)).thenReturn(testPackageEvents);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		when(packageEventRepository.findByDateAndPackage(any(), any())).thenAnswer(new Answer<List<PackageEvent>>() {
			
			@Override
			public List<PackageEvent> answer(InvocationOnMock invocation) throws Throwable {
				Date date = invocation.getArgument(0);
				Package packageBag = invocation.getArgument(1);
				List<PackageEvent> packageEvents = new ArrayList<>();
				
				for(PackageEvent event : testPackageEvents) {
					if(event.getPackage() == packageBag && event.getDate() == date)
						packageEvents.add(event);
				}
				
				return packageEvents;
				
			}
		});
		
		Collections.reverse(testPackageEvents);
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		@SuppressWarnings("unchecked")
		TreeMap<Date, TreeSet<PackageEvent>> result = (TreeMap<Date, TreeSet<PackageEvent>>)packageController.events(REQUESTED_PACKAGE_ID, testPrincipal).get("result");
		
		List<PackageEvent> sortedPackageEvents = new ArrayList<>();
		for(Map.Entry<Date, TreeSet<PackageEvent>> entry : result.entrySet()) {
			for(PackageEvent event : entry.getValue()) {
				sortedPackageEvents.add(event);
			}
		}
		
		assertEquals(ERROR_MESSAGE, testPackageEvents, sortedPackageEvents);
	}
	
	@Test
	public void testActivatePackage() throws RepositoryStorageException {
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int testRepositoryCurrentVersion = testRepository.getVersion();
		User testUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		Package testPackageToActivate = PackageTestFixture.GET_FIXTURE_PACKAGE(testRepository, testUser);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUser, testRepository);
		
		testRepository.setPublished(false);
		testPackageMaintainer.setPackage(testPackageToActivate.getName());
		
		testPackageToActivate.setActive(false);
		
		when(packageRepository.findByIdAndDeleted(testPackageToActivate.getId(), false)).thenReturn(testPackageToActivate);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		when(packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(testPackageToActivate.getName(), testRepository, false)).thenReturn(testPackageMaintainer);
				
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		HashMap<String,String> response = packageController.activatePackage(testPackageToActivate.getId(), testPrincipal);
		
		assertEquals("Success message is not correct.", MessageCodes.SUCCESS_PACKAGE_ACTIVATED,response.get("success"));
		assertTrue("Package should be set active.", testPackageToActivate.isActive());
		assertEquals("Repository was not updated correctly.", testRepositoryCurrentVersion + 1, testRepository.getVersion());
		assertEquals("There should be exactly one package event.", 1, packageEvents.size());
		assertEquals("active", packageEvents.get(0).getChangedVariable());
		assertEquals("true", packageEvents.get(0).getValueAfter());
	}
	
	@Test
	public void testDeactivatePackage() {
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int testRepositoryCurrentVersion = testRepository.getVersion();
		User testUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		Package testPackageToDeactivate = PackageTestFixture.GET_FIXTURE_PACKAGE(testRepository, testUser);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testUser, testRepository);
		testRepository.setPublished(false);
		testPackageMaintainer.setPackage(testPackageToDeactivate.getName());
		
		testPackageToDeactivate.setActive(true);
		when(packageRepository.findByIdAndDeleted(testPackageToDeactivate.getId(), false)).thenReturn(testPackageToDeactivate);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
		when(packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(testPackageToDeactivate.getName(), testRepository, false)).thenReturn(testPackageMaintainer);
				
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		HashMap<String,String> response = packageController.deactivatePackage(testPackageToDeactivate.getId(), testPrincipal);

		System.out.println("WARN: " + response.get("success"));
		assertEquals("Success message is not correct.", MessageCodes.SUCCESS_PACKAGE_DEACTIVATED,response.get("success"));
		assertFalse("Package should be set active.", testPackageToDeactivate.isActive());
		assertEquals("Repository was not updated correctly.", testRepositoryCurrentVersion + 1, testRepository.getVersion());
		assertEquals("There should be exactly one package event.", 1, packageEvents.size());
		assertEquals("active", packageEvents.get(0).getChangedVariable());
		assertEquals("false", packageEvents.get(0).getValueAfter());
	}
}
