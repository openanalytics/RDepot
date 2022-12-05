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
package eu.openanalytics.rdepot.base.mediator.newsfeed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.utils.NewsfeedEventSpecs;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.entities.Package;

@Component
public class NewsfeedEventsRolesFiltering {

	private final RepositoryMaintainerService repositoryMaintainerService;
	private final PackageMaintainerService packageMaintainerService;
	private final SubmissionService submissionService;
	private final CommonPackageService packageService;
	
	private List<RepositoryMaintainer> repositoryMaintainers = new ArrayList<RepositoryMaintainer>();
	private List<PackageMaintainer> packageMaintainers = new ArrayList<PackageMaintainer>();
	private List<Repository<?,?>> repositories = new ArrayList<>();
	private List<Package<?, ?>> packages = new ArrayList<>();
	private List<Submission> submissions = new ArrayList<>();
	private Specification<NewsfeedEvent> specification;
	
	public NewsfeedEventsRolesFiltering(RepositoryMaintainerService repositoryMaintainerService,
			PackageMaintainerService packageMaintainerService, 
			CommonPackageService commonPackageService,
			SubmissionService submissionService) {
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.packageMaintainerService = packageMaintainerService;
		this.submissionService = submissionService;
		this.packageService = commonPackageService;
	}
	
	public Specification<NewsfeedEvent> getNewsfeedEventsRolesSpecification(User user){
		
		switch(user.getRole().getName()) {
			case "repositorymaintainer":
				repositoryMaintainers = repositoryMaintainerService.findByUserWithoutDeleted(user);	
				
				repositoryMaintainers.forEach(repositoryMaintainer -> {
					repositories.add(repositoryMaintainer.getRepository());
					packageMaintainers.addAll(packageMaintainerService.findByRepository(repositoryMaintainer.getRepository()));
					packages.addAll(packageService.findAllByRepository(repositoryMaintainer.getRepository()));
					submissions.addAll(submissionService.findAllByRepository(repositoryMaintainer.getRepository()));
				});
				
				if(!repositoryMaintainers.isEmpty()) {
					specification = Specification.where(NewsfeedEventSpecs
							.isRepositoryMaintainer(user, repositoryMaintainers, packageMaintainers, repositories,
								packages, submissions));
				} else {
					specification = Specification.where(NewsfeedEventSpecs.byUser(user));
				}
				break;
			case "packagemaintainer":
				packageMaintainers = packageMaintainerService.findByUser(user);
				packageMaintainers.forEach(packageMaintainer -> {
					packages.addAll(packageService
							.findAllByNameAndRepository(packageMaintainer.getPackageName(), packageMaintainer.getRepository()));
				});
				packages.forEach(rPackage -> {
					Optional<Submission> submission = submissionService.findByPackage(rPackage);
					if(submission.isPresent()) {
						submissions.add(submission.get());
					}
				});
				if(!packageMaintainers.isEmpty()) {
					specification = Specification.where(NewsfeedEventSpecs
							.isPackageMaintainer(user, packageMaintainers,
								packages, submissions));
				}
				else {
					specification = Specification.where(NewsfeedEventSpecs.byUser(user));
				}
				break;
			case "user":
				specification = Specification.where(NewsfeedEventSpecs.byUser(user));
				break;
			default: //admin should have access to everything 
				specification = null;
				break;
		}
		return specification;
	}
}
