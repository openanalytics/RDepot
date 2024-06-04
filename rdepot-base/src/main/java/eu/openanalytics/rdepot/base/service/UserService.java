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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.daos.UserDao;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import java.util.List;
import java.util.Optional;

/**
 * Reads and write {@link User users} from/into the database.
 */
@org.springframework.stereotype.Service
public class UserService extends Service<User> {

    private final UserDao dao;

    private final RoleService roleService;

    public UserService(UserDao dao, RoleService roleService) {
        super(dao);
        this.dao = dao;
        this.roleService = roleService;
    }

    /**
     * @return active users by the given role
     */
    public List<User> findByRole(Role role) {
        return dao.findByRoleAndDeletedAndActive(role, false, true);
    }

    /**
     * @return unique user by the given login
     */
    public Optional<User> findByLogin(String login) {
        return dao.findByLogin(login);
    }

    /**
     * @return unique user by the given e-mail address
     */
    public Optional<User> findByEmail(String email) {
        return dao.findByEmail(email);
    }

    /**
     * @return true if given user has admin rights
     */
    public boolean isAdmin(User user) {
        return user.getRole().getValue() == Role.VALUE.ADMIN;
    }

    /**
     * Fetches first admin found in the system.
     * No assumptions should be made about admin priority,
     * unless there is an overriding implementation.
     * @return admin
     */
    public User findFirstAdmin() throws AdminNotFound {
        Optional<Role> role = roleService.findByValue(Role.VALUE.ADMIN);
        if (role.isEmpty()) throw new AdminNotFound();
        List<User> admins = findByRole(role.get());
        if (admins.isEmpty()) throw new AdminNotFound();
        else return admins.get(0);
    }
}
