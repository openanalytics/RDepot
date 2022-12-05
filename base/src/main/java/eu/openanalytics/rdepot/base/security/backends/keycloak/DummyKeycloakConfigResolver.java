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
package eu.openanalytics.rdepot.base.security.backends.keycloak;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnExpression(
		"!'${app.authentication}'.equals('keycloak')"
		)
public class DummyKeycloakConfigResolver extends KeycloakSpringBootConfigResolver {

	private KeycloakDeployment keycloakDeployment;
    
	public DummyKeycloakConfigResolver(Environment environment, AdapterConfig cfg) {		
		cfg.setRealm("dummy");
		cfg.setAuthServerUrl("dummy");
		cfg.setResource("dummy");
		cfg.setSslRequired("external");
		cfg.setPrincipalAttribute("dummy");
		cfg.setUseResourceRoleMappings(Boolean.valueOf("false"));
		Map<String,Object> credentials = new HashMap<>();
		credentials.put("secret", "dummy");
		cfg.setCredentials(credentials);
		keycloakDeployment = KeycloakDeploymentBuilder.build(cfg);
    }
	
	@Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        return keycloakDeployment;
    }
}
