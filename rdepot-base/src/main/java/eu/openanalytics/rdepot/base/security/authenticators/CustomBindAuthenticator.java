/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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

import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import eu.openanalytics.rdepot.base.security.exceptions.RoleNotFoundException;
import eu.openanalytics.rdepot.base.security.exceptions.UserInactiveException;
import eu.openanalytics.rdepot.base.security.exceptions.UserSoftDeletedException;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.time.DateProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;

/**
 * Resolves provided credentials against {@link User users} existing in the db.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class CustomBindAuthenticator {
    private final Environment environment;
    protected final UserService userService;
    private final RoleService roleService;
    private final SecurityMediator securityMediator;

    /**
     * Resolves provided credentials against given authentication back-end.
     * If the user exists in the database, it will be checked
     * if their account is not inactive and/or deleted.
     * If the user is not found, though, their account will be created on the spot.
     * @param username login;
     *  it is checked as first when fetching the user from the database
     * @param useremail it is checked as second when fetching the user from the database
     * @param fullname name and surname
     * @param backendName name of the authentication back-end, e.g. oauth2
     * @throws AuthException if the account exists but was deactivated/soft-deleted
     *  or when a new user cannot be created
     */
    protected Collection<? extends GrantedAuthority> authenticate(
            String username, String useremail, String fullname, String backendName) throws AuthException {

        List<String> defaultAdmins = new ArrayList<>();

        for (int i = 0; ; i++) {
            String admin = environment.getProperty(String.format("app." + backendName + ".default.admins[%d]", i));
            if (admin == null) break;
            else defaultAdmins.add(admin);
        }

        if (defaultAdmins.isEmpty()) {
            defaultAdmins.add("admin");
        }
        if (useremail == null) {
            useremail = username + "@localhost";
        }

        User user;
        try {
            Optional<User> tmp;
            if ((tmp = userService.findActiveByLogin(username)).isPresent()) {
                user = tmp.get();
            } else if ((tmp = userService.findByEmail(useremail)).isPresent()) {
                user = tmp.get();
            } else {
                user = createNewUser(username, fullname, useremail, defaultAdmins.contains(username));
            }
        } catch (CreateEntityException e) {
            log.error(e.getMessage(), e);
            throw new AuthException(e.getMessageCode());
        }
        verifyAndUpdateUser(user, username, fullname, useremail, defaultAdmins.contains(username));

        user.setLastLoggedInOn(DateProvider.now());

        return securityMediator.getGrantedAuthorities(username);
    }

    private void verifyAndUpdateUser(User user, String login, String name, String email, boolean isAdmin)
            throws AuthException {
        if (user.isDeleted()) {
            throw new UserSoftDeletedException(user);
        }
        if (!user.isActive()) {
            throw new UserInactiveException(user);
        }
        if (!user.getName().equals(name)) {
            user.setName(name);
        }
        if (!user.getLogin().equals(login)) {
            user.setLogin(login);
        }
        if (!user.getEmail().equals(email)) {
            user.setEmail(email);
        }
        if (isAdmin && user.getRole().getValue() != Role.VALUE.ADMIN) {
            user.setRole(roleService.findByValue(Role.VALUE.ADMIN).orElseThrow(RoleNotFoundException::new));
        }
    }

    private User createNewUser(String login, String name, String email, boolean isAdmin)
            throws RoleNotFoundException, CreateEntityException {
        final User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setEmail(email);
        user.setActive(true);
        user.setDeleted(false);
        user.setCreatedOn(DateProvider.now());

        if (isAdmin) {
            user.setRole(roleService.findByValue(Role.VALUE.ADMIN).orElseThrow(RoleNotFoundException::new));
        } else {
            user.setRole(roleService.findByValue(Role.VALUE.USER).orElseThrow(RoleNotFoundException::new));
        }

        return userService.create(user);
    }
}
