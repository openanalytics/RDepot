/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.utils.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;

public class RepositoryMaintainerSpecs {
	public static Specification<RepositoryMaintainer> ofRepository(Repository<?,?> repository) {
		return new Specification<RepositoryMaintainer>() {

			private static final long serialVersionUID = -212096479559321556L;

			@Override
			public Predicate toPredicate(Root<RepositoryMaintainer> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("repository"), repository);
			}
		};
	}
	
	public static Specification<RepositoryMaintainer> isDeleted(boolean deleted) {
		return new Specification<RepositoryMaintainer>() {

			private static final long serialVersionUID = -8823081061049392401L;

			@Override
			public Predicate toPredicate(Root<RepositoryMaintainer> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("deleted"), deleted);
			}
		};
	}
	
	public static Specification<RepositoryMaintainer> ofUser(User user) {
		return new Specification<RepositoryMaintainer>() {

			private static final long serialVersionUID = -3276671709477951716L;

			@Override
			public Predicate toPredicate(Root<RepositoryMaintainer> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("user"), user);
			}
		};
	}
}
