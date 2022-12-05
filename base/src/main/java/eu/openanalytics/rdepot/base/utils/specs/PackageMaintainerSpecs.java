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
package eu.openanalytics.rdepot.base.utils.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;

public class PackageMaintainerSpecs {
	public static Specification<PackageMaintainer> ofRepository(Repository<?,?> repository) {
		return new Specification<PackageMaintainer>() {

			private static final long serialVersionUID = -5268826389562559432L;

			@Override
			public Predicate toPredicate(Root<PackageMaintainer> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("repository"), repository);
			}
		};
	}
	
	public static Specification<PackageMaintainer> isDeleted(boolean deleted) {
		return new Specification<PackageMaintainer>() {

			private static final long serialVersionUID = -6334476911635252902L;

			@Override
			public Predicate toPredicate(Root<PackageMaintainer> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("deleted"), deleted);
			}
		};
	}
	
	public static Specification<PackageMaintainer> ofUser(User user) {
		return new Specification<PackageMaintainer>() {

			private static final long serialVersionUID = -5797105559251617670L;

			@Override
			public Predicate toPredicate(Root<PackageMaintainer> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("user"), user);
			}
		};
	}
}
