/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.utils.TechnologyResolver;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * Class that simplifies building {@link Specification} to perform a search.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsfeedEventSpecs {

    private static final Map<ResourceType, String> RESOURCE_TYPES = Map.of(
            ResourceType.PACKAGE_MAINTAINER, "packageMaintainer",
            ResourceType.REPOSITORY_MAINTAINER, "repositoryMaintainer",
            ResourceType.SUBMISSION, "submission",
            ResourceType.PACKAGE, "packageBag",
            ResourceType.REPOSITORY, "repository",
            ResourceType.USER, "user",
            ResourceType.ACCESS_TOKEN, "accessToken");

    private static final String AUTHOR = "author";
    private static final String RESOURCE_TECHNOLOGY = "resourceTechnology";

    private static class RelatedResourceSpecification implements Specification<NewsfeedEvent> {

        @Serial
        private static final long serialVersionUID = -7861657472204796715L;

        protected final String resourceName;

        @Getter
        protected transient Object value;

        protected final String property;

        public RelatedResourceSpecification(String resourceName, String property, Object value) {
            this.resourceName = resourceName;
            this.property = property;
            this.value = value;
        }

        @Override
        public Predicate toPredicate(
                Root<NewsfeedEvent> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            return criteriaBuilder.equal(root.get(resourceName).get(property), value);
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
            out.defaultWriteObject();
            out.writeObject(getValue());
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            value = in.readObject();
        }
    }

    private static Specification<NewsfeedEvent> notNullSpecification() {
        return (root, query, criteriaBuilder) -> root.isNotNull();
    }

    public static Specification<NewsfeedEvent> byUser(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(AUTHOR), user);
    }

    public static Specification<NewsfeedEvent> byUserName(List<String> userNames) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get(AUTHOR).get("name")).value(userNames);
    }

    public static Specification<NewsfeedEvent> byPackageNames(List<String> packageNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT)
                        .get("name"))
                .value(packageNames);
    }

    public static Specification<NewsfeedEvent> byPackageNamesAndVersions(
            List<String> packageNames, List<String> packageVersions) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackages = root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.in(joinPackages.get("name")).value(packageNames));
            predicates.add(criteriaBuilder.in(joinPackages.get("version")).value(packageVersions));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> bySubmissionAndPackageNames(List<String> packageNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.SUBMISSION), JoinType.LEFT)
                        .join("packageBag", JoinType.LEFT)
                        .get("name"))
                .value(packageNames);
    }

    public static Specification<NewsfeedEvent> bySubmissionAndPackageNamesAndVersions(
            List<String> packageNames, List<String> packageVersions) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackages = root.join(RESOURCE_TYPES.get(ResourceType.SUBMISSION), JoinType.LEFT)
                    .join("packageBag", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.in(joinPackages.get("name")).value(packageNames));
            predicates.add(criteriaBuilder.in(joinPackages.get("version")).value(packageVersions));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> byPackageMaintainerAndPackageNames(List<String> packageNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER), JoinType.LEFT)
                        .get("packageName"))
                .value(packageNames);
    }

    public static Specification<NewsfeedEvent> byPackageNamesAndRepositoryNames(
            List<String> packageNames, List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackages = root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT);
            Join<?, ?> joinRepositories = joinPackages.join("repositoryGeneric", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.in(joinPackages.get("name")).value(packageNames));
            predicates.add(criteriaBuilder.in(joinRepositories.get("name")).value(repositoryNames));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> byPackageNamesAndVersionsAndRepositoryNames(
            List<String> packageNames, List<String> packageVersions, List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackages = root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT);
            Join<?, ?> joinRepositories = joinPackages.join("repositoryGeneric", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.in(joinPackages.get("name")).value(packageNames));
            predicates.add(criteriaBuilder.in(joinPackages.get("version")).value(packageVersions));
            predicates.add(criteriaBuilder.in(joinRepositories.get("name")).value(repositoryNames));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> bySubmissionAndPackageNamesAndRepositoryNames(
            List<String> packageNames, List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackages = root.join(RESOURCE_TYPES.get(ResourceType.SUBMISSION), JoinType.LEFT)
                    .join("packageBag", JoinType.LEFT);
            Join<?, ?> joinRepositories = joinPackages.join("repositoryGeneric", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.in(joinPackages.get("name")).value(packageNames));
            predicates.add(criteriaBuilder.in(joinRepositories.get("name")).value(repositoryNames));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> bySubmissionAndPackageNamesAndVersionsAndRepositoryNames(
            List<String> packageNames, List<String> packageVersions, List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackages = root.join(RESOURCE_TYPES.get(ResourceType.SUBMISSION), JoinType.LEFT)
                    .join("packageBag", JoinType.LEFT);
            Join<?, ?> joinRepositories = joinPackages.join("repositoryGeneric", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.in(joinPackages.get("name")).value(packageNames));
            predicates.add(criteriaBuilder.in(joinPackages.get("version")).value(packageVersions));
            predicates.add(criteriaBuilder.in(joinRepositories.get("name")).value(repositoryNames));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> byPackageMaintainerAndPackageNamesAndRepositoryNames(
            List<String> packageNames, List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinPackageMaintainers =
                    root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER), JoinType.LEFT);
            Join<?, ?> joinRepositories = joinPackageMaintainers.join("repository", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder
                    .in(joinPackageMaintainers.get("packageName"))
                    .value(packageNames));
            predicates.add(criteriaBuilder.in(joinRepositories.get("name")).value(repositoryNames));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> byRepositoryNames(List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.REPOSITORY), JoinType.LEFT)
                        .get("name"))
                .value(repositoryNames);
    }

    public static Specification<NewsfeedEvent> byPackageAndRepositoryNames(List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT)
                        .join("repositoryGeneric", JoinType.LEFT)
                        .get("name"))
                .value(repositoryNames);
    }

    public static Specification<NewsfeedEvent> byPackageMaintainerAndRepositoryNames(List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER), JoinType.LEFT)
                        .join("repository", JoinType.LEFT)
                        .get("name"))
                .value(repositoryNames);
    }

    public static Specification<NewsfeedEvent> byRepositoryMaintainerAndRepositoryNames(List<String> repositoryNames) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.join(RESOURCE_TYPES.get(ResourceType.REPOSITORY_MAINTAINER), JoinType.LEFT)
                        .join("repository", JoinType.LEFT)
                        .get("name"))
                .value(repositoryNames);
    }

    public static Specification<NewsfeedEvent> ofType(List<NewsfeedEventType> newsfeedEventType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("type")).value(newsfeedEventType);
    }

    public static Specification<NewsfeedEvent> fromDate(Instant fromDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("time"), fromDate);
    }

    public static Specification<NewsfeedEvent> toDate(Instant toDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("time"), toDate);
    }

    public static Specification<NewsfeedEvent> hasRelatedResource(Resource resource) {
        return new RelatedResourceSpecification(RESOURCE_TYPES.get(resource.getResourceType()), "id", resource.getId());
    }

    public static Specification<NewsfeedEvent> isRepositoryMaintainer(
            User requester,
            List<RepositoryMaintainer> repositoryMaintainers,
            List<PackageMaintainer> packageMaintainers,
            List<Repository> rRepositories,
            List<Package> packages,
            List<Submission> submissions) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.addAll(resourcePredicates(
                    repositoryMaintainers,
                    RESOURCE_TYPES.get(ResourceType.REPOSITORY_MAINTAINER),
                    criteriaBuilder,
                    root));
            predicates.addAll(resourcePredicates(
                    packageMaintainers, RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER), criteriaBuilder, root));
            predicates.addAll(resourcePredicates(
                    rRepositories, RESOURCE_TYPES.get(ResourceType.REPOSITORY), criteriaBuilder, root));
            predicates.addAll(
                    resourcePredicates(packages, RESOURCE_TYPES.get(ResourceType.PACKAGE), criteriaBuilder, root));
            predicates.addAll(resourcePredicates(
                    submissions, RESOURCE_TYPES.get(ResourceType.SUBMISSION), criteriaBuilder, root));
            predicates.add(criteriaBuilder.equal(root.get(AUTHOR), requester));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> isPackageMaintainer(
            User requester,
            List<PackageMaintainer> packageMaintainers,
            List<Package> rPackages,
            List<Submission> submissions) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.addAll(resourcePredicates(
                    packageMaintainers, RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER), criteriaBuilder, root));
            predicates.addAll(
                    resourcePredicates(rPackages, RESOURCE_TYPES.get(ResourceType.PACKAGE), criteriaBuilder, root));
            predicates.addAll(resourcePredicates(
                    submissions, RESOURCE_TYPES.get(ResourceType.SUBMISSION), criteriaBuilder, root));
            predicates.add(criteriaBuilder.equal(root.get(AUTHOR), requester));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<NewsfeedEvent> byTechnology(List<String> technologies) {

        return (root, query, criteriaBuilder) -> {
            TechnologyResolver technologyResolver = new TechnologyResolver();
            List<String> updatedTechnologies = technologyResolver.getTechnologies(technologies);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder
                    .in(root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT)
                            .get(RESOURCE_TECHNOLOGY))
                    .value(updatedTechnologies));
            predicates.add(criteriaBuilder
                    .in(root.join(RESOURCE_TYPES.get(ResourceType.REPOSITORY), JoinType.LEFT)
                            .get(RESOURCE_TECHNOLOGY))
                    .value(updatedTechnologies));
            predicates.add(criteriaBuilder
                    .in(root.join(RESOURCE_TYPES.get(ResourceType.SUBMISSION), JoinType.LEFT)
                            .join(RESOURCE_TYPES.get(ResourceType.PACKAGE), JoinType.LEFT)
                            .get(RESOURCE_TECHNOLOGY))
                    .value(updatedTechnologies));
            predicates.add(criteriaBuilder
                    .in(root.join(RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER), JoinType.LEFT)
                            .join(RESOURCE_TYPES.get(ResourceType.REPOSITORY), JoinType.LEFT)
                            .get(RESOURCE_TECHNOLOGY))
                    .value(updatedTechnologies));
            predicates.add(criteriaBuilder
                    .in(root.join(RESOURCE_TYPES.get(ResourceType.REPOSITORY_MAINTAINER), JoinType.LEFT)
                            .join(RESOURCE_TYPES.get(ResourceType.REPOSITORY), JoinType.LEFT)
                            .get(RESOURCE_TECHNOLOGY))
                    .value(updatedTechnologies));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<Predicate> resourcePredicates(
            List<? extends Resource> entities,
            String fieldName,
            CriteriaBuilder criteriaBuilder,
            Root<NewsfeedEvent> root) {
        List<Predicate> predicates = new ArrayList<>();
        entities.forEach((resource) ->
                predicates.add(criteriaBuilder.equal(root.get(fieldName).get("id"), resource.getId())));
        return predicates;
    }

    public static Specification<NewsfeedEvent> hasResourceWithResourcePropertyAndOneOfTypes(
            String property, Resource resource, String... types) {
        Specification<NewsfeedEvent> spec;

        if (types.length == 0) {
            spec = notNullSpecification();
        } else {
            spec = new RelatedResourceNestedIdSpecification(types[0], property, resource.getId());

            for (int i = 1; i < types.length; i++) {
                spec = spec.or(new RelatedResourceNestedIdSpecification(types[i], property, resource.getId()));
            }
        }

        return spec;
    }

    public static Specification<NewsfeedEvent> ofResourceTypes(List<ResourceType> resourceTypes) {

        Specification<NewsfeedEvent> spec = null;

        for (ResourceType resourceType : resourceTypes) {
            spec = SpecificationUtils.orComponent(spec, hasResourceOfType(resourceType));
        }
        return spec;
    }

    public static Specification<NewsfeedEvent> hasResourceOfType(ResourceType resourceType) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(RESOURCE_TYPES.get(resourceType)));
    }

    public static Specification<NewsfeedEvent> relatedResourceHasRelatedRepository(Repository repository) {
        return hasResourceWithResourcePropertyAndOneOfTypes(
                        RESOURCE_TYPES.get(ResourceType.REPOSITORY),
                        repository,
                        RESOURCE_TYPES.get(ResourceType.PACKAGE_MAINTAINER),
                        RESOURCE_TYPES.get(ResourceType.REPOSITORY_MAINTAINER),
                        RESOURCE_TYPES.get(ResourceType.PACKAGE))
                .or(hasResourceOfType(ResourceType.REPOSITORY).and(hasRelatedResource(repository)));
    }

    private static class RelatedResourceNestedIdSpecification extends RelatedResourceSpecification {

        @Serial
        private static final long serialVersionUID = -8475467216624159313L;

        public RelatedResourceNestedIdSpecification(String resourceName, String property, Integer id) {
            super(resourceName, property, id);
        }

        @Override
        public Predicate toPredicate(Root<NewsfeedEvent> root, @NonNull CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.equal(
                    root.join(resourceName, JoinType.LEFT)
                            .join(property, JoinType.LEFT)
                            .get("id"),
                    value);
        }
    }
}
