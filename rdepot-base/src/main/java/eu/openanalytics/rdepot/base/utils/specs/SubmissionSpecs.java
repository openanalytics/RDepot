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

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.utils.TechnologyResolver;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionSpecs {

    private static final String PACKAGE = "packageBag";

    public static Specification<Submission> ofState(List<SubmissionState> state) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("state")).value(state);
    }

    public static Specification<Submission> ofSubmitter(String submitter) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.join("submitter", JoinType.LEFT).get("name")),
                "%" + submitter.toLowerCase() + "%");
    }

    public static Specification<Submission> ofApprover(String approver) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.join("approver", JoinType.LEFT).get("name")),
                "%" + approver.toLowerCase() + "%");
    }

    public static Specification<Submission> ofPackage(String packageBag) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.join(PACKAGE, JoinType.LEFT).get("name")),
                "%" + packageBag.toLowerCase() + "%");
    }

    public static Specification<Submission> fromDate(String fromDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDate date = LocalDate.parse(fromDate);

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), date));
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public static Specification<Submission> toDate(String toDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDate date = LocalDate.parse(toDate);

            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), date));
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public static Specification<Submission> ofTechnology(List<String> technologies) {
        return (root, query, criteriaBuilder) -> {
            TechnologyResolver technologyResolver = new TechnologyResolver();
            List<String> updatedTechnologies = technologyResolver.getTechnologies(technologies);
            return criteriaBuilder
                    .in(root.get(PACKAGE).get("resourceTechnology"))
                    .value(updatedTechnologies);
        };
    }

    public static Specification<Submission> ofRepository(List<String> repositories) {
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .in(root.get(PACKAGE).get("repositoryGeneric").get("name"))
                .value(repositories);
    }
}
