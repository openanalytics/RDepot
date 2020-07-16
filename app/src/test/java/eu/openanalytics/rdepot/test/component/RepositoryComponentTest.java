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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import eu.openanalytics.rdepot.controller.RepositoryController;
import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.RepositoryStorageException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.EventRepository;
import eu.openanalytics.rdepot.repository.PackageEventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerEventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.repository.RepositoryEventRepository;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerEventRepository;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.SubmissionEventRepository;
import eu.openanalytics.rdepot.repository.SubmissionRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.PackageMaintainerEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.test.config.MockRepositoryBeansConfig;
import eu.openanalytics.rdepot.test.config.TestPrincipal;
import eu.openanalytics.rdepot.test.config.WebApplicationTestConfig;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RoleTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, RepositoryComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class RepositoryComponentTest extends BaseComponentTest {
	
	@Autowired
	EventRepository eventRepository;	
	
	@Autowired
	PackageRepository packageRepository;
	
	@Autowired
	PackageEventRepository packageEventRepository;
	
	@Autowired
	PackageMaintainerRepository packageMaintainerRepository;
	
	@Autowired
	PackageMaintainerEventRepository packageMaintainerEventRepository;
	
	@Autowired
	RepositoryRepository repositoryRepository;
	
	@Autowired
	RepositoryEventRepository repositoryEventRepository;
	
	@Autowired
	RepositoryMaintainerRepository repositoryMaintainerRepository;
	
	@Autowired
	RepositoryMaintainerEventRepository repositoryMaintainerEventRepository;
	
	@Autowired
	SubmissionRepository submissionRepository;
	
	@Autowired
	SubmissionEventRepository submissionEventRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	PackageService packageService;
	
	@Autowired
	PackageMaintainerService packageMaintainerService;
		
	@Autowired
	PackageMaintainerEventService packageMaintainerEventService;
	
	@Autowired
	PackageStorage packageStorage;
	
	@Autowired
	RepositoryStorage repositoryStorage;
	
	@Autowired
	MessageSource messageSource;
	
	private List<RepositoryEvent> repositoryEvents = new ArrayList<>();
	private List<PackageEvent> packageEvents = new ArrayList<>();
	private List<RepositoryMaintainerEvent> repositoryMaintainerEvents = new ArrayList<>();
	private List<PackageMaintainerEvent> packageMaintainerEvents = new ArrayList<>();

	
	@Before
	public void setUpEvents() {
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				repositoryEvents.add(invocation.getArgument(0));
				return invocation.getArgument(0);
			}
			
		});
		
		when(packageEventRepository.save(any())).thenAnswer(new Answer<PackageEvent>() {

			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				packageEvents.add(invocation.getArgument(0));
				return invocation.getArgument(0);
			}
			
		});
		
		when(repositoryMaintainerEventRepository.save(any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {

			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				repositoryMaintainerEvents.add(invocation.getArgument(0));
				return invocation.getArgument(0);
			}
			
		});
		
		when(packageMaintainerEventRepository.save(any())).thenAnswer(new Answer<PackageMaintainerEvent>() {

			@Override
			public PackageMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				packageMaintainerEvents.add(invocation.getArgument(0));
				return invocation.getArgument(0);
			}
			
		});
		
		repositoryEvents.clear();
		repositoryMaintainerEvents.clear();
		packageMaintainerEvents.clear();
	}
	
	
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
	private RepositoryController repositoryController;
	
	@Test
	public void shouldRepositoryControllerBeNotNull() { 
		assertNotNull(repositoryController);
	}
	
	@Test
	public void shouldReturnRepositoriesPageWithAdminCredentials() { //114
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		String page = repositoryController.repositoriesPage(testModel, testPrincipalUser);
		
		assertEquals("repositories", page);
	}
	
	@Test
	public void shouldReturnRepositoriesPageWithRepositoryMaintainerCredentials() { //line 114
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainer,
						testRepositories);
		
		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Integer> maintained = new ArrayList<>();
		for(Repository r : testRepositories) {
			maintained.add(r.getId());
		}
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipalUser = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		String page = repositoryController.repositoriesPage(testModel, testPrincipalUser);
			
		assertEquals(maintained, testModel.asMap().get("maintained"));
		assertEquals("repositories", page);
	}
	
	@Test
	public void shouldReturnRepositoriesPageWithUserOrPackageMaintainerCredentials() { //line 114
		List<User> users = UserTestFixture.GET_FIXTURE_USERS(0, 1, 1);
		User testUser = users.get(1);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		User testPackageMaintainer = users.get(0);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainer.getLogin(), false)).thenReturn(testPackageMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		Model testModel1 = new ExtendedModelMap();
		Model testModel2 = new ExtendedModelMap();
		Principal testPrincipalUser = new TestPrincipal(testUser.getLogin());
		Principal testPrincipalPackageMaintainer = new TestPrincipal(testPackageMaintainer.getLogin());
		
		String pageUser = repositoryController.repositoriesPage(testModel1, testPrincipalUser);
		String pagePackageMaintainer = repositoryController.repositoriesPage(testModel2, testPrincipalPackageMaintainer);

		assertEquals("repositories", pageUser);
		assertEquals("repositories", pagePackageMaintainer);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnrepositoriesPageWithCorrectModelAndUserCredentials() { //line 114
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		List<Integer> exepectedMaintained = new ArrayList<>();
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipalUser = new TestPrincipal(testUser.getLogin());
		
		String page = repositoryController.repositoriesPage(testModel, testPrincipalUser);
		
		int expectedUserRole = testUser.getRole().getValue();
		int actualModelRole = (int) testModel.asMap().get("role");
		
		List<Integer> actualMaintained = (List<Integer>) testModel.asMap().get("maintained");
		
		List<Repository> actualRepositories = (List<Repository>) testModel.asMap().get("repositories");
		
		assertEquals(expectedUserRole, actualModelRole);
		assertEquals(exepectedMaintained, actualMaintained);
		assertEquals(testRepositories, actualRepositories);
		assertEquals("repositories", page);
	}
	
	@Test
	public void shouldNotCreateNewRepositoryWithNullRequester() { //line 127
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotCreateNewRepositoryWithNonAdminCredentials() { //line 127
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldCreateNewRepositoryWithAdminCredentialsAndDuplicateNameErrors() { //line 127
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");

		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(testRepository, result.get("repository"));
		assertEquals(bindingResult, result.get("org.springframework.validation.BindingResult.repository"));
	}
	
	@Test
	public void shouldCreateNewRepositoryWithAdminCredentialsAndDuplicationUriError() { //line 127
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(testRepository);
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(testRepository, result.get("repository"));
		assertEquals(bindingResult, result.get("org.springframework.validation.BindingResult.repository"));
	}
	
	@Test
	public void shouldCreateNewRepositoryWithAdminCredentialsIdAndNullRepositoryError() { //line 127
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		int requestedId = 127;
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		testRepository.setId(requestedId);
		
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(testRepository, result.get("repository"));
		assertEquals(bindingResult, result.get("org.springframework.validation.BindingResult.repository"));
	}
	
	@Test
	public void shouldCreateNewRepositoryWithAdminCredentialsIdAndNameError() { //line 127
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		int requestedId = 127;
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setId(requestedId);
		Repository testRepositoryDuplicate = testRepositories.get(1);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepositoryDuplicate);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(testRepository, result.get("repository"));
		assertEquals(bindingResult, result.get("org.springframework.validation.BindingResult.repository"));
	}
	
	@Test
	public void shouldCreateNewRepositoryWithAdminCredentialsIdAndDuplicationUriError() { //line 127
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		int requestedId = 127;
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setId(requestedId);
		Repository testRepositoryDuplicate = testRepositories.get(1);
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(testRepositoryDuplicate);
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(testRepository, result.get("repository"));
		assertEquals(bindingResult, result.get("org.springframework.validation.BindingResult.repository"));
	}
	
	@Test
	public void shouldReturnErrorWhenRepositoryIsDuplicated() { //line 127
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		when(repositoryRepository.saveAndFlush(testRepository)).thenThrow(new DataIntegrityViolationException("duplicate"));
		
		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_DUPLICATE, result.get("error"));
	}
	
//	@Test
//	public void shouldCreateNewRepository() { //line 127
//		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
//		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
//		
//		Event testEvent = new Event();
//		testEvent.setValue("create");
//		
//		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
//		
//		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
//		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
//		
//		when(repositoryRepository.saveAndFlush(testRepository)).thenReturn(testRepository);
//		
//		when(eventRepository.findByValue("create")).thenReturn(testEvent);
//		
//		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
//		
//		when(repositoryEventRepository.save(any())).then(new Answer<RepositoryEvent> () {
//			@Override
//			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
//					RepositoryEvent eventToSave = new RepositoryEvent();
//					testRepositoryEvents.add(eventToSave);
//				return eventToSave;
//			}
//		});
//		
//		HashMap<String, Object> result = repositoryController.createNewRepository(testRepository, testPrincipal, bindingResult);
//		
//		assertFalse(testRepositoryEvents.isEmpty());
//		assertEquals(MessageCodes.SUCCESS_REPOSITORY_CREATED, result.get("success"));
//	}
	
	@Test
	public void shouldReturnUpdatedNewsFeedWithAdminCredentials() { //line 184
		String data = "1999-01-16";
		int lastPosition = 13;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1);
		Repository testRepository = testRepositories.get(0);
		
		List<RepositoryEvent> testRepositoryEvents = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(testRequester, testRepository, 3);
		testRepositoryEvents.get(0).setChangedVariable("added");
		testRepositoryEvents.get(1).setChangedVariable("submitted");
		testRepositoryEvents.get(1).setDate(testRepositoryEvents.get(0).getDate());
		testRepositoryEvents.get(2).setChangedVariable("removed");
		testRepositoryEvents.get(2).setDate(testRepositoryEvents.get(0).getDate());
		
		Package testPackageAdded = new Package();
		Package testPackageRemoved = new Package();
		
		Submission testSubmission = new Submission();
		testSubmission.setId(lastPosition);
		testSubmission.setPackage(testPackageAdded);
		testSubmission.setChanges("no");
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(repositoryRepository.findAll(Sort.by(new Order(Direction.ASC, "name")))).thenReturn(testRepositories);
		
		when(repositoryEventRepository.findAll()).thenReturn(testRepositoryEvents);
		
		when(repositoryEventRepository.findByDateAndRepository(any(), eq(testRepository))).thenReturn(testRepositoryEvents);
		
		when(packageRepository.getOne(56780)).thenReturn(testPackageAdded);
		when(submissionRepository.getOne(56781)).thenReturn(testSubmission);
		when(packageRepository.getOne(56782)).thenReturn(testPackageRemoved);
		
		LinkedHashMap<String, ArrayList<HashMap<String, String>>> result = repositoryController.updateNewsfeed(testPrincipal, testRedirectAttributes, data, lastPosition);
		
		assertFalse(result.isEmpty());
	}
	
	@Test
	public void shouldReturnNewsFeedPageWithCorrectModel() { //line 299
		List<User> users = new ArrayList<>();
		users.add(UserTestFixture.GET_FIXTURE_ADMIN());
		users.addAll(UserTestFixture.GET_FIXTURE_USERS(1, 1, 1));
		
		User testAdmin = users.get(0);
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.newsfeedPage(testPrincipal, testModel, testRedirectAttributes);
		
		int expectedUserRole = testAdmin.getRole().getValue();
		int actualModelRole = (int) testModel.asMap().get("role");
		
		assertEquals(expectedUserRole, actualModelRole);
		assertEquals("newsfeed", address);	
	}
	
	@Test
	public void shouldNotReturnPackagesOfRepositoryPageWhenRepositoryIsNull() { //line 308
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		int requestedId = 0;
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.packagesOfRepositoryPage(requestedId, testPrincipal, testModel, testRedirectAttributes);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
		assertEquals("redirect:/manager/repositories", address);
	}
	
	@Test
	public void shouldNotReturnPackagesOfRepositoryPageWhenUserIsNotAuthorizedToEdit() { // line 308

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		int requestedId = 0;
		
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.packagesOfRepositoryPage(requestedId, testPrincipal, testModel, testRedirectAttributes);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, testRedirectAttributes.getFlashAttributes().get("error"));
		assertEquals("redirect:/manager/repositories", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPackagesOfRepositoryPageWithAdminCredentials() { // line 308

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testAdmin, 3);
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.packagesOfRepositoryPage(requestedId, testPrincipal, testModel, testRedirectAttributes);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		int expectedRole = testAdmin.getRole().getValue();
		int actualRole = (int) testModel.asMap().get("role");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(expectedRole, actualRole);
		assertEquals(testPackages, actualPackages);
		assertEquals("packages", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPackagesOfRepositoryPageWithRepositoryMaintainerCredentials() { //line 308

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Repository testRepository = testRepositories.get(0);
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainer,
						testRepositories);
		
		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.packagesOfRepositoryPage(requestedId, testPrincipal, testModel, testRedirectAttributes);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		int expectedRole = testRepositoryMaintainer.getRole().getValue();
		int actualRole = (int) testModel.asMap().get("role");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(expectedRole, actualRole);
		assertEquals(testPackages, actualPackages);
		assertEquals("packages", address);
	}
	
	@Test
	public void shouldReturnErrorMessageGivenWrongRepositoryId() { //line 331 updateRepository
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		int requestedId = 331;
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND,response.get("error"));
	}
	
	@Test
	public void shouldReturnErrorMessageWithNullRequester() { //line 331 updateRepository

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND,response.get("error"));
	}
	
	@Test
	public void shouldReturnErrorMessageWhenUserIsNotAuthorizedToEdit() { //line 331 updateRepository
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED,response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithAdminCredentials() { //line 331
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		int requestedId = testUpdatedRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testUpdatedRepository);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		Event testEvent = new Event(1, "update");
		when(eventRepository.findByValue("update")).thenReturn(testEvent);
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
			
		});
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		assertFalse(testRepositoryEvents.isEmpty());
		assertTrue(testRepository.getPublicationUri().equals(testUpdatedRepository.getPublicationUri()));
		assertTrue(testRepository.getServerAddress().equals(testUpdatedRepository.getServerAddress()));
		assertTrue(testRepository.getName().equals(testUpdatedRepository.getName()));
		assertEquals(MessageCodes.SUCCESS_REPOSITORY_UPDATED,response.get("success"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithAdminCredentialsAndDuplicateNameError() { //line 331
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		int requestedId = testRepository.getId();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_NAME, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithAdminCredentialsAndDuplicatePublicationUriError() { //line 331
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(testRepository);
		
		int requestedId = testRepository.getId();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_PUBLICATIONURI, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithAdminCredentialsIdAndNullRepositoryError() { //line 331
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		testRepository.setId(331);
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(null);
		
		int requestedId = testRepository.getId();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithAdminCredentialsIdAndDuplicateNameError() { //line 331
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		testRepository.setId(331);
		
		Repository testRepository2 = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository2);
		
		int requestedId = testRepository.getId();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_NAME, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithAdminCredentialsIdAndDuplicatePublicationUriError() { //line 331
		
		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		testRepository.setId(331);
		
		Repository testRepository2 = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(testRepository2);
		
		int requestedId = testRepository.getId();
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_PUBLICATIONURI, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithRepositoryMaintainerCredentials() { //line 331

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
				testRepositoryMaintainer,
				testRepositories);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedId = testUpdatedRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testUpdatedRepository);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		BindingResult bindingResult = Mockito.mock(BindingResult.class);
		when(bindingResult.hasErrors()).thenReturn(false);
		
		Event testEvent = new Event(1, "update");
		when(eventRepository.findByValue("update")).thenReturn(testEvent);
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
			}
			
		});
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		assertFalse(testRepositoryEvents.isEmpty());
		assertTrue(testRepository.getPublicationUri().equals(testUpdatedRepository.getPublicationUri()));
		assertTrue(testRepository.getServerAddress().equals(testUpdatedRepository.getServerAddress()));
		assertTrue(testRepository.getName().equals(testUpdatedRepository.getName()));
		assertEquals(MessageCodes.SUCCESS_REPOSITORY_UPDATED,response.get("success"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithRepositoryMaintainerCredentialsAndDuplicateNameError() { //line 331

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
				testRepositoryMaintainer,
				testRepositories);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedId = testUpdatedRepository.getId();
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_NAME, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithRepositoryMaintainerCredentialsAndDuplicatePublicationUriError() { //line 331

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
				testRepositoryMaintainer,
				testRepositories);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedId = testUpdatedRepository.getId();
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_PUBLICATIONURI, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithRepositoryMaintainerCredentialsIdAndNullRepository() { //line 331

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository testRepository = testRepositories.get(0);
		testRepository.setId(331);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
				testRepositoryMaintainer,
				testRepositories);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedId = testUpdatedRepository.getId();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithRepositoryMaintainerCredentialsIdAndDuplicateNameError() { //line 331

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Repository testRepository = testRepositories.get(0);
		testRepository.setId(331);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		Repository testRepository2 = testRepositories.get(2);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
				testRepositoryMaintainer,
				testRepositories);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedId = testUpdatedRepository.getId();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository2);
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_NAME, response.get("error"));
	}
	
	@Test
	public void shouldReturnUpdatedRepositoryWithRepositoryMaintainerCredentialsIdAndDuplicatePublicationUriError() { //line 331

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Repository testRepository = testRepositories.get(0);
		testRepository.setId(331);
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setId(testRepository.getId());
		Repository testRepository2 = testRepositories.get(2);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
				testRepositoryMaintainer,
				testRepositories);

		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		int requestedId = testUpdatedRepository.getId();
		
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository2);
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		when(repositoryRepository.findByPublicationUriAndDeleted(testRepository.getPublicationUri(), false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		BindingResult bindingResult = new BeanPropertyBindingResult(testRepository, "repository");
		
		HashMap<String, Object> response = repositoryController.updateRepository(testRepository, bindingResult, requestedId, testPrincipal);
		
		Repository actualRepository = (Repository) response.get("repository");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(bindingResult, response.get("org.springframework.validation.BindingResult.repository"));
		assertEquals(MessageCodes.ERROR_FORM_DUPLICATE_PUBLICATIONURI, response.get("error"));
	}
	
	@Test
	public void shouldReturnRepositories() { //line 370

		/* Hibernate.initialize(user.getRepositoryMaintainers()); (Line no. 163) 
		 * in UserService in method public User findByLoginWithMaintainers(String login)  
		 * might cause some problems.*/
		
		User testUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
	
		List<Repository> expectedRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(expectedRepositories);
		List<Repository> actualRepositories = repositoryController.repositories(testPrincipal);
		
		assertEquals(expectedRepositories, actualRepositories);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPublishedPageNameWithAdminCredentials() { //line 428
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(testRepository);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testAdmin, 3);
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.publishedPage(requestedName, testRedirectAttributes, testModel, testPrincipal);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(testPackages, actualPackages);
		assertEquals("repository-published", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPublishedPageNameWithRepositoryMaintainerCredentials() { //line 428
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(testRepository);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.publishedPage(requestedName, testRedirectAttributes, testModel, testPrincipal);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(testPackages, actualPackages);
		assertEquals("repository-published", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPublishedPageNameWhenUserIsNotAuthorized() { //line 428
		User testPackageMaintainer = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainer.getLogin(), false)).thenReturn(testPackageMaintainer);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(testRepository);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testPackageMaintainer, 3);
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testPackageMaintainer.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.publishedPage(requestedName, testRedirectAttributes, testModel, testPrincipal);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(testPackages, actualPackages);
		assertEquals("repository-published", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPublishedPageNameWithNullPrincipal() { //line 428
		User testPackageMaintainer = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainer.getLogin(), false)).thenReturn(testPackageMaintainer);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(testRepository);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testPackageMaintainer, 3);
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = null;
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.publishedPage(requestedName, testRedirectAttributes, testModel, testPrincipal);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertEquals(testRepository, actualRepository);
		assertEquals(testPackages, actualPackages);
		assertEquals("repository-published", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPublishedPageNameWithAdminOrRepositoryMaintainerCredentialsWithNullRepository() { //line 428
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(null);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.publishedPage(requestedName, testRedirectAttributes, testModel, testPrincipal);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertNull(actualRepository);
		assertNull(actualPackages);
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
		assertEquals("redirect:/manager/repositories", address);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnPublishedPageNameWithNullPrincipleAndNullRepository() { //line 428
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(null);
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = null;
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String address = repositoryController.publishedPage(requestedName, testRedirectAttributes, testModel, testPrincipal);
		
		Repository actualRepository = (Repository) testModel.asMap().get("repository");
		
		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
		
		assertNull(actualRepository);
		assertNull(actualPackages);
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testModel.asMap().get("error"));
		assertFalse(MessageCodes.ERROR_REPOSITORY_NOT_FOUND.equals(testRedirectAttributes.getFlashAttributes().get("error")));
		assertEquals("error", address);
	}
	
//	@Test
//	@SuppressWarnings("unchecked")
//	
//	public void shouldReturnPublishedPageIdWithAdminCredentials() { //line 435
//		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testAdmin, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertEquals(testRepository, actualRepository);
//		assertEquals(testPackages, actualPackages);
//		assertEquals("repository-published", address);
//	}
//	
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldReturnPublishedPageIdWithRepositoryMaintainerCredentials() { //line 435
//		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertEquals(testRepository, actualRepository);
//		assertEquals(testPackages, actualPackages);
//		assertEquals("repository-published", address);
//	}
	
//	public void shouldReturnPublishedPageIdWithAdminCredentials() { //line 435
//		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testAdmin, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertEquals(testRepository, actualRepository);
//		assertEquals(testPackages, actualPackages);
//		assertEquals("repository-published", address);
//	}
//	
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldReturnPublishedPageIdWithRepositoryMaintainerCredentials() { //line 435
//		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertEquals(testRepository, actualRepository);
//		assertEquals(testPackages, actualPackages);
//		assertEquals("repository-published", address);
//	}
//	
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldReturnPublishedPageIdWhenUserIsNotAuthorized() { //line 435
//		User testPackageMaintainer = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainer.getLogin(), false)).thenReturn(testPackageMaintainer);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testPackageMaintainer, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = new TestPrincipal(testPackageMaintainer.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertEquals(testRepository, actualRepository);
//		assertEquals(testPackages, actualPackages);
//		assertEquals("repository-published", address);
//	}
//	
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldReturnPublishedPageIdWithNullPrincipal() { //line 435
//		User testPackageMaintainer = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageMaintainer.getLogin(), false)).thenReturn(testPackageMaintainer);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testPackageMaintainer, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = null;
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertEquals(testRepository, actualRepository);
//		assertEquals(testPackages, actualPackages);
//		assertEquals("repository-published", address);
//	}
//	
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldReturnPublishedPageIdWithAdminOrRepositoryMaintainerCredentialsWithNullRepository() { //line 435
//		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertNull(actualRepository);
//		assertNull(actualPackages);
//		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
//		assertEquals("redirect:/manager/repositories", address);
//	}
//	
//	@Test
//	@SuppressWarnings("unchecked")
//	public void shouldReturnPublishedPageIdWithNullPrincipleAndNullRepository() { //line 435
//		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		int requestedId = testRepository.getId();
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
//		
//		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
//		
//		Model testModel = new ExtendedModelMap();
//		Principal testPrincipal = null;
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = repositoryController.publishedPage(requestedId, testRedirectAttributes, testModel, testPrincipal);
//		
//		Repository actualRepository = (Repository) testModel.asMap().get("repository");
//		
//		List<Package> actualPackages = (List<Package>) testModel.asMap().get("packages");
//		
//		assertNull(actualRepository);
//		assertNull(actualPackages);
//		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testModel.asMap().get("error"));
//		assertFalse(MessageCodes.ERROR_REPOSITORY_NOT_FOUND.equals(testRedirectAttributes.getFlashAttributes().get("error")));
//		assertEquals("error", address);
//	}

	@Test
	public void shouldReturnPublishedRepositoryWithNullRequester() { //line 444
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = repositoryController.publishRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldReturnPublishedRepositoryWithNullRepository() { //line 444
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = repositoryController.publishRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldReturnPublishedRepositoryWhenUserIsNotAuthorized() { //line 444
		User testPackageController = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageController.getLogin(), false)).thenReturn(testPackageController);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testPackageController.getLogin());
		
		HashMap<String, String> result = repositoryController.publishRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldReturnPublishedRepositoryWithAdminCredentials() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 444
		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		Repository testRepository = testRepositories.get(0);
		int requestedId = testRepository.getId();
		testRepository.setVersion(2);
		
		Repository testUpdatedRepository = testRepositories.get(1);
		testUpdatedRepository.setVersion(1);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
		
		testRequester.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 3);
		
		testRepository.setPackages(new HashSet<Package>(testPackages));
		
		Event testEventUpdateRepository = new Event();
		testEventUpdateRepository.setValue("update");
		
		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
		
		File testFile = new File("testFile");
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository, testUpdatedRepository);
		
		when(eventRepository.findByValue("update")).thenReturn(testEventUpdateRepository);	
		
		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				RepositoryEvent repositoryToSave = invocation.getArgument(0);
				testRepositoryEvents.add(repositoryToSave);
				return repositoryToSave;
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
		
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(testRepository, dateStamp);
		doNothing().when(repositoryStorage).populateGeneratedFolder(testPackages, testRepository, dateStamp);
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(testRepository, dateStamp)).thenReturn(testFile);
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), eq(testFile), eq(testRepository));
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = repositoryController.publishRepository(requestedId, testPrincipal);
		
		assertFalse(testRepositoryEvents.isEmpty());
		assertEquals(MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, result.get("success"));
	}
	
	@Test
	public void shouldPublishRepositoryWithRepositoryMaintainerCredentials() throws RepositoryStorageException, CreateFolderStructureException, LinkFoldersException { //line 444
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User requester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		List<User> testMaintainerUsers = new ArrayList<>();
		testMaintainerUsers.add(requester);
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, requester, 2);
		List<RepositoryMaintainer> testMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testMaintainerUsers, testRepository);
		testRepository.setPublished(false);
		testRepository.setRepositoryMaintainers(new HashSet<>(testMaintainers));
		testRepository.setVersion(1);
		testRepository.setPackages(new HashSet<Package>(testPackages));
		requester.setRepositoryMaintainers(new HashSet<>(testMaintainers));
		when(userRepository.findByLoginIgnoreCaseAndDeleted(requester.getLogin(), false)).thenReturn(requester);
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), any(), any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(null);
		when(eventRepository.findByValue("update")).thenReturn(updateEvent);
