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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;

public class PackageMaintainerTestFixture {
	public static final String PACKAGE_NAME = PackageTestFixture.NAME;
	public static final Boolean DELETED = false;
	
	public static List<PackageMaintainer> GET_EXAMPLE_PACKAGE_MAINTAINERS() {
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(123);
		User user = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(111);
		
		List<PackageMaintainer> maintainers = new ArrayList<>();
		for(int i = 100; i < 103; i++) {
			PackageMaintainer maintainer = new PackageMaintainer(i, user, repository, PACKAGE_NAME + i, false);
			maintainers.add(maintainer);
		}
		
		maintainers.get(2).setDeleted(true);
		return maintainers;
	}
	
	public static Page<PackageMaintainer> GET_EXAMPLE_PACKAGE_MAINTAINERS_PAGED() {
		return new PageImpl<PackageMaintainer>(GET_EXAMPLE_PACKAGE_MAINTAINERS());
	}
	
	public static List<PackageMaintainer> GET_FIXTURE_PACKAGE_MAINTAINERS(User user, Repository repository, int packageCount, int idShift) {
		List<PackageMaintainer> packageMaintainers = new ArrayList<>();
		
		for(int i = idShift; i < packageCount + idShift; i++) {
			packageMaintainers.add(new PackageMaintainer(i, user, repository, PACKAGE_NAME + Integer.toString(i), DELETED));
		}
		
		return packageMaintainers;
	}
	
	public static List<PackageMaintainer> GET_FIXTURE_PACKAGE_MAINTAINERS(User user, Repository repository, int packageCount) {
		return GET_FIXTURE_PACKAGE_MAINTAINERS(user, repository, packageCount, 0);
	}
	
	public static PackageMaintainer GET_FIXTURE_PACKAGE_MAINTAINER(User user, Repository repository) {
		return GET_FIXTURE_PACKAGE_MAINTAINERS(user, repository, 1).get(0);
	}
	
	public static PackageMaintainer GET_FIXTURE_PACKAGE_MAINTAINER(User user, Repository repository, RPackage packageBag, int shift) {
		return new PackageMaintainer(shift, user, repository, packageBag.getName(), DELETED);
	}
	
	public static PackageMaintainer GET_FIXTURE_PACKAGE_MAINTAINER(User user, Repository repository, RPackage packageBag) {
		return GET_FIXTURE_PACKAGE_MAINTAINER(user, repository, packageBag, 0);
	}

	public static PackageMaintainer GET_FIXTURE_PACKAGE_MAINTAINER() {
		return GET_EXAMPLE_PACKAGE_MAINTAINERS().get(0);
	}
}
