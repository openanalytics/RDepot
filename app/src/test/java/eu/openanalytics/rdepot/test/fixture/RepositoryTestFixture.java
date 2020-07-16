/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;

public class RepositoryTestFixture {
	public static final String PUBLICATION_URL = "http://localhost/test_repo";
	public static final String NAME = "TestRepo";
	public static final String SERVER_ADDRESS = "http://oa-rdepot-repo:8080/Repo";
	public static final Boolean PUBLISHED = false;
	public static final Boolean DELETED = false;
	
	public static List<Repository> GET_FIXTURE_REPOSITORIES(int repositoryCount, int idShift) {
		List<Repository> repositories = new ArrayList<>();
		
		for(int i = idShift; i < repositoryCount + idShift; i++) {
			Repository repository = new Repository(
					i,
					PUBLICATION_URL + Integer.toString(i),
					NAME + Integer.toString(i),
					SERVER_ADDRESS + Integer.toString(i),
					PUBLISHED,
					DELETED
					);
			repository.setVersion(1);
			repositories.add(repository);
		}
		
		return repositories;
	}
	
	public static List<Repository> GET_FIXTURE_REPOSITORIES(int repositoryCount) {
		return GET_FIXTURE_REPOSITORIES(repositoryCount, 0);
	}
	
	public static Repository GET_FIXTURE_REPOSITORY() {
		return GET_FIXTURE_REPOSITORIES(1).get(0);
	}
	
	/**
	 * Creates example Repository with package maintainers and repository maintainers.
	 * 
	 * @param packageMaintainerCount
	 * @param repositoryMaintainerCount
	 * @return 
	 */
	public static Repository GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(int packageMaintainerCount, int repositoryMaintainerCount) {
		Repository repository = GET_FIXTURE_REPOSITORY();
		List<User> repositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(repositoryMaintainerCount, 0, 0);
		User packageMaintainerUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(repositoryMaintainerCount);
		List<RepositoryMaintainer> repositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(repositoryMaintainerUsers, repository);
		repository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(repositoryMaintainers));
		List<PackageMaintainer> packageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(packageMaintainerUser, repository, packageMaintainerCount);
		
		Set<Package> packages = new HashSet<>();
		for(PackageMaintainer packageMaintainer : packageMaintainers) {
			Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, packageMaintainer.getUser());
			packageBag.setName(packageMaintainer.getPackage());
			packages.add(packageBag);
		}
		repository.setPackages(packages);
		repository.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
		return repository;
	}
}
