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
package eu.openanalytics.rdepot.base.security.authenticators;

import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import java.util.Collection;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@ComponentScan("eu.openanalytics.rdepot")
@Service
@Transactional
@ConditionalOnProperty(value = "app.authentication", havingValue = "oauth2")
public class Oauth2CustomBindAuthenticator extends CustomBindAuthenticator {

    public Oauth2CustomBindAuthenticator(
            Environment environment,
            UserService userService,
            RoleService roleService,
            SecurityMediator securityMediator) {
        super(environment, userService, roleService, securityMediator);
    }

    public Collection<? extends GrantedAuthority> authenticate(String login, String email, String fullname)
            throws AuthException {
        return super.authenticate(login, email, fullname, "oauth2");
    }
}
