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
package eu.openanalytics.rdepot.base.event.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;

/**
 * Class that simplifies building {@link Specification} to perform a search.
 */
public class NewsfeedEventSpecs {
	
	private static final Map<ResourceType, String> RESOURCE_TYPES = Map.of(
			ResourceType.PACKAGE_MAINTAINER, "packageMaintainer",
			ResourceType.REPOSITORY_MAINTAINER, "repositoryMaintainer",
			ResourceType.SUBMISSION, "submission",
			ResourceType.PACKAGE, "packageBag",
			ResourceType.REPOSITORY, "repository",
			ResourceType.USER, "user"
	);
	
	private static class RelatedResourceSpecification implements Specification<NewsfeedEvent> {

		private static final long serialVersionUID = -7861657472204796715L;
		protected final String resourceName;
		protected final Object value;
		protected final String property;
		
		public RelatedResourceSpecification(String resourceName, String property, Object value) {
			this.resourceName = resourceName;
			this.property = property;
			this.value = value;
		}
		
		@Override
		public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
				CriteriaBuilder criteriaBuilder) {
			return criteriaBuilder.equal(root.get(resourceName).get(property), value);
		}
		
	};
	
	private static class RelatedResourceNestedIdSpecification extends RelatedResourceSpecification {
		
		private static final long serialVersionUID = -8475467216624159313L;

		public RelatedResourceNestedIdSpecification(String resourceName, String property, Integer id) {
			super(resourceName, property, id);
		}
		
		@Override
		public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
				CriteriaBuilder cb) {
//			return criteriaBuilder.and(criteriaBuilder.isNotNull(root.get(resourceName)),
//					criteriaBuilder.equal(root.get(resourceName).get(property).get("id"), value));
			return cb.equal(
					root.join(resourceName, JoinType.LEFT)
						.join(property, JoinType.LEFT).get("id"), value);
		}
	}
	
	public static Specification<NewsfeedEvent> byUser(User user) {
		return new Specification<NewsfeedEvent>() {

			private static final long serialVersionUID = 841947770956144491L;

			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("author"), user);
			}
			
		};
	}
	
	private static List<Predicate> resourcePredicates(List<? extends Resource> entities, String fieldName, 
			CriteriaBuilder criteriaBuilder, Root<NewsfeedEvent> root){
		List<Predicate> predicates = new ArrayList<Predicate>();
		entities.forEach(resource -> {
			predicates.add(criteriaBuilder.equal(root.get(fieldName), resource.getId()));
		});
		return predicates;
	}
	
	public static Specification<NewsfeedEvent> isRepositoryMaintainer(User requester, List<RepositoryMaintainer> repositoryMaintainers, 
			List<PackageMaintainer> packageMaintainers, List<Repository<?,?>> rRepositories, List<Package<?,?>> packages, List<Submission> submissions){
		return new Specification<NewsfeedEvent>() {
			
			private static final long serialVersionUID = 1L;
			
			
			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
				CriteriaBuilder criteriaBuilder) {
				
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.addAll(resourcePredicates(repositoryMaintainers, "repositoryMaintainer", criteriaBuilder, root));
				predicates.addAll(resourcePredicates(packageMaintainers, "packageMaintainer", criteriaBuilder, root));
				predicates.addAll(resourcePredicates(rRepositories, "repository", criteriaBuilder, root));
				predicates.addAll(resourcePredicates(packages, "packageBag", criteriaBuilder, root));
				predicates.addAll(resourcePredicates(submissions, "submission", criteriaBuilder, root));
				predicates.add(criteriaBuilder.equal(root.get("author"), requester));

				
				return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}
	
	public static Specification<NewsfeedEvent> isPackageMaintainer(User requester,
			List<PackageMaintainer> packageMaintainers, List<Package<?,?>> rPackages, List<Submission> submissions) {
		return new Specification<NewsfeedEvent>() {
			
			private static final long serialVersionUID = 1L;
			
			
			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
				CriteriaBuilder criteriaBuilder) {
				
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.addAll(resourcePredicates(packageMaintainers, "packageMaintainer", criteriaBuilder, root));
				predicates.addAll(resourcePredicates(rPackages, "packageBag", criteriaBuilder, root));
				predicates.addAll(resourcePredicates(submissions, "submission", criteriaBuilder, root));
				predicates.add(criteriaBuilder.equal(root.get("author"), requester));

				
				return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}
	
	public static Specification<NewsfeedEvent> byDate(LocalDate date) {
		return new Specification<NewsfeedEvent>() {

			private static final long serialVersionUID = 841947770956144491L;

			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("date"), date);
			}
			
		};
	}
	
	public static Specification<NewsfeedEvent> byTime(LocalDateTime time) {
		return new Specification<NewsfeedEvent>() {

			private static final long serialVersionUID = 841947770956144491L;

			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.equal(root.get("time"), time);
			}
			
		};
	}
	
	public static Specification<NewsfeedEvent> hasRelatedResource(Resource resource) {
		return new RelatedResourceSpecification(RESOURCE_TYPES
				.get(resource.getResourceType()), "id", resource.getId());
	}
	
	public static Specification<NewsfeedEvent> hasResourceWithId(Integer id) {
		return hasResourceWithPropertyAndValueAndOneOfTypes("id", id, 
				"packageMaintainer",
				"repositoryMaintainer",
				"submission",
				"packageBag",
				"repository",
				"user");
	}
	
	private static Specification<NewsfeedEvent> notNullSpecification() {
		return new Specification<NewsfeedEvent>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return root.isNotNull();
			}
		};
	}
	
	public static Specification<NewsfeedEvent> hasResourceWithResourcePropertyAndOneOfTypes(String property, Resource resource, String...types) {
		Specification<NewsfeedEvent> spec = null;
		
		if(types.length == 0) {
			spec = notNullSpecification();
		} else {
			spec = new RelatedResourceNestedIdSpecification(types[0], property, resource.getId());
			
			for(int i = 1; i < types.length; i++) {
				spec = spec.or(new RelatedResourceNestedIdSpecification(types[i], property, resource.getId()));
			}
		}
		
		return spec;
	}
	
	public static Specification<NewsfeedEvent> hasResourceWithPropertyAndValueAndOneOfTypes(String property, Object value, String... types) {
		Specification<NewsfeedEvent> spec = null;
		
		if(types.length == 0) {
			spec = notNullSpecification();
		} else {
			spec = new RelatedResourceSpecification(types[0], property, value);
			
			for(int i = 1; i < types.length; i++) {
				spec = spec.or(new RelatedResourceSpecification(types[i], property, value));
			}
		}
		
		return spec;
	}
	
	public static Specification<NewsfeedEvent> isType(NewsfeedEventType newsfeedEventType) {
		return new Specification<NewsfeedEvent>() {

			private static final long serialVersionUID = 3189514620108228421L;

			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				
				return criteriaBuilder.equal(root.get("type"), newsfeedEventType);
			}
			
		};
	}
	
	public static Specification<NewsfeedEvent> hasResourceOfType(ResourceType resourceType) {
		return new Specification<NewsfeedEvent>() {

			private static final long serialVersionUID = 5332225922729224259L;

			@Override
			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.isNotNull(root
						.get(RESOURCE_TYPES.get(resourceType)));
			}
			
		};
	}

	public static Specification<NewsfeedEvent> isTechnology(Technology technology) {
		return hasResourceWithPropertyAndValueAndOneOfTypes("technology", technology, 
				"packageMaintainer",
				"repositoryMaintainer",
				"submission",
				"package",
				"repository",
				"user");
	}
	
//	public static Specification<NewsfeedEvent> hasId(int id) {
//		return new Specification<NewsfeedEvent>() {
//
//			private static final long serialVersionUID = 1808971030874989312L;
//
//			@Override
//			public Predicate toPredicate(Root<NewsfeedEvent> root, CriteriaQuery<?> query,
//					CriteriaBuilder criteriaBuilder) {
//				return criteriaBuilder.equal(root.get("id"), id);
//			}
//			
//		};
//	}

	public static Specification<NewsfeedEvent> relatedResourceHasRelatedRepository(
			Repository<?, ?> repository) {
		return hasResourceWithResourcePropertyAndOneOfTypes
				(
					"repository", 
					repository, 
					"packageMaintainer", 
					"repositoryMaintainer",
					"packageBag"
				)
			.or
				(
					hasResourceOfType(ResourceType.REPOSITORY)
					.and(hasRelatedResource(repository))
				);
	}


}
