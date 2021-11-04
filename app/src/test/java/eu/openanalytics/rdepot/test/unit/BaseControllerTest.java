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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.WebRequest;

import eu.openanalytics.rdepot.api.v1.controller.BaseController;

@RunWith(MockitoJUnitRunner.class)
public class BaseControllerTest {
	
	@InjectMocks
	BaseController baseController;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testHandleUncaughtException() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		WebRequest request = Mockito.mock(WebRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		int scBadRequest = 400;
		String message = "message23492";
		Exception exception = new Exception(message);
		
		String responseText = baseController.handleUncaughtException(exception, request, response);
		
		assertEquals("Unknown error occurred: message23492", responseText);
		assertEquals("application/json", response.getHeader("Content-Type"));
		assertEquals(scBadRequest, response.getStatus());
	}
	
	@Test
	public void testHandleUncaughtExceptionWhenRequestIsNotAjax() throws IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		WebRequest request = Mockito.mock(WebRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn(null);
		int scInternalServerError = 500;
		String message = "message23492";
		Exception exception = new Exception(message);
		
		String responseText = baseController.handleUncaughtException(exception, request, response);
		
		assertNull(responseText);
		assertEquals(scInternalServerError, response.getStatus());
		assertEquals(message, response.getErrorMessage());
	}
	
	@Test
	public void testIndex() {
		UserDetails userDetails = Mockito.mock(UserDetails.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContext currentContext = SecurityContextHolder.getContext();
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
		
		String page = baseController.index();
		assertEquals("redirect:/manager", page);
		
		SecurityContextHolder.setContext(currentContext);
	}
	
//	@Test
//	public void testIndexWhenPrincipalIsNull() {
//		Authentication authentication = Mockito.mock(Authentication.class);
//		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
//		SecurityContext currentContext = SecurityContextHolder.getContext();
//		SecurityContextHolder.setContext(securityContext);
//		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(null);
//		
//		String page = baseController.index();
//		assertEquals("redirect:/login", page);
//		
//		SecurityContextHolder.setContext(currentContext);
//	}
//	
//	@Test
//	public void testIndexWhenPrincipalIsString() {
//		Authentication authentication = Mockito.mock(Authentication.class);
//		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
//		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
//		SecurityContext currentContext = SecurityContextHolder.getContext();
//		SecurityContextHolder.setContext(securityContext);
//		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("");
//		
//		String page = baseController.index();
//		assertEquals("redirect:/login", page);
//		
//		SecurityContextHolder.setContext(currentContext);
//	}
}
