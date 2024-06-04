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
package eu.openanalytics.rdepot.base.mediator.newsfeed;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.utils.specs.NewsfeedEventSpecs;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Filters out those events that a given {@link User user} should not see.
 */
@Component
@RequiredArgsConstructor
public class NewsfeedEventsRolesFiltration {

    private final RepositoryMaintainerService repositoryMaintainerService;
    private final PackageMaintainerService packageMaintainerService;
    private final CommonPackageService packageService;
    private final SubmissionService submissionService;

    /**
     * Creates {@link Specification} excluding those
     * {@link NewsfeedEvent events} that a {@link User user}
     * should not be able to see.
     */
    public Specification<NewsfeedEvent> getNewsfeedEventsRolesSpecification(User user) {
        return switch (user.getRole().getName()) {
            case "repositorymaintainer" -> getRepositoryMaintainersSpecification(user);
            case "packagemaintainer" -> getPackageMaintainersSpecification(user);
            case "user" -> Specification.where(NewsfeedEventSpecs.byUser(user));
            default -> null;
        };
    }

    private Specification<NewsfeedEvent> getRepositoryMaintainersSpecification(User user) {
        List<RepositoryMaintainer> repositoryMaintainers = repositoryMaintainerService.findByUserWithoutDeleted(user);
        List<PackageMaintainer> packageMaintainers = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();
        List<Package> packages = new ArrayList<>();
        List<Submission> submissions = new ArrayList<>();

        repositoryMaintainers.forEach(repositoryMaintainer -> {
            repositories.add(repositoryMaintainer.getRepository());
            packageMaintainers.addAll(packageMaintainerService.findByRepository(repositoryMaintainer.getRepository()));
            packages.addAll(packageService.findAllByRepository(repositoryMaintainer.getRepository()));
            submissions.addAll(submissionService.findAllByRepository(repositoryMaintainer.getRepository()));
        });

        if (!repositoryMaintainers.isEmpty()) {
            return Specification.where(NewsfeedEventSpecs.isRepositoryMaintainer(
                    user, repositoryMaintainers, packageMaintainers, repositories, packages, submissions));
        } else {
            return Specification.where(NewsfeedEventSpecs.byUser(user));
        }
    }

    private Specification<NewsfeedEvent> getPackageMaintainersSpecification(User user) {
        List<PackageMaintainer> packageMaintainers = packageMaintainerService.findByUser(user);
        List<Package> packages = new ArrayList<>();
        List<Submission> submissions = new ArrayList<>();

        packageMaintainers.forEach((packageMaintainer) -> packages.addAll(packageService.findAllByNameAndRepository(
                packageMaintainer.getPackageName(), packageMaintainer.getRepository())));
        packages.forEach(rPackage -> {
            Optional<Submission> submission = submissionService.findByPackage(rPackage);
            submission.ifPresent(submissions::add);
        });
        if (!packageMaintainers.isEmpty()) {
            return Specification.where(
                    NewsfeedEventSpecs.isPackageMaintainer(user, packageMaintainers, packages, submissions));
        } else {
            return Specification.where(NewsfeedEventSpecs.byUser(user));
        }
    }
}
