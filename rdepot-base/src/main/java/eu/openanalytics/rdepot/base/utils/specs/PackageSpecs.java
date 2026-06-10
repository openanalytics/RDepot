/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.utils.TechnologyResolver;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PackageSpecs {

    public static <P extends Package> Specification<P> ofRepository(List<String> repositories) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("repositoryGeneric").get("name")).value(repositories);
    }

    public static <P extends Package> Specification<P> isDeleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("deleted"), deleted);
    }

    public static <P extends Package> Specification<P> isBinary(boolean binary) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("binary"), binary);
    }

    public static <P extends Package> Specification<P> ofSubmissionState(List<SubmissionState> states) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("submission").get("state")).value(states);
    }

    public static <P extends Package> Specification<P> ofTechnology(List<String> technologies) {
        return (root, query, criteriaBuilder) -> {
            TechnologyResolver technologyResolver = new TechnologyResolver();
            List<String> updatedTechnologies = technologyResolver.getTechnologies(technologies);
            return criteriaBuilder.in(root.get("resourceTechnology")).value(updatedTechnologies);
        };
    }

    public static <P extends Package> Specification<P> ofName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static <P extends Package> Specification<P> ofMaintainer(List<String> maintainers) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("user").get("name")).value(maintainers);
    }

    public static <P extends Package> Specification<P> notMaintainedBy(List<String> maintainers) {
        return (root, query, criteriaBuilder) -> {
            Join<?, ?> joinMaintainers = root.join("maintainers", JoinType.LEFT);
            Join<?, ?> joinUsers = joinMaintainers.join("user", JoinType.LEFT);

            Predicate notName = criteriaBuilder.not(
                    criteriaBuilder.in(joinUsers.get("name")).value(maintainers));
            Predicate nullName = criteriaBuilder.isNull(joinUsers.get("name"));
            Predicate notDeleted = criteriaBuilder.equal(joinMaintainers.get("deleted"), false);

            return criteriaBuilder.or(nullName, criteriaBuilder.and(notName, notDeleted));
        };
    }
}