//		doNothing().when(repositoryEventRepository.save(any()));
		//implement event listener just as in package component test
		
		Principal testPrincipal = new TestPrincipal(requester.getLogin());
		HashMap<String,String> response = repositoryController.publishRepository(testRepository.getId(), testPrincipal);
		assertEquals(MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, response.get("success"));
	}
	
	@Test
	public void shouldNotUnpublishRepositoryWithNullRequester() { //line 475
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = repositoryController.unpublishRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotUnpublishedRepositoryWithNullRepository() { //line 475
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = repositoryController.unpublishRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldReturnUnpublishedRepositoryWhenUserIsNotAuthorized() { //line 475
		User testPackageController = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testPackageController.getLogin(), false)).thenReturn(testPackageController);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testPackageController.getLogin());
		
		HashMap<String, String> result = repositoryController.unpublishRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
//	@Test
//	public void shouldReturnUnpublishedRepositoryWithAdminCredentials() throws RepositoryStorageException {
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
//		
//		Repository testRepository = testRepositories.get(0);
//		int requestedId = testRepository.getId();
//		testRepository.setVersion(2);
//		
//		Repository testUpdatedRepository = testRepositories.get(1);
//		testUpdatedRepository.setVersion(1);
//		File
//		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
//		
//		testRequester.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 3);
//		
//		testRepository.setPackages(new HashSet<Package>(testPackages));
//		
//		Event testEventUpdateRepository = new Event();
//		testEventUpdateRepository.setValue("update");
//		
//		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository, testUpdatedRepository);
//		
//		when(eventRepository.findByValue("update")).thenReturn(testEventUpdateRepository);	
//		
//		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
//
//			@Override
//			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
//				RepositoryEvent repositoryToSave = invocation.getArgument(0);
//				testRepositoryEvents.add(repositoryToSave);
//				return repositoryToSave;
//			}
//		
//		});
//		
//		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
//		
//		doNothing().when(repositoryStorage).deleteCurrentDirectory(testRepository);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, String> result = repositoryController.unpublishRepository(requestedId, testPrincipal);
//		
//		assertFalse(testRepositoryEvents.isEmpty());
//		assertEquals(MessageCodes.SUCCESS_REPOSITORY_UNPUBLISHED, result.get("success"));
//	}
	
