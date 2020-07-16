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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.security.Principal;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import eu.openanalytics.rdepot.controller.SubmissionController;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.EventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.SubmissionEventRepository;
import eu.openanalytics.rdepot.repository.SubmissionRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.EmailService;
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
import eu.openanalytics.rdepot.test.fixture.SubmissionEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, SubmissionComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class SubmissionComponentTest extends BaseComponentTest {

	@Autowired
	EventRepository eventRepository;
	
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
	SubmissionRepository submissionRepository;
	
	@Autowired
	SubmissionEventRepository submissionEventRepository;
	
	@Autowired
	PackageMaintainerService packageMaintainerService;
	
	@Autowired
	RepositoryService repositoryService;
	
	@Autowired
	EmailService emailService;
	
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
	private SubmissionController submissionController;
	
	@Test
	public void shouldRepositoryControllerBeNotNull() {
		assertNotNull(submissionController);
	}
	
//	TODO createSubmission test
	
	@Test
	public void shouldReturnSubmissions() { //line 156
		User testSubmitter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testSubmitter, 1).get(0);
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testSubmitter, testPackage, 3);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testSubmitter.getLogin(), false)).thenReturn(testSubmitter);
		
		when(submissionRepository.findByUserAndDeleted(eq(testSubmitter), eq(false), any())).thenReturn(testSubmissions);
		
		Principal testPrincipal = new TestPrincipal(testSubmitter.getLogin());
		
		List<Submission> submissionList = submissionController.submissions(testPrincipal);
		
		assertEquals(testSubmissions, submissionList);
	}
	
	@Test
	public void shouldReturnDeletedSubmissions() { //line 166
		User testSubmitter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testSubmitter, 1).get(0);
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testSubmitter, testPackage, 3);
		
		when(submissionRepository.findByDeleted(eq(true), any())).thenReturn(testSubmissions);
		
		List<Submission> submissionList = submissionController.deletedSubmissions();
		
		assertEquals(testSubmissions, submissionList);
	}
	
	@Test
	public void shouldReturnSubmissionsPage() { //line 173
		User testSubmitter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testSubmitter, 1).get(0);
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testSubmitter, testPackage, 3);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testSubmitter.getLogin(), false)).thenReturn(testSubmitter);
		
		when(submissionRepository.findByUserAndDeleted(eq(testSubmitter), eq(false), any())).thenReturn(testSubmissions);
		
		Principal testPrincipal = new TestPrincipal(testSubmitter.getLogin());
		
		Model testModel = new ExtendedModelMap();
		
		String result = submissionController.submissionsPage(testPrincipal, testModel);
		
		assertEquals(testSubmitter, testModel.asMap().get("user"));
		assertEquals(testSubmissions, testModel.asMap().get("submissions"));
		assertEquals("submissions", result);
	}
	
	@Test
	public void shouldReturnAllSubmissionsPageWithAdminCredentials() { //line 185
		User testUser = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testUser, testPackage, 3);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		when(submissionRepository.findByDeleted(eq(false), any())).thenReturn(testSubmissions);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		Model testModel = new ExtendedModelMap();
		
		String result = submissionController.allSubmissionsPage(testPrincipal, testModel);
		
		assertEquals(testUser, testModel.asMap().get("user"));
		assertEquals(testSubmissions, testModel.asMap().get("submissions"));
		assertEquals("submissions", result);
	}
	
	@Test
	public void shouldReturnAllSubmissionsPageWithReposiotryMaintainerCredentials() { //line 185
		User testUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		
		User testDifferentUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		testDifferentUser.setId(1);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(1); 
		
		Repository testRepository = testRepositories.get(0);
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
		
		List<RepositoryMaintainer> testRepositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(testDifferentUser, testRepositories);
				
		testDifferentUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(testRepositoryMaintainers));
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testUser, testPackage, 3);
		
		List<Submission> expectedSubmissions = new ArrayList<>();
		
		expectedSubmissions.addAll(testSubmissions);
		expectedSubmissions.addAll(testSubmissions);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testDifferentUser);
		
		when(submissionRepository.findByDeletedAndPackage_Repository(false, testRepository)).thenReturn(testSubmissions);
		
		when(submissionRepository.findByUserAndDeleted(eq(testDifferentUser), eq(false), any())).thenReturn(testSubmissions);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		Model testModel = new ExtendedModelMap();
		
		String result = submissionController.allSubmissionsPage(testPrincipal, testModel);
		
		assertEquals(testUser, testModel.asMap().get("user"));
		assertEquals(expectedSubmissions, testModel.asMap().get("submissions"));
		assertEquals("submissions", result);
	}
	
	@Test
	public void shouldReturnAllSubmissionPageWithPackageMaintainerCredentials() { //line 185
		User testUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		User testDifferentUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		testDifferentUser.setId(1); 
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> testPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 3);
		
		List<PackageMaintainer> testPackageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(testDifferentUser, testRepository, 3);
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testUser, testPackages.get(0), 3);
		
		testDifferentUser.setPackageMaintainers(new HashSet<PackageMaintainer>(testPackageMaintainers));
		
		List<Submission> expectedSubmissions = new ArrayList<>();
		
		for(int i = 0; i < 4; i++) {
			expectedSubmissions.addAll(testSubmissions);
		}

		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		when(userRepository.findByIdAndDeleted(testUser.getId(), false)).thenReturn(testDifferentUser);
		
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackageMaintainers.get(0).getPackage(), testRepository, false)).thenReturn(testPackages);
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackageMaintainers.get(1).getPackage(), testRepository, false)).thenReturn(testPackages);
		when(packageRepository.findByNameAndRepositoryAndDeleted(testPackageMaintainers.get(2).getPackage(), testRepository, false)).thenReturn(testPackages);
		
		when(submissionRepository.findByPackageAndDeleted(testPackages.get(0), false)).thenReturn(testSubmissions.get(0));
		when(submissionRepository.findByPackageAndDeleted(testPackages.get(1), false)).thenReturn(testSubmissions.get(1));
		when(submissionRepository.findByPackageAndDeleted(testPackages.get(2), false)).thenReturn(testSubmissions.get(2));
		
		when(submissionRepository.findByUserAndDeleted(eq(testDifferentUser), eq(false), any())).thenReturn(testSubmissions);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		Model testModel = new ExtendedModelMap();
		
		String result = submissionController.allSubmissionsPage(testPrincipal, testModel);
		
		assertEquals(testUser, testModel.asMap().get("user"));
		assertEquals(expectedSubmissions, testModel.asMap().get("submissions"));
		assertEquals("submissions", result);
	}
	
	@Test
	public void shouldReturnAllSubmissionsPageWithUserCredentials() { //line 185
		User testUser = UserTestFixture.GET_FIXTURE_USER();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testUser, 1).get(0);
		
		List<Submission> testSubmissions = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testUser, testPackage, 3);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testUser.getLogin(), false)).thenReturn(testUser);
		
		when(submissionRepository.findByUserAndDeleted(eq(testUser), eq(false), any())).thenReturn(testSubmissions);
		
		Principal testPrincipal = new TestPrincipal(testUser.getLogin());
		
		Model testModel = new ExtendedModelMap();
		
		String result = submissionController.allSubmissionsPage(testPrincipal, testModel);
		
		assertEquals(testUser, testModel.asMap().get("user"));
		assertEquals(testSubmissions, testModel.asMap().get("submissions"));
		assertEquals("submissions", result);
	}
	
