/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.mapper;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

public class NameMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(NameMapper.class); 
	
	private static Environment environment;
	
	private static Map<String, String> fields;
	
	private static String pattern;

	public static String getName(Environment env, AccessToken token) {
		environment = env;
		fields = new HashMap<>();
		fields.put("fullName", token.getName());
		fields.put("givenName", token.getGivenName());
		fields.put("middleName", token.getMiddleName());
		fields.put("familyName", token.getFamilyName());
		fields.put("preferredUsername", token.getPreferredUsername());
		fields.put("nickName", token.getNickName());
		
		token.getOtherClaims().forEach((k, v) -> fields.put(k, String.valueOf(v)));
		
		pattern = environment.getProperty("app.keycloak.name-mapping", "givenName familyName");
		return process();
	}
	
	public static String getName(Environment env, OidcIdToken token) {
		environment = env;		
		fields = new HashMap<>();
		fields.put("fullName", token.getFullName());
		fields.put("givenName", token.getGivenName());
		fields.put("middleName", token.getMiddleName());
		fields.put("familyName", token.getFamilyName());
		fields.put("preferredUsername", token.getPreferredUsername());
		fields.put("nickName", token.getNickName());	
		
		token.getClaims().forEach((k, v) -> fields.put(k, String.valueOf(v)));
		
		pattern = environment.getProperty("app.openid.name-mapping", "givenName familyName");
		return process();
	}
	
	private static String process() {
		String result = "";		
		pattern = pattern.replace("{", "").replace("}", "");
		String[] requestedFields = pattern.split(" ");
		int i = 0;
		result += chooseField(requestedFields[i]);
		i++;
		for(; i < requestedFields.length; i++) {
			result += " ";
			result += chooseField(requestedFields[i]);
		}				
		return result;
	}
	
	private static String chooseField(String arg) {
		if(fields.containsKey(arg)) {
			return fields.get(arg);
		} else {
			logger.error("There's no such claim: " + arg);
			return "";
		}
	}
}
