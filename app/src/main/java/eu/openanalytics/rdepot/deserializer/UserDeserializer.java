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
package eu.openanalytics.rdepot.deserializer;

import java.io.IOException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;

/**
 * This class is responsible for User object deserialization
 */
@Component
public class UserDeserializer extends StdDeserializer<User> {

	private static final long serialVersionUID = -177937409371835251L;

	UserService userService;
	
	RoleService roleService;
	
	@Autowired
	MessageSource messageSource;
	
	Locale locale = LocaleContextHolder.getLocale();
	
	public UserDeserializer() {
		this(User.class);
	}
	
	@Autowired
	public UserDeserializer(UserService userService, RoleService roleService) {
		this();
		this.userService = userService;
		this.roleService = roleService;
	}
	
	public UserDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public User deserialize(JsonParser p, DeserializationContext ctxt) 
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);
		
		Integer id = node.has("id") ? node.get("id").asInt() : null;
		String login = node.has("login") ? node.get("login").asText() : null;
		String email = node.has("email") ? node.get("email").asText() : null;
		Boolean active = node.has("active") ? node.get("active").asBoolean() : null;
		String roleName = node.has("role") ? node.get("role").asText() : null;
		
		User presentUser = null;
		if(login != null) {
			presentUser = userService.findByLogin(login);
		} else if(email != null) {
			presentUser = userService.findByEmail(email);
		}
		
		User user = new User();
		if(presentUser != null) {
			user.setId(presentUser.getId());
			user.setLogin(presentUser.getLogin());
			user.setDeleted(presentUser.isDeleted());
			user.setName(presentUser.getName());
			user.setEmail(email);
		} else {
			user.setId(id);
			user.setLogin(login);
			user.setEmail(email);
		}
		
		Role role = null;
		if(roleName != null) {
			role = roleService.findByName(roleName);
		}
		
		user.setActive(active);
		user.setRole(role);
		
		return user;
	}

}
