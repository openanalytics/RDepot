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
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.spring.integration.test.annotation.SpringAnnotationConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import eu.openanalytics.rdepot.controller.ManagerController;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.test.config.MockRepositoryBeansConfig;
import eu.openanalytics.rdepot.test.config.TestPrincipal;
import eu.openanalytics.rdepot.test.config.WebApplicationTestConfig;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@RunWith(Arquillian.class)
@SpringAnnotationConfiguration(classes = {WebApplicationTestConfig.class, ManagerComponentTest.class, MockRepositoryBeansConfig.class})
@WebAppConfiguration
@Configuration
public class ManagerComponentTest extends BaseComponentTest {
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RepositoryRepository repositoryRepository;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Autowired
	private ManagerController managerController;
	
	@Test
	public void shouldManagerControllerBeNotNull() {
		assertNotNull(managerController);
	}
	
	@Test
	public void shouldReturnManager() {
		User testAdmin = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userRepository.findByLoginIgnoreCaseAndDeleted(testAdmin.getLogin(), false)).thenReturn(testAdmin);
		
		List<Repository> testRepositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(3);
		when(repositoryRepository.findByDeleted(eq(false), any())).thenReturn(testRepositories);
		
		Model testModel = new ExtendedModelMap();
		Principal testPrincipalUser = new TestPrincipal(testAdmin.getLogin());
		
		String manager = managerController.manager(testModel, testPrincipalUser);
		
		int expectedRole = testAdmin.getRole().getValue();
		
		assertEquals(expectedRole, testModel.asMap().get("role"));
		assertEquals(testRepositories, testModel.asMap().get("repositories"));
		assertNotNull(testModel.asMap().get("multiUploads"));
		assertEquals("manager", manager);
	}
}
