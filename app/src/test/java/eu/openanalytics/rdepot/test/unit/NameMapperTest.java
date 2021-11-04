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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.keycloak.representations.AccessToken;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import eu.openanalytics.rdepot.mapper.NameMapper;

public class NameMapperTest {

	@Test
	public void useDefaultMappingsWithKeycloakToken() {
		MockEnvironment env = new MockEnvironment();
		AccessToken token = new AccessToken();
		token.setGivenName("Albert");
		token.setFamilyName("Einstein");
		
		String actual = NameMapper.getName(env, token);
		assertEquals("Albert Einstein", actual);
	}
	
	@Test
	public void useCustomMappingsWithKeycloakToken() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("app.keycloak.name-mapping", "{firstName} {lastName}");
		AccessToken token = new AccessToken();
		token.setGivenName("Albert");
		token.setFamilyName("Einstein");
		token.setOtherClaims("firstName", "firstName");
		token.setOtherClaims("lastName", "lastName");
		
		String actual = NameMapper.getName(env, token);
		assertEquals("firstName lastName", actual);
	}
	
	@Test
	public void useDefaultMappingsWithOIDCToken() {
		MockEnvironment env = new MockEnvironment();		
		Map<String, Object> claims = new HashMap<>();
		claims.put("given_name", "Albert");
		claims.put("family_name", "Einstein");
		OidcIdToken token = new OidcIdToken("123", null, null, claims);
		
		String actual = NameMapper.getName(env, token);
		assertEquals("Albert Einstein", actual);
	}
	
	@Test
	public void useCustomMappingsWithOIDCToken() {
		MockEnvironment env = new MockEnvironment();
		env.setProperty("app.openid.name-mapping", "{firstName} {lastName}");		
		Map<String, Object> claims = new HashMap<>();
		claims.put("given_name", "Albert");
		claims.put("family_name", "Einstein");
		claims.put("firstName", "firstName");
		claims.put("lastName", "lastName");
		OidcIdToken token = new OidcIdToken("123", null, null, claims);
		
		String actual = NameMapper.getName(env, token);
		assertEquals("firstName lastName", actual);
	}
}
