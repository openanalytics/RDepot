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

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.utils.TechnologyResolver;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class RepositorySpecs {

	public static <P extends Repository> Specification<P> isDeleted(boolean deleted) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("deleted"), deleted);
	}

	public static <P extends Repository> Specification<P> ofNameSearching(String name) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
	}
	
	public static <P extends Repository> Specification<P> ofName(String name) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name);
	}
	
	public static <P extends Repository> Specification<P> ofMaintainer(List<String> maintainers) {
		return (root, query, criteriaBuilder) -> {
			Join<Object, Object> rootMaintainer = root.join("repositoryMaintainers", JoinType.LEFT);
			return criteriaBuilder.in(rootMaintainer.get("user").get("name")).value(maintainers);
		};
	}
	
	public static <P extends Repository> Specification<P> ofTechnology(List<String> technologies) {
		return (root, query, criteriaBuilder) -> {
			TechnologyResolver technologyResolver = new TechnologyResolver();
			List<String> updatedTechnologies = technologyResolver.getTechnologies(technologies);
			return criteriaBuilder.in(root.get("resourceTechnology")).value(updatedTechnologies);
		};
	}
	
	public static <P extends Repository> Specification<P> isPublished(boolean published) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("published"), published);
	}
}
