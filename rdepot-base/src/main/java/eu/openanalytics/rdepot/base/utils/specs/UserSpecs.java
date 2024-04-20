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
package eu.openanalytics.rdepot.base.utils.specs;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import eu.openanalytics.rdepot.base.entities.User;

/**
 * Used to build {@link Specification} to fetch 
 * {@link User Users} from {@link JpaRepository DAOs}.
 */
public class UserSpecs {
	public static Specification<User> ofRole(List<String> role) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("role").get("name")).value(role);
	}
	
	public static Specification<User> isActive(boolean active) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), active);
	}
	
	public static Specification<User> byName(String name) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
	}
	
	public static Specification<User> byLogin(String login) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.like(criteriaBuilder.lower(root.get("login")), "%" + login.toLowerCase() + "%");
	}
	
	public static Specification<User> byEmail(String email) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
	}
}
