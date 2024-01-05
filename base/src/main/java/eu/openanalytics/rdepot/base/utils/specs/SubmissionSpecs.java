/**
 * R Depot
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

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;

public class SubmissionSpecs {
	public static Specification<Submission> ofState(SubmissionState state) {
		return new Specification<Submission>() {

			private static final long serialVersionUID = 3662191641957849820L;

			@Override
			public Predicate toPredicate(Root<Submission> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("state"), state);
			}
		};
	}
	
	public static Specification<Submission> ofUser(User user) {
		return new Specification<Submission>() {

			private static final long serialVersionUID = -1603347549726878112L;

			@Override
			public Predicate toPredicate(Root<Submission> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("user"), user);
			}
		};
	}
	
	public static <T extends Package<T, ?>> Specification<Submission> ofPackage(T packageBag) {
		return new Specification<Submission>() {

			private static final long serialVersionUID = -5096233901046313341L;

			@Override
			public Predicate toPredicate(Root<Submission> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("packageBag"), packageBag);
			}
		};
	}
}
