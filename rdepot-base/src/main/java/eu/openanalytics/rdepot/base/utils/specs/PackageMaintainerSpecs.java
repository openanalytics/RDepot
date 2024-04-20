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

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.utils.TechnologyResolver;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PackageMaintainerSpecs {
	public static Specification<PackageMaintainer> ofRepository(List<String> repositories) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("repository").get("name")).value(repositories);
	}
	
	public static Specification<PackageMaintainer> isDeleted(boolean deleted) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("deleted"), deleted);
	}
	
	public static Specification<PackageMaintainer> ofTechnology(List<String> technologies) {
		return (root, query, criteriaBuilder) -> {
			TechnologyResolver technologyResolver = new TechnologyResolver();
			List<String> updatedTechnologies = technologyResolver.getTechnologies(technologies);
			return criteriaBuilder.in(root.get("repository").get("resourceTechnology")).value(updatedTechnologies);
		};
	}
	
	public static Specification<PackageMaintainer> byMaintainer(String maintainer) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("name")), "%" + maintainer.toLowerCase() + "%");
	}
	
	public static Specification<PackageMaintainer> ofPackageName(String packageName) {
		return (root, query, criteriaBuilder) -> 
			criteriaBuilder.like(criteriaBuilder.lower(root.get("packageName")), "%" + packageName.toLowerCase() + "%");
	}
}
