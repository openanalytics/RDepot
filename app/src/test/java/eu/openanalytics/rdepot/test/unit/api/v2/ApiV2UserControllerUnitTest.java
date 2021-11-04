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
package eu.openanalytics.rdepot.test.unit.api.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.controller.ApiV2UserController;
import eu.openanalytics.rdepot.api.v2.dto.UserDto;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.context.TestConfig;
import eu.openanalytics.rdepot.test.fixture.RoleFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;


@ContextConfiguration(classes = {TestConfig.class})
@WebMvcTest(ApiV2UserController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({TestConfig.class})
public class ApiV2UserControllerUnitTest extends ApiV2ControllerUnitTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	WebApplicationContext webApplicationContext;
	
	@Test
	public void getAllUsers() throws Exception {
		User user = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(user);
		
		List<User> users = new ArrayList<>();
		users.add(user);
		users.addAll(UserTestFixture.GET_FIXTURE_USERS(1, 1, 1, 1));
		
		when(userService.findAll(any(Pageable.class))).thenReturn(new PageImpl<User>(users));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_USERS_PATH))));
	}
	
	@Test
	public void getAllUsers_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getAllUsers_returns403_whenUserIsNotAuthorized() throws Exception {
		User user = getUserAndAuthenticate(userService);
		Principal principal = getMockPrincipal(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isForbidden())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getUser() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		
		User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.findByLogin(principal.getName())).thenReturn(admin);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + user.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_USER_PATH))));
	}
	
	@Test
	public void getUser_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/3")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getUser_returns403_whenUserIsNeitherAdminNorTheSameUser() throws Exception {
		User notAdmin = getUserAndAuthenticate(userService);
		Principal principal = getMockPrincipal(notAdmin);
		
		User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.findByLogin(principal.getName())).thenReturn(notAdmin);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + user.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isForbidden())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getUser_returns200_whenRequesterIsTheRequestedUser() throws Exception {
		User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		Principal principal = getMockPrincipal(user);
		
		authenticate(user);
		
		when(userService.isAdmin(user)).thenReturn(false);
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + user.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_USER_PATH))));
	}
	
	@Test
	public void getUser_returns404_whenUserIsNotFound() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		final Integer ID = 123;
		
		when(userService.findById(ID)).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(admin);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isNotFound())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_USER_NOT_FOUND_PATH))));		
	}
	
	@Test
	public void getRoles_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/roles")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getRoles_returns403_whenUserIsNotAuthorized() throws Exception {
		User user = getUserAndAuthenticate(userService);
		Principal principal = getMockPrincipal(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/roles")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isForbidden())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getToken_returns403_whenUserIsNeitherAdminNorTheSameUser() throws Exception {
		User notAdmin = getUserAndAuthenticate(userService);
		Principal principal = getMockPrincipal(notAdmin);
		
		User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.findByLogin(principal.getName())).thenReturn(notAdmin);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + user.getId() + "/token")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isForbidden())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getToken_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/123/token")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getToken_returns404_whenUserIsNotFound() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		final Integer ID = 123;
		
		when(userService.findByLogin(principal.getName())).thenReturn(admin);
		when(userService.findById(ID)).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + ID + "/token")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isNotFound())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_USER_NOT_FOUND_PATH))));		
	}
	
	@Test
	public void patchUser_returns401_whenUserIsNotAuthenticated() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":false}]";
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/users/123")
					.contentType("application/json-patch+json")
					.content(patchJson))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void patchUser_returns403_whenUserIsNotAuthorized() throws Exception {
		User notAdmin = getUserAndAuthenticate(userService);
		Principal principal = getMockPrincipal(notAdmin);
		
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":false}]";
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/users/123")
					.contentType("application/json-patch+json")
					.principal(principal)
					.content(patchJson))
			.andExpect(
					status()
					.isForbidden())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void patchUser_returns404_whenUserIsNotFound() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		final Integer ID = 123;
		
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":false}]";
		when(userService.findByLogin(principal.getName())).thenReturn(admin);
		when(userService.findById(ID)).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/users/" + ID)
					.contentType("application/json-patch+json")
					.principal(principal)
					.content(patchJson))
			.andExpect(
					status()
					.isNotFound())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_USER_NOT_FOUND_PATH))));		
	}
	
	@Test
	public void patchUser() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":false}]";
		User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.findByLogin(principal.getName())).thenReturn(admin);
		when(userService.evaluateAndUpdate(any(), eq(admin))).thenAnswer(new Answer<User>() {

			@Override
			public User answer(InvocationOnMock invocation) throws Throwable {
				UserDto dto = (UserDto)invocation.getArgument(0);
				
				assertEquals(false, dto.isActive());
				user.setActive(false);
				return user;
			}
			
		});
		doNothing().when(userValidator).validate(any(), any());
		when(userValidator.supports(User.class)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/users/" + user.getId())
					.contentType("application/json-patch+json")
					.content(patchJson).principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_USER_PATCHED_PATH))));
	}
	
	@Test
	public void getRoles() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		
		when(roleService.findAll()).thenReturn(RoleFixture.GET_FIXTURE_ROLES(1, 1, 1, 1));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/roles")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_ROLES_PATH))));
	}
	
	@Test
	public void getToken() throws Exception {
		User admin = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(admin);
		
		final User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		final String EXAMPLE_TOKEN = "S3CR3TT0K3N";
		
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.findByLogin(principal.getName())).thenReturn(admin);
		when(userService.generateToken(user.getLogin())).thenReturn(EXAMPLE_TOKEN);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + user.getId() + "/token")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_TOKEN_PATH))));
	}
	
	@Test
	public void getToken_whenRequesterIsTheRequestedUser() throws Exception {
		User user = UserTestFixture.GET_FIXTURE_USERS(0, 0, 1, 3).get(0);
		Principal principal = getMockPrincipal(user);
		
		authenticate(user);
		
		final String EXAMPLE_TOKEN = "S3CR3TT0K3N";
		
		when(userService.isAdmin(user)).thenReturn(false);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.findById(user.getId())).thenReturn(user);
		when(userService.generateToken(user.getLogin())).thenReturn(EXAMPLE_TOKEN);

		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/users/" + user.getId() + "/token")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_TOKEN_PATH))));
	}
}