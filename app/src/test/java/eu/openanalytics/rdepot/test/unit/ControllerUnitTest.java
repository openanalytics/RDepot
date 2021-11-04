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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

public abstract class ControllerUnitTest {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	protected static class MessageType {
		public static final String ERROR = "error";
		public static final String SUCCESS = "success";
		public static final String WARNING = "warning";
	}
	
	protected Authentication getMockAuthentication(User user) {
		Authentication authentication = mock(Authentication.class);
		
		when(authentication.getPrincipal()).thenReturn(null);
		when(authentication.getName()).thenReturn(user.getLogin());
		
		return authentication;
	}
	
	protected Principal getMockPrincipal(User user) {
		Principal mockPrincipal = mock(Principal.class);
		when(mockPrincipal.getName()).thenReturn(user.getLogin());
		
		
		return mockPrincipal;
	}
	
	protected ViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

	    viewResolver.setPrefix("classpath:templates/");
	    viewResolver.setSuffix(".html");

	    return viewResolver;
	}
	
	protected String getExpectedJson(String messageType, String message) throws JsonProcessingException {
		Map<String, String> resultMessage = new HashMap<>();
		resultMessage.put(messageType, message);
		String expectedJson = objectMapper.writeValueAsString(resultMessage);
		
		return expectedJson;
	}
	
	protected User getUserAndAuthenticate() {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		SecurityContextHolder.getContext().setAuthentication(getMockAuthentication(user));
		
		return user;
	}
}