//	@Test
//	public void shouldReturnUnpublishedRepositoryWithRepositoryMaintainerCredentials() throws RepositoryStorageException {
//		User testRequester = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
//		
//		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
//		
//		Repository testRepository = testRepositories.get(0);
//		int requestedId = testRepository.getId();
//		testRepository.setVersion(2);
//		
//		Repository testUpdatedRepository = testRepositories.get(1);
//		testUpdatedRepository.setVersion(1);
//		
//		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRequester, testRepositories);
//		
//		testRequester.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
//		
//		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 3);
//		
//		testRepository.setPackages(new HashSet<Package>(testPackages));
//		
//		Event testEventUpdateRepository = new Event();
//		testEventUpdateRepository.setValue("update");
//		
//		List<RepositoryEvent> testRepositoryEvents = new ArrayList<>();
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository, testUpdatedRepository);
//		
//		when(eventRepository.findByValue("update")).thenReturn(testEventUpdateRepository);	
//		
//		when(repositoryEventRepository.save(any())).thenAnswer(new Answer<RepositoryEvent>() {
//
//			@Override
//			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
//				RepositoryEvent repositoryToSave = invocation.getArgument(0);
//				testRepositoryEvents.add(repositoryToSave);
//				return repositoryToSave;
//			}
//		
//		});
//		
//		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
//		
//		doNothing().when(repositoryStorage).deleteCurrentDirectory(testRepository);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		HashMap<String, String> result = repositoryController.unpublishRepository(requestedId, testPrincipal);
//		
//		assertFalse(testRepositoryEvents.isEmpty());
//		assertEquals(MessageCodes.SUCCESS_REPOSITORY_UNPUBLISHED, result.get("success"));
//	}
	
	@Test
	public void shouldNotReturnDeletedRepositoryWithNullRequester() { //line 506
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(null);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		HashMap<String, String> result = repositoryController.deleteRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_FOUND, result.get("error"));
	}
	
	@Test
	public void shouldNotReturnDeletedRepositoryWhenUserIsNotAuthorized() { //line 506
		User testRepositoryController = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryController.getLogin(), false)).thenReturn(testRepositoryController);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		int requestedId = testRepository.getId();
		when(repositoryRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testRepository);
		
		Principal testPrincipal = new TestPrincipal(testRepositoryController.getLogin());
		
		HashMap<String, String> result = repositoryController.deleteRepository(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldDeleteRepositoryAndReturnItWithAdminCredentials() throws RepositoryStorageException, CreateFolderStructureException, DeleteFileException, LinkFoldersException { //line 506
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		List<User> testAdmins = new ArrayList<>();
		testAdmins.add(testRequester);
		List<User> testRepositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(2, 0, 0, 1);
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 2);
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testRepositoryMaintainerUsers, testRepository);
		PackageMaintainer testPackageMaintainerForPackage1 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository, testPackages.get(0));
		PackageMaintainer testPackageMaintainerForPackage2 = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository, testPackages.get(1));
		List<PackageMaintainer> testPackageMaintainers = new ArrayList<>();
		testPackageMaintainers.add(testPackageMaintainerForPackage1);
		testPackageMaintainers.add(testPackageMaintainerForPackage2);
		
		testRepository.setRepositoryMaintainers(new HashSet<>(testRepositoryMaintainers));
		testRepository.setPackageMaintainers(new HashSet<>(testPackageMaintainers));
		testRepository.setPackages(new HashSet<>(testPackages));
		
		Event testUpdateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Event testDeleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Role testRole = RoleTestFixture.GET_FIXTURE_ROLES(1, 0, 0, 0).get(0);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		when(repositoryRepository.findByIdAndDeleted(testRepository.getId(), false)).thenReturn(testRepository);
		when(eventRepository.findByValue("update")).thenReturn(testUpdateEvent);
		when(eventRepository.findByValue("delete")).thenReturn(testDeleteEvent);
		when(repositoryMaintainerRepository.findByIdAndDeleted(anyInt(), eq(false))).thenAnswer(new Answer<RepositoryMaintainer>() {

			@Override
			public RepositoryMaintainer answer(InvocationOnMock invocation) throws Throwable {
				int expectedId = invocation.getArgument(0);
				
				for(RepositoryMaintainer rm : testRepositoryMaintainers) {
					if(rm.getId() == expectedId && !rm.isDeleted())
						return rm;
				}
				
				return null;
		}});
		when(packageMaintainerRepository.findByPackageAndRepository(anyString(), eq(testRepository))).thenAnswer(new Answer<PackageMaintainer>() {

			@Override
			public PackageMaintainer answer(InvocationOnMock invocation) throws Throwable {
				String packageName = invocation.getArgument(0);

				for(PackageMaintainer pm : testPackageMaintainers) {
					if(packageName == pm.getPackage() && !pm.isDeleted())
						return pm;
				}
				return null;
		}});
		when(packageMaintainerRepository.findByIdAndDeleted(anyInt(), eq(false))).thenAnswer(new Answer<PackageMaintainer>() {

			@Override
			public PackageMaintainer answer(InvocationOnMock invocation) throws Throwable {
				int id = invocation.getArgument(0);
				for(PackageMaintainer pm : testPackageMaintainers) {
					if(id == pm.getId() && !pm.isDeleted())
						return pm;
				}
				return null;
		}});
		when(packageRepository.findByIdAndDeleted(anyInt(), eq(false))).thenAnswer(new Answer<Package>() {

			@Override
			public Package answer(InvocationOnMock invocation) throws Throwable {
				int id = invocation.getArgument(0);
				for(Package packageBag : testPackages) { 
					if(packageBag.getId() == id && !packageBag.isDeleted()) {
						return packageBag;
					}
				}
				return null;
			}});
		when(roleRepository.findByName("admin")).thenReturn(testRole);
		when(userRepository.findByRoleAndDeleted(testRole, false)).thenReturn(testAdmins);
		
		when(packageRepository.findByRepositoryAndDeleted(testRepository, false)).thenReturn(testPackages);
		when(packageRepository.findByRepositoryAndActiveAndDeleted(eq(testRepository), eq(true), eq(false), any())).thenReturn(testPackages);
		
		doNothing().when(repositoryStorage).createFolderStructureForGeneration(eq(testRepository), any());
		doNothing().when(repositoryStorage).populateGeneratedFolder(eq(testPackages), eq(testRepository), any());
		doNothing().when(repositoryStorage).copyFromRepositoryToRemoteServer(any(), any(), any(), any());
		doNothing().when(repositoryStorage).deleteCurrentDirectory(any());
		when(repositoryStorage.linkCurrentFolderToGeneratedFolder(eq(testRepository), any())).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		HashMap<String, String> response = repositoryController.deleteRepository(testRepository.getId(), testPrincipal);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORY_DELETED, response.get("success"));
	}
	
	@Test
	public void shouldReturnShiftDeletedRepository() throws PackageStorageException { //line 534
		User testUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1);
		Repository testRepository =  testRepositories.get(0);
		int requestedId = testRepository.getId();
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 2);
		testRepository.setPackages(new HashSet<Package>(testPackages));
		
		Package testPackage = testPackages.get(0);
		Package testPackage2 = testPackages.get(1);
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testUser, testRepository, 2);
		PackageMaintainer testPackageMaintainer = testPackageMaintainers.get(0);
		PackageMaintainer testPackageMaintainer2 = testPackageMaintainers.get(1);
		
		testRepository.setPackageMaintainers(new HashSet<PackageMaintainer>(testPackageMaintainers));
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.
				GET_FIXTURE_REPOSITORY_MAINTAINERS(testUser, testRepositories);
		
		RepositoryMaintainer testRepositoryMaintainer = testRepositoryMaintainers.get(0);
		
		testRepository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<RepositoryMaintainerEvent> testRepositoryMaintainerEvents = RepositoryMaintainerEventTestFixture
				.GET_FIXTURE_REPOSITORY_MAINTAINER_EVENTS(testUser, testRepositoryMaintainer, 2);
		RepositoryMaintainerEvent testRepositoryMaintainerEvent = testRepositoryMaintainerEvents.get(0);
		RepositoryMaintainerEvent testRepositoryMaintainerEvent2 = testRepositoryMaintainerEvents.get(1);
		
		List<PackageMaintainerEvent> testPackageMaintainerEvents = PackageMaintainerEventTestFixture
				.GET_FIXTURE_PACKAGE_MAINTAINER_EVENTS(testUser, testPackageMaintainer, 2);
		
		List<PackageMaintainerEvent> testPackageMaintainerEvents2 = PackageMaintainerEventTestFixture
				.GET_FIXTURE_PACKAGE_MAINTAINER_EVENTS(testUser, testPackageMaintainer2, 2);
		
		List<PackageEvent> testPackageEvents = PackageEventTestFixture.GET_FIXTURE_SORTED_PACKAGE_EVENTS(testUser, testPackage, 1, 2);
		PackageEvent testPackageEvent = testPackageEvents.get(0);
		PackageEvent testPackageEvent2 = testPackageEvents.get(1);
		
		testPackage.setPackageEvents(new HashSet<PackageEvent>(testPackageEvents));
		testPackage2.setPackageEvents(new HashSet<PackageEvent>(testPackageEvents));
		
		testRepositoryMaintainer.setRepositoryMaintainerEvents(new HashSet<RepositoryMaintainerEvent>(testRepositoryMaintainerEvents));
		
		testPackageMaintainer.setPackageMaintainerEvents(new HashSet<PackageMaintainerEvent>(testPackageMaintainerEvents));
		testPackageMaintainer2.setPackageMaintainerEvents(new HashSet<PackageMaintainerEvent>(testPackageMaintainerEvents2));
		
		Event testEventDeleteRepository = new Event();
		testEventDeleteRepository.setValue("delete");
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(testUser, testPackage);
		Submission testSubmission2 = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(testUser, testPackage2);
		
		List<SubmissionEvent> testSubmissionEvents = SubmissionEventTestFixture.GET_SUBMISSION_EVENT_TEST_FIXTURE(2);
		SubmissionEvent testSubmissionEvent = testSubmissionEvents.get(0);
		SubmissionEvent testSubmissionEvent2 = testSubmissionEvents.get(1);
		
		testSubmission.setSubmissionEvents(new HashSet<SubmissionEvent>(testSubmissionEvents));
		
		testPackage.setSubmission(testSubmission);
		testPackage2.setSubmission(testSubmission2);
		
		when(repositoryRepository.findByIdAndDeleted(requestedId, true)).thenReturn(testRepository);
		
		when(repositoryMaintainerRepository.findByIdAndDeleted(testRepositoryMaintainer.getId(), true)).thenReturn(testRepositoryMaintainer);
		
		doNothing().when(repositoryMaintainerEventRepository).delete(any());
		doNothing().when(repositoryMaintainerRepository).delete(any());
		
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer.getId(), true)).thenReturn(testPackageMaintainer);
		when(packageMaintainerRepository.findByIdAndDeleted(testPackageMaintainer2.getId(), true)).thenReturn(testPackageMaintainer2);
		
		doNothing().when(packageMaintainerEventRepository).delete(any());
		doNothing().when(packageMaintainerRepository).delete(any());
		
		when(packageRepository.findByIdAndDeleted(testPackage.getId(), true)).thenReturn(testPackage);
		when(packageRepository.findByIdAndDeleted(testPackage2.getId(), true)).thenReturn(testPackage2);
		
		when(eventRepository.findByValue("delete")).thenReturn(testEventDeleteRepository);
		
		when(submissionRepository.findByIdAndDeleted(testSubmission.getId(), true)).thenReturn(testSubmission);
		
		doNothing().when(submissionEventRepository).delete(any());
		doNothing().when(submissionRepository).delete(any());
		doNothing().when(packageStorage).deleteSource(any());
		doNothing().when(packageEventRepository).delete(any());
		doNothing().when(packageRepository).delete(any());
		
		when(repositoryMaintainerEventRepository.getOne(testRepositoryMaintainerEvent.getId())).thenReturn(testRepositoryMaintainerEvent);
		when(repositoryMaintainerEventRepository.getOne(testRepositoryMaintainerEvent2.getId())).thenReturn(testRepositoryMaintainerEvent2);
		
		when(packageMaintainerEventRepository.getOne(testPackageMaintainerEvents.get(0).getId())).thenReturn(testPackageMaintainerEvents.get(0));
		when(packageMaintainerEventRepository.getOne(testPackageMaintainerEvents.get(1).getId())).thenReturn(testPackageMaintainerEvents.get(1));
		
		when(packageMaintainerEventRepository.getOne(testPackageMaintainerEvents2.get(0).getId())).thenReturn(testPackageMaintainerEvents2.get(0));
		when(packageMaintainerEventRepository.getOne(testPackageMaintainerEvents2.get(1).getId())).thenReturn(testPackageMaintainerEvents2.get(1));
		
		when(submissionEventRepository.getOne(testSubmissionEvent.getId())).thenReturn(testSubmissionEvent);
		when(submissionEventRepository.getOne(testSubmissionEvent2.getId())).thenReturn(testSubmissionEvent2);
		
		when(packageEventRepository.getOne(testPackageEvent.getId())).thenReturn(testPackageEvent);
		when(packageEventRepository.getOne(testPackageEvent2.getId())).thenReturn(testPackageEvent2);
		
		HashMap<String, String> result = repositoryController.shiftDeleteRepository(requestedId);
		
		assertEquals(MessageCodes.SUCCESS_REPOSITORY_DELETED, result.get("success"));
	}
	
	@Test
	public void shouldReturnPublishedPackagePageLatestWithNullRepository() { //line 552
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Repository testRepository = testRepositories.get(0);
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(null);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainer,
						testRepositories);
		
		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
		String packageName = testPackages.get(0).getName();
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String result = repositoryController.publishedPackagePage(requestedName, packageName, testRedirectAttributes, testModel, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testModel.asMap().get("error"));
		assertEquals("error", result);
	}
	
	@Test
	public void shouldReturnPublishedPackagePageLatestWithNullPackages() { //line 552
		User testRepositoryMaintainer = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRepositoryMaintainer.getLogin(), false)).thenReturn(testRepositoryMaintainer);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Repository testRepository = testRepositories.get(0);
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testRepositoryMaintainer,
						testRepositories);
		
		testRepositoryMaintainer.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRepositoryMaintainer, 3);
		String packageName = testPackages.get(0).getName();
		when(packageRepository.findByNameAndRepositoryAndDeleted(packageName, testRepository, false)).thenReturn(null);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testRepositoryMaintainer.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String expectedResult = "redirect:/manager/repositories/" + testRepository.getName();
		
		String result = repositoryController.publishedPackagePage(requestedName, packageName, testRedirectAttributes, testModel, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_PACKAGE_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void shouldReturnPublishedPackagePageLatest() { //line 552
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		Repository testRepository = testRepositories.get(0);
		String requestedName = testRepository.getName();
		when(repositoryRepository.findByNameAndDeleted(requestedName, false)).thenReturn(testRepository);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(
						testAdmin,
						testRepositories);
		
		testAdmin.setRepositoryMaintainers(new LinkedHashSet<RepositoryMaintainer>(testRepositoryMaintainers));

		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testAdmin, 1).get(0);
		testPackage.setVersion("0");
		
		List<Package> testPackages = new ArrayList<>();
		testPackages.add(testPackage);
		
		for(int i = 1; i < 3; i++) {
			testPackages.add(new Package(testPackage));
			testPackages.get(i).setId(i);
			testPackages.get(i).setVersion(Integer.toString(i));
		}
		
		String packageName = testPackages.get(0).getName();
		Package testLatestPackage = testPackages.get(2);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(packageName, testRepository, false)).thenReturn(testPackages);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipal = new TestPrincipal(testAdmin.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		String result = repositoryController.publishedPackagePage(requestedName, packageName, testRedirectAttributes, testModel, testPrincipal);
		
		assertEquals(testLatestPackage, testModel.asMap().get("packageBag"));
		assertEquals("package-published", result);
	}
	

	@Test
	public void shouldNotReturnPackageArchiveWithNullRequesterAndNullRepository() {
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> expectedPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 3);
		for(int i = 0; i < 3; i++) {
			expectedPackages.get(i).setName("TestPackage");
			expectedPackages.get(i).setVersion(Integer.toString(i));
		}
		Package testPackage = expectedPackages.get(0);
	
		Model testModel = new ExtendedModelMap();
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(null);
		
		String address = repositoryController.packageArchive(testRepository.getName(), testPackage.getName(), testRedirectAttributes, testModel, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testModel.asMap().get("error"));
		assertEquals("error", address);
	}
	
	@Test
	public void shouldNotReturnPackageArchiveWithNullRepository() {
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> expectedPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 3);
		for(int i = 0; i < 3; i++) {
			expectedPackages.get(i).setName("TestPackage");
			expectedPackages.get(i).setVersion(Integer.toString(i));
		}
		Package testPackage = expectedPackages.get(0);
	
		Model testModel = new ExtendedModelMap();
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(null);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		String address = repositoryController.packageArchive(testRepository.getName(), testPackage.getName(), testRedirectAttributes, testModel, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
		assertEquals("redirect:/manager/repositories", address);
	}
	
	@Test
	public void shouldReturnPackageArchive() {
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> expectedPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 3);
		for(int i = 0; i < 3; i++) {
			expectedPackages.get(i).setName("TestPackage");
			expectedPackages.get(i).setVersion(Integer.toString(i));
		}
		Collections.reverse(expectedPackages);
		Package testPackage = expectedPackages.get(0);
		
		List<Package> testPackages = new ArrayList<>();
		testPackages.add(expectedPackages.get(1));
		testPackages.add(expectedPackages.get(2));
		testPackages.add(expectedPackages.get(0));
		
		Model testModel = new ExtendedModelMap();
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		when(repositoryRepository.findByNameAndDeleted(testRepository.getName(), false)).thenReturn(testRepository);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(packageRepository.findByNameAndRepositoryAndActive(testPackage.getName(), testRepository, true)).thenReturn(testPackages);
		
		String address = repositoryController.packageArchive(testRepository.getName(), testPackage.getName(), testRedirectAttributes, testModel, testPrincipal);
		
		assertEquals(testRepository, testModel.asMap().get("repository"));
		assertEquals(expectedPackages, testModel.asMap().get("packages"));
		assertEquals("package-archive", address);
	}
}