/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.python.test.strategy;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import eu.openanalytics.rdepot.test.context.TestWebApplicationContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

@ExtendWith(MockitoExtension.class)
public abstract class StrategyTest {
		
	@Mock
	protected PythonRepositoryService service;
	
	@Mock
	protected RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	protected PackageMaintainerService packageMaintainerService;
	
	@Mock
	protected BestMaintainerChooser bestMaintainerChooser;
	
	@Mock
	protected PythonPackageService packageService;
	
	@Mock
	protected PythonPackageDeleter deleter;
	
	@Mock
	protected PythonRepositorySynchronizer repositorySynchronizer;
	
	@Mock
	protected MessageSource messageSource;
	
	@Mock
	protected AccessTokenService accessTokenService;
	
	@BeforeEach
	public void setUp() {
		//This piece of code is used mainly to provide mock message source for static methods
		MockServletContext msc = new MockServletContext();
		msc.addInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, 
				TestWebApplicationContext.class.getName());
		ServletContextListener listener = new ContextLoaderListener();
		ServletContextEvent event = new ServletContextEvent(msc);
		listener.contextInitialized(event);
	    Mockito.lenient().when(messageSource.getMessage(any(), any(),any(), any())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String messageCode = invocation.getArgument(0);
				return messageCode;
			}
		});
		new StaticMessageResolver(messageSource);
	}
}