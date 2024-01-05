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

import eu.openanalytics.rdepot.base.entities.Repository;

public class RepositorySpecs {
	
	public static <P extends Repository<P, ?>> Specification<P> isDeleted(boolean deleted) {
		return new Specification<P>() {

			private static final long serialVersionUID = -9006313241412538456L;

			@Override
			public Predicate toPredicate(Root<P> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("deleted"), deleted);
			}
			
		};
	}
	
	public static <P extends Repository<P, ?>> Specification<P> ofName(String name) {
		return new Specification<P>() {


			private static final long serialVersionUID = 5999133086687443956L;

			@Override
			public Predicate toPredicate(Root<P> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("name"), name);
			}
			
		};
	}
}