//	@Test
//	public void shouldNotReturnCanceledSubmissionByMaintainerWithNullSubmission() { //line 197
//		int requestedId = 197;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.cancelSubmissionBySubmitterOrMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertEquals(MessageCodes.ERROR_SUBMISSION_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
//	
//	@Test
//	public void shouldNotReturnCanceledSubmissionByMaintainerWithAlreadyAcceptedSubmission() { //line 197
//		int requestedId = 197;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
//		testSubmission.setAccepted(true);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.cancelSubmissionByMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertEquals(MessageCodes.ERROR_SUBMISSION_ALREADY_ACCEPTED, testRedirectAttributes.getFlashAttributes().get("error"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
//	@Test
//	public void shouldNotReturnCanceledSubmissionByMaintainerWhenUserIsNotAuthorizedToCancel() { //line 197
//		int requestedId = 197;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_USER();
//		
//		User testDifferentUser = UserTestFixture.GET_FIXTURE_USER();
//		testDifferentUser.setId(requestedId);
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testDifferentUser, 1).get(0);
//		
//		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testDifferentUser, testPackage, 1).get(0);
//		testSubmission.setAccepted(false);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.cancelSubmissionByMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, testRedirectAttributes.getFlashAttributes().get("error"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
//	
//	@Test
//	public void shouldReturnCanceledSubmissionByMaintainer() { //line 197
//		int requestedId = 197;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
//		testSubmission.setAccepted(false);
//		
//		Event testDeleteEvent  = new Event();
//		testDeleteEvent.setValue("delete");
//		
//		List<SubmissionEvent> testEvents = new ArrayList<>();
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
//		
//		when(eventRepository.findByValue("delete")).thenReturn(testDeleteEvent);
//		
//		when(submissionRepository.findByIdAndDeleted(testSubmission.getId(), false)).thenReturn(testSubmission);
//		
//		when(submissionEventRepository.save(any())).thenAnswer(new Answer<SubmissionEvent>() {
//			
//			@Override
//			public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
//				SubmissionEvent toSave = new SubmissionEvent();
//				testEvents.add(toSave);
//				return toSave;
//			}
//			
//		});
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.cancelSubmissionByMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertFalse(testEvents.isEmpty());
//		assertEquals(MessageCodes.SUCCESS_SUBMISSION_CANCELED, testRedirectAttributes.getFlashAttributes().get("success"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
	
	@Test
	public void shouldNotReturnCanceledSubmissionBySubmitterOrMaintainerWithNullSubmission() { //line 227
		int requestedId = 227;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = submissionController.cancelSubmissionBySubmitterOrMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_SUBMISSION_NOT_FOUND, result.get("error"));
	}
	
	@Test 
	public void shouldNotReturnCanceledSubmissionBySubmitterOrMaintainerWithAlreadyAcceptedSubmission() { //line 227
		int requestedId = 227;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
		testSubmission.setAccepted(true);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
				
		HashMap<String, String> result = submissionController.cancelSubmissionBySubmitterOrMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED, result.get("warning"));
	}
	
	@Test
	public void shouldNotReturnCanceledSubmissionBySubmitterOrMaintainerWhenUserIsNotAuthorizedToCancel() { //line 227
		int requestedId = 227;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER();
		
		User testDifferentUser = UserTestFixture.GET_FIXTURE_USER();
		testDifferentUser.setId(requestedId);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testDifferentUser, 1).get(0);
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testDifferentUser, testPackage, 1).get(0);
		testSubmission.setAccepted(false);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
				
		HashMap<String, String> result = submissionController.cancelSubmissionBySubmitterOrMaintainer(requestedId, testPrincipal);
		
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, result.get("error"));
	}
	
	@Test
	public void shouldReturnCanceledSubmissionBySubmitterOrMaintainerWithUserCredentials() {
		int requestedId = 227;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
		testSubmission.setAccepted(false);
		
		Event testDeleteEvent = new Event();
		testDeleteEvent.setValue("delete");
		
		List<SubmissionEvent> testEvents = new ArrayList<>();
		
		List<SubmissionEvent> expectedEvents = new ArrayList<>();
		SubmissionEvent toSave = new SubmissionEvent(0, new Date(), testRequester, testSubmission, testDeleteEvent, "deleted", "", "", new Date());
		expectedEvents.add(toSave);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
		
		when(eventRepository.findByValue("delete")).thenReturn(testDeleteEvent);
		
		when(submissionRepository.findByIdAndDeleted(testSubmission.getId(), false)).thenReturn(testSubmission);
		
		when(submissionEventRepository.save(any())).then(new Answer<SubmissionEvent>() {
			@Override
			public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
				testEvents.add(toSave);
				return toSave;
			}
		});
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		HashMap<String, String> result = submissionController.cancelSubmissionBySubmitterOrMaintainer(requestedId, testPrincipal);
		
		assertEquals(expectedEvents, testEvents);
		assertEquals(MessageCodes.SUCCESS_SUBMISSION_CANCELED, result.get("success"));
	}
	
	@Test
	public void shouldNotReturnSubmissionPageWithNullSubmission() { //line 266
		int requestedId = 266;
		
		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		Model testModel = new ExtendedModelMap();
		
		String address = submissionController.submissionPage(requestedId, testPrincipal, testRedirectAttributes, testModel);
		
		assertEquals("redirect:/manager/submissions/all", address);
		assertEquals(MessageCodes.ERROR_SUBMISSION_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
	}
	
	@Test
	public void shouldNotReturnSubmissionPageWhenUserIsNotAUthorizedToCancel() { //line 266
		int requestedId = 266;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER();
		
		User testDifferentUser = UserTestFixture.GET_FIXTURE_USER();
		testDifferentUser.setId(requestedId);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testDifferentUser, 1).get(0);
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testDifferentUser, testPackage, 1).get(0);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		Model testModel = new ExtendedModelMap();
		
		String address = submissionController.submissionPage(requestedId, testPrincipal, testRedirectAttributes, testModel);
		
		assertEquals("redirect:/manager/submissions/all", address);
		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, testRedirectAttributes.getFlashAttributes().get("error"));
	}
	
	@Test
	public void shouldReturnSubmissionPageWithPackageMaintainerCredentials() { //line 266
		int requestedId = 266;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		User testDifferentUser = UserTestFixture.GET_FIXTURE_ADMIN();
		testDifferentUser.setId(requestedId);
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testDifferentUser, 1).get(0);
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testDifferentUser, testPackage, 1).get(0);
		
		PackageMaintainer testPackageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(testRequester, testRepository);
		Set<PackageMaintainer> testPackageMaintainers = new HashSet<>();
		testPackageMaintainers.add(testPackageMaintainer);
		
		testRequester.setPackageMaintainers(testPackageMaintainers);
		
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
		
		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
		
		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
		
		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
		
		Model testModel = new ExtendedModelMap();
		
		String address = submissionController.submissionPage(requestedId, testPrincipal, testRedirectAttributes, testModel);
		
		assertEquals(testSubmission, testModel.asMap().get("submission"));
		assertEquals(testRequester.getRole().getValue(), testModel.asMap().get("role"));
		assertEquals("submission", address);
	}
	
