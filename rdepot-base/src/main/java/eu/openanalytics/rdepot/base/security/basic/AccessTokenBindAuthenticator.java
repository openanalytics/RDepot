/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.security.basic;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authenticators.CustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import java.util.Collection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@ComponentScan("eu.openanalytics.rdepot")
@Service
@Transactional
public class AccessTokenBindAuthenticator extends CustomBindAuthenticator {

    private final AccessTokenService accessTokenService;

    public AccessTokenBindAuthenticator(
            Environment environment,
            UserService userService,
            RoleService roleService,
            AccessTokenService accessTokenService,
            SecurityMediator securityMediator) {
        super(environment, userService, roleService, securityMediator);
        this.accessTokenService = accessTokenService;
    }

    public Collection<? extends GrantedAuthority> authenticate(String username, String accessToken, String type)
            throws AuthException {
        User user = userService.findActiveByLogin(username).orElseThrow(() -> new AuthException("user.notfound"));

        if (accessTokenService.verifyToken(accessToken, user.getId())) {
            return super.authenticate(user.getLogin(), user.getEmail(), user.getName(), type);
        }
        throw new AuthException("invalid.access.token");
    }
}
