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
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.openanalytics.rdepot.controller.ManagerController;
import eu.openanalytics.rdepot.model.MultiUploadRequest;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class ManagerControllerTest {
	
	@InjectMocks
	ManagerController managerController;
	
	@Mock
	RepositoryService repositoryService;
	
	@Mock
	UserService userService;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		Locale locale = new Locale("english");
		LocaleContextHolder.setLocale(locale);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testManager() {
		User user = new User(123, new Role(1, 3, "admin", "Administrator"), "Albert Einstein", "einstein@example.org", "einstein", true, false);
		Principal mockPrincipal = Mockito.mock(Principal.class);
		Mockito.when(mockPrincipal.getName()).thenReturn("einstein");
		Mockito.when(userService.findByLogin("einstein")).thenReturn(user);
		
		Repository repository1 = new Repository(123, "someuri", "repo1", "address", true, false);
		Repository repository2 = new Repository(234, "someuri", "repo1", "address", true, false);
		Repository repository3 = new Repository(345, "someuri", "repo1", "address", true, false);
		List<Repository> repositories = new ArrayList<>();
		repositories.add(repository1);
		repositories.add(repository2);
		repositories.add(repository3);
		Mockito.when(repositoryService.findAll()).thenReturn(repositories);

		Model model = new ExtendedModelMap();
		String page = managerController.manager(model, mockPrincipal);
		
		
		assertEquals("manager", page);
		assertEquals(3, (int)model.asMap().get("role"));
		assertEquals(repositories, (List<Repository>)model.asMap().get("repositories"));
		assertTrue(model.asMap().get("multiUploads") instanceof MultiUploadRequest);
	}
}