//	@Test
//	public void shouldNotReturnAcceptedSubmissionByMaintainerWithNullSubmissionError() { //line 287
//		int requestedId = 287;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(null);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.acceptSubmissionByMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertEquals(MessageCodes.ERROR_SUBMISSION_NOT_FOUND, testRedirectAttributes.getFlashAttributes().get("error"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
//	
//	@Test
//	public void shouldNotReturnAcceptedSubmissionByMaintainerWhenSubmissionIsAlreadyAcceptedWarning() { //line 287
//		int requestedId = 287;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_ADMIN();
//
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
//		
//		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
//		testSubmission.setAccepted(true);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.acceptSubmissionByMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertEquals(MessageCodes.ERROR_SUBMISSION_ALREADY_ACCEPTED, testRedirectAttributes.getFlashAttributes().get("warning"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
//	
//	@Test
//	public void shouldNotReturnAcceptedSubmissionByMaintainerWhenUserIsNotAuthorizedToAcceptError() { //line 287
//		int requestedId = 287;
//		
//		User testRequester = UserTestFixture.GET_FIXTURE_USER();
//		testRequester.setId(requestedId);
//		
//		User testDifferentUser = UserTestFixture.GET_FIXTURE_ADMIN();
//
//		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		
//		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testDifferentUser, 1).get(0);
//		
//		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
//		testSubmission.setAccepted(false);
//		
//		when(userRepository.findByLoginIgnoreCaseAndDeleted(testRequester.getLogin(), false)).thenReturn(testRequester);
//		
//		when(submissionRepository.findByIdAndDeleted(requestedId, false)).thenReturn(testSubmission);
//		
//		Principal testPrincipal = new TestPrincipal(testRequester.getLogin());
//		
//		RedirectAttributes testRedirectAttributes = new RedirectAttributesModelMap();
//		
//		String address = submissionController.acceptSubmissionByMaintainer(requestedId, testPrincipal, testRedirectAttributes);
//		
//		assertEquals(MessageCodes.ERROR_USER_NOT_AUTHORIZED, testRedirectAttributes.getFlashAttributes().get("error"));
//		assertEquals("redirect:/manager/submissions", address);
//	}
	
	@Test
	public void shouldReturnAcceptedSubmissionByMaintainerREST() {
		
	}
	
	@Test
	public void shouldReturnShiftDeleted() { //line 344
		int requestedId = 344;
		
		User testRequester = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		Repository testRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGES(testRepository, testRequester, 1).get(0);
		
		Submission testSubmission = SubmissionTestFixture.GET_FIXTURE_SUBMISSIONS(testRequester, testPackage, 1).get(0);
		
		List<SubmissionEvent> testSubmissionEvents = SubmissionEventTestFixture.GET_SUBMISSION_EVENT_TEST_FIXTURE(1);
		testSubmission.setSubmissionEvents(new HashSet<SubmissionEvent>(testSubmissionEvents));
		
		SubmissionEvent testSubmissionEvent = testSubmissionEvents.get(0);     
		
		when(submissionRepository.findByIdAndDeleted(requestedId, true)).thenReturn(testSubmission);
		
		when(submissionEventRepository.getOne(testSubmissionEvent.getId())).thenReturn(testSubmissionEvent);
		
		doNothing().when(submissionEventRepository).delete(testSubmissionEvent);
		
		doNothing().when(submissionRepository).delete(testSubmission);
		
		HashMap<String, String> result = submissionController.shiftDeleteSubmission(requestedId);
		
		assertEquals(MessageCodes.SUCCESS_SUBMISSION_CANCELED, result.get("success"));
	}
}
