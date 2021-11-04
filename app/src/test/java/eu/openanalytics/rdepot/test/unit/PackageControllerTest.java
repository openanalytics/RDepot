/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import eu.openanalytics.rdepot.api.v1.controller.GlobalController;
import eu.openanalytics.rdepot.api.v1.controller.PackageController;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeactivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;

@RunWith(MockitoJUnitRunner.class)
public class PackageControllerTest extends ControllerUnitTest {
	
	private static final String URI_PREFIX = "/manager/packages";
	private static final String TEST_FILES = "src/test/resources/unit/test_files";
	
	@Mock
	protected MessageSource messageSource;
	
	MockMvc mockMvc;
	
	@Mock
	PackageService packageService;
	
	@Mock
	UserService userService;
	
	@Mock
	RepositoryService repositoryService;
	
	@Mock
	PackageStorage packageStorage;
	
	@InjectMocks
	PackageController packageController;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
		.thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgument(0);
			}
			
		});
		
		mockMvc = MockMvcBuilders.standaloneSetup(packageController)
				.setViewResolvers(viewResolver())
				.setControllerAdvice(new GlobalController(userService))
				.build();
	}
	
	@Test
	public void packagesPage() throws Exception {
		final int PACKAGE_COUNT = 3;
		
		User user = getUserAndAuthenticate();
		User anotherUser = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		Principal principal = getMockPrincipal(user);
		
		List<Package> maintainedByUser = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, PACKAGE_COUNT);
		List<Integer> maintainedByUserIds = new ArrayList<>();
		
		maintainedByUser.forEach(packageBag -> maintainedByUserIds.add(packageBag.getId()));
		List<Package> allPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, anotherUser, PACKAGE_COUNT);
		allPackages.addAll(maintainedByUser);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(packageService.findMaintainedBy(user)).thenReturn(maintainedByUser);
		when(packageService.findAll()).thenReturn(allPackages);
		
		mockMvc.perform(
				get(URI_PREFIX)
				.principal(principal))
		.andExpect(model().attribute("packages", allPackages))
		.andExpect(model().attribute("role", user.getRole().getValue()))
		.andExpect(model().attribute("maintained", maintainedByUserIds));		
	}
	
	@Test
	public void packages() throws Exception {
		final int PACKAGE_COUNT = 3;

		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();

		Principal principal = getMockPrincipal(user);
		
		List<Package> maintainedByUser = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, PACKAGE_COUNT);
		
		String expected = objectMapper.writeValueAsString(maintainedByUser);
		
		when(packageService.findAllByRepositoryName(null)).thenReturn(maintainedByUser);		
		
		mockMvc.perform(
				get(URI_PREFIX + "/list")
				.principal(principal))
		.andExpect(status().isOk())
		.andExpect(content().json(expected));
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "pwd", roles = "admin")
	public void deletedPackages() throws Exception {
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> deleted = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3);
		deleted.forEach(packageBag -> packageBag.setDeleted(true));
		
		when(packageService.findByDeleted(true)).thenReturn(deleted);
		
		String expected = objectMapper.writeValueAsString(deleted);
		
		mockMvc.perform(get(URI_PREFIX + "/deleted"))
			.andExpect(status().isOk())
			.andExpect(content().json(expected));
	}
	
	@Test
	public void publishedPage_whenUserIsNotLoggedIn() throws Exception {
		final int PACKAGE_ID = 123;
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		when(packageService.findById(packageBag.getId())).thenReturn(packageBag);
		when(repositoryService.findByName(repository.getName())).thenReturn(repository);
		
		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/published"))
		.andExpect(model().attribute("packageBag", packageBag))
		.andExpect(model().attribute("repository", repository));
	}
	
	@Test
	public void publishedPage_whenUserIsLoggedIn() throws Exception {
		final int PACKAGE_ID = 123;
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		Principal principal = getMockPrincipal(user);

		when(packageService.findById(packageBag.getId())).thenReturn(packageBag);
		when(repositoryService.findByName(repository.getName())).thenReturn(repository);
		when(userService.findByLogin(user.getLogin())).thenReturn(user);
		
		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/published")
				.principal(principal))
		.andExpect(model().attribute("packageBag", packageBag))
		.andExpect(model().attribute("repository", repository))
		.andExpect(model().attribute("role", user.getRole().getValue()));
	}
	
	@Test
	public void publishedPage_redirectsToManager_whenUserIsNotLoggedInAndPackageIsNull() throws Exception {
		final int PACKAGE_ID = 123;
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(null);

		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/published"))
		.andExpect(redirectedUrl("/manager"))
		.andExpect(flash().attribute("error", "Package 123: " + MessageCodes.ERROR_PACKAGE_NOT_FOUND));
	}
	
	@Test
	public void publishedPage_redirectsToPackages_whenUserIsLoggedInAndPackageIsNull() throws Exception {
		final int PACKAGE_ID = 123;
		User user = getUserAndAuthenticate();

		Principal principal = getMockPrincipal(user);
		
		when(userService.findByLogin(user.getLogin())).thenReturn(user);
		when(packageService.findById(PACKAGE_ID)).thenReturn(null);

		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/published")
				.principal(principal))
		.andExpect(redirectedUrl("/manager/packages"));
	}
	
	@Test
	public void downloadPage() throws Exception {
		final int PACKAGE_ID = 123;
		
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		Principal principal = getMockPrincipal(user);
		File expectedFile = new File(TEST_FILES + "/test_downloadPage/test_1.5.0.tar.gz");
		
		if(!expectedFile.exists())
			fail("Test file not found!");
		
		byte[] expected = Files.readAllBytes(expectedFile.toPath());
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(packageService.getPackageInBytes(packageBag)).thenReturn(expected);
		
		MvcResult result = mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/" 
						+ packageBag.getName() + "_" +packageBag.getVersion() + ".tar.gz")
				.principal(principal)
				.accept("application/gzip"))
				.andExpect(status().isOk())
				.andReturn();
		
		assertEquals("Content disposition is not correct.", "attachment; filename= \"" 
				+ packageBag.getName() + "_" +packageBag.getVersion() + ".tar.gz\"",
				result.getResponse().getHeader("Content-Disposition"));
		assertArrayEquals("Returned file is not correct.", 
				expected, result.getResponse().getContentAsByteArray());
	}
	
	@Test
	public void downloadPage_Returns404_WhenPackageIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;

		when(packageService.findById(PACKAGE_ID)).thenReturn(null);
		
		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/test_1.5.0.tar.gz"))
		.andExpect(status().isNotFound());
	}
	
	@Test
	public void downloadPage_Returns404_WhenPackageSourceIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(packageService.getPackageInBytes(packageBag)).thenThrow(new FileNotFoundException());
		
		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/" 
						+ packageBag.getName() + "_" +packageBag.getVersion() + ".tar.gz"))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void downloadPage_Returns500_WhenFetchingSourceCausesAnException() throws Exception {
		final int PACKAGE_ID = 123;
		final String TEST_MESSAGE = "test123";
		
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		GetFileInBytesException expectedException = new GetFileInBytesException(
				messageSource, new Locale("en"), TEST_MESSAGE, TEST_MESSAGE);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(packageService.getPackageInBytes(packageBag)).thenThrow(expectedException);

		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/" 
						+ packageBag.getName() + "_" +packageBag.getVersion() + ".tar.gz"))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	public void downloadReferenceManual() throws Exception {
		final int PACKAGE_ID = 123;
		
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		Principal principal = getMockPrincipal(user);
		File expectedFile = new File(TEST_FILES + "/test_downloadReferenceManual/abc.pdf");
		
		if(!expectedFile.exists())
			fail("Test file not found!");
		
		byte[] expected = Files.readAllBytes(expectedFile.toPath());
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(packageService.getReferenceManualInBytes(packageBag)).thenReturn(expected);
		
		MvcResult result = mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/" + packageBag.getName() + ".pdf")
				.principal(principal)
				.accept(MediaType.APPLICATION_PDF))
				.andExpect(status().isOk())
				.andReturn();
		
		assertEquals("Content disposition is not correct.", "attachment; filename= \"" 
				+ packageBag.getName() + ".pdf\"",
				result.getResponse().getHeader("Content-Disposition"));
		assertArrayEquals("Returned file is not correct.", 
				expected, result.getResponse().getContentAsByteArray());
	}
	
	@Test
	public void downloadReferenceManual_Throws404_WhenPackageManualIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		
		User user = getUserAndAuthenticate();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(packageService.getReferenceManualInBytes(packageBag)).thenThrow(new FileNotFoundException());
		
		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/" + packageBag.getName() + ".pdf"))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void downloadReferenceManual_Throws404_WhenPackageIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;

		when(packageService.findById(PACKAGE_ID)).thenReturn(null);
		
		mockMvc.perform(
				get(URI_PREFIX + "/" + PACKAGE_ID + "/download/abc.pdf"))
		.andExpect(status().isNotFound());
	}
	
	//TODO: test downloadVignette()
	
	@Test
	@WithMockUser(username = "testuser", password = "pwd", roles = "admin")
	public void deactivatePackage() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = getExpectedJson(MessageType.SUCCESS, MessageCodes.SUCCESS_PACKAGE_DEACTIVATED);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doNothing().when(packageService).deactivatePackage(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/deactivate")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).deactivatePackage(packageBag, user);
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "pwd", roles = "user")
	public void deactivatePackage_Returns401_WhenUserIsNotAuthorized() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);

		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(false);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/deactivate")
				.principal(principal)).andExpect(status().isUnauthorized()).andReturn();
		
		assertEquals("Returned JSON is not correct", 
				expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void deactivatePackage_Returns401_WhenUserIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);

		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(null);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/deactivate")
				.principal(principal)).andExpect(status().isUnauthorized()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void deactivatePackage_Returns404_WhenPackageIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, "Package " + PACKAGE_ID + ": " + MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(null);

		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/deactivate")
				.principal(principal)).andExpect(status().isNotFound()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void deactivatePackage_ReturnsWarning_WhenPackageIsAlreadyActivated() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.WARNING, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.WARNING_PACKAGE_ALREADY_DEACTIVATED);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doThrow(new PackageAlreadyDeactivatedWarning(messageSource, new Locale("en"), packageBag))
		.when(packageService).deactivatePackage(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/deactivate")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).deactivatePackage(packageBag, user);
	}
	
	@Test
	public void deactivatePackage_Returns500_WhenPackageActivateExceptionIsThrown() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.ERROR_PACKAGE_DEACTIVATE);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doThrow(new PackageDeactivateException(messageSource, new Locale("en"), packageBag))
		.when(packageService).deactivatePackage(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/deactivate")
				.principal(principal)).andExpect(status().isInternalServerError()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).deactivatePackage(packageBag, user);
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "pwd", roles = "admin")
	public void activatePackage() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = getExpectedJson(MessageType.SUCCESS, MessageCodes.SUCCESS_PACKAGE_ACTIVATED);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doNothing().when(packageService).activatePackage(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/activate")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).activatePackage(packageBag, user);
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "pwd", roles = "user")
	public void activatePackage_Returns401_WhenUserIsNotAuthorized() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);

		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(false);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/activate")
				.principal(principal)).andExpect(status().isUnauthorized()).andReturn();
		
		assertEquals("Returned JSON is not correct", 
				expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void activatePackage_Returns401_WhenUserIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);

		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(null);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/activate")
				.principal(principal)).andExpect(status().isUnauthorized()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void activatePackage_Returns404_WhenPackageIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, "Package " + PACKAGE_ID + ": " + MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(null);

		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/activate")
				.principal(principal)).andExpect(status().isNotFound()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void activatePackage_ReturnsWarning_WhenPackageIsAlreadyActivated() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.WARNING, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.WARNING_PACKAGE_ALREADY_ACTIVATED);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doThrow(new PackageAlreadyActivatedWarning(messageSource, new Locale("en"), packageBag))
		.when(packageService).activatePackage(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/activate")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).activatePackage(packageBag, user);
	}
	
	@Test
	public void activatePackage_Returns500_WhenPackageActivateExceptionIsThrown() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.ERROR_PACKAGE_ACTIVATE);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doThrow(new PackageActivateException(messageSource, new Locale("en"), packageBag))
		.when(packageService).activatePackage(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				patch(URI_PREFIX + "/" + PACKAGE_ID + "/activate")
				.principal(principal)).andExpect(status().isInternalServerError()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).activatePackage(packageBag, user);
	}
	
	@Test
	public void deletePackage() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = getExpectedJson(MessageType.SUCCESS, MessageCodes.SUCCESS_PACKAGE_DELETED);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doNothing().when(packageService).delete(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/delete")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).delete(packageBag, user);
	}
	
	@Test
	@WithMockUser(username = "testuser", password = "pwd", roles = "user")
	public void deletePackage_Returns401_WhenUserIsNotAuthorized() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);

		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(false);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/delete")
				.principal(principal)).andExpect(status().isUnauthorized()).andReturn();
		
		assertEquals("Returned JSON is not correct", 
				expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void deletePackage_Returns401_WhenUserIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);

		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(null);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/delete") 
				.principal(principal)).andExpect(status().isUnauthorized()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void deletePackage_Returns404_WhenPackageIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, "Package " + PACKAGE_ID + ": " 
							+ MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		
		when(packageService.findById(PACKAGE_ID)).thenReturn(null);

		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/delete")
				.principal(principal)).andExpect(status().isNotFound()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void deletePackage_ReturnsWarning_WhenPackageIsAlreadyDeleted() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.WARNING, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.WARNING_PACKAGE_ALREADY_DELETED);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doThrow(new PackageAlreadyDeletedWarning(messageSource, new Locale("en"), packageBag))
		.when(packageService).delete(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/delete")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).delete(packageBag, user);
	}
	
	@Test
	public void deletePackage_Returns500_WhenPackageDeleteExceptionIsThrown() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.ERROR_PACKAGE_DELETE);

		when(packageService.findById(PACKAGE_ID)).thenReturn(packageBag);
		when(userService.findByLoginWithRepositoryMaintainers(user.getLogin())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doThrow(new PackageDeleteException(messageSource, new Locale("en"), packageBag))
		.when(packageService).delete(packageBag, user);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/delete")
				.principal(principal)).andExpect(status().isInternalServerError()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).delete(packageBag, user);
	}
	
	@Test
	public void shiftDeletePackage() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		packageBag.setDeleted(true);
		
		String expectedJson = getExpectedJson(MessageType.SUCCESS, MessageCodes.SUCCESS_PACKAGE_DELETED);

		when(packageService.findByIdAndDeleted(PACKAGE_ID, true)).thenReturn(packageBag);
		doNothing().when(packageService).shiftDelete(packageBag);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/sdelete")
				.principal(principal)).andExpect(status().isOk()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).shiftDelete(packageBag);
	}
	
	@Test
	public void shiftDeletePackage_Returns404_WhenPackageIsNotFound() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);

		Principal principal = getMockPrincipal(user);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, "Package " + PACKAGE_ID + ": " 
							+ MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		
		when(packageService.findByIdAndDeleted(PACKAGE_ID, true)).thenReturn(null);

		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/sdelete")
				.principal(principal)).andExpect(status().isNotFound()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
	}
	
	@Test
	public void shiftDeletePackage_Returns500_WhenPackageDeleteExceptionIsThrown() throws Exception {
		final int PACKAGE_ID = 123;
		final String USER_NAME = "testuser";
		
		User user = getUserAndAuthenticate();
		user.setName(USER_NAME);
		user.setLogin(USER_NAME);
		
		Principal principal = getMockPrincipal(user);
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		packageBag.setId(PACKAGE_ID);
		
		String expectedJson = 
				getExpectedJson(MessageType.ERROR, packageBag.getName() 
						+ " " + packageBag.getVersion() + ": " 
						+ MessageCodes.ERROR_PACKAGE_DELETE);

		when(packageService.findByIdAndDeleted(PACKAGE_ID, true)).thenReturn(packageBag);
		doThrow(new PackageDeleteException(messageSource, new Locale("en"), packageBag))
		.when(packageService).shiftDelete(packageBag);
		
		MvcResult result = mockMvc.perform(
				delete(URI_PREFIX + "/" + PACKAGE_ID + "/sdelete")
				.principal(principal)).andExpect(status().isInternalServerError()).andReturn();
		
		assertEquals("Returned JSON is not correct", expectedJson, result.getResponse().getContentAsString());
		verify(packageService, times(1)).shiftDelete(packageBag);
	}
}
