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
package eu.openanalytics.rdepot.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;

@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, Integer> 
{
	public User findByLoginIgnoreCase(String login);
	public User findByEmail(String email);
	public List<User> findByRole(Role role);
	public List<User> findByRole(Role role, Sort sort);
	public List<User> findByDeleted(boolean deleted, Sort sort);
	public User findByIdAndDeleted(int id, boolean deleted);
	public User findByLoginIgnoreCaseAndDeleted(String login, boolean deleted);
	public User findByEmailAndDeleted(String email, boolean deleted);
	public List<User> findByRoleAndDeleted(Role role, boolean deleted);
	public List<User> findByRoleAndActiveAndDeleted(Role adminRole, boolean active, boolean deleted);
}
