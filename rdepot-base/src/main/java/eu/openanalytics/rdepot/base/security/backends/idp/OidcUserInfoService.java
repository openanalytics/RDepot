/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.base.security.backends.idp;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "oauth2.authorizationserver", havingValue = "true")
public class OidcUserInfoService {
    @Resource
    private Environment env;

    public OidcUserInfo loadUser(DefaultOidcUser oidcUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", oidcUser.getSubject());
        claims.put(env.getProperty("oauth2.login-field", "preferred_username"), oidcUser.getPreferredUsername());
        claims.put(env.getProperty("oauth2.full-name-field", "name"), oidcUser.getName());
        claims.put(env.getProperty("oauth2.email-field", "email"), oidcUser.getEmail());
        claims.put("username", oidcUser.getPreferredUsername());
        return new OidcUserInfo(claims);
    }
}
