/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.r.test.strategy.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class PackageMaintainerTestFixture {
	public static PackageMaintainer GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(RRepository repository) {
		return GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(123, repository);
	}
	
	public static PackageMaintainer GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(int id, RRepository repository) {
		PackageMaintainer maintainer = new PackageMaintainer();
		maintainer.setId(id);
		maintainer.setPackageName("test_package");
		maintainer.setRepository(repository);
		maintainer.setUser(UserTestFixture.GET_PACKAGE_MAINTAINER());
		return maintainer;
	}
	
	public static PackageMaintainer GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(String name, User user, RRepository rRepository) {
		PackageMaintainer maintainer = new PackageMaintainer();
		
		maintainer.setId(100);
		maintainer.setPackageName(name);
		maintainer.setRepository(rRepository);
		maintainer.setUser(user);
		return maintainer;
	}
	
	public static List<PackageMaintainer> GET_PACKAGE_MAINTAINERS_FOR_REPOSITORY(int count, RRepository repository) {
		List<PackageMaintainer> maintainers = new ArrayList<>();
		
		for(int i = 0; i < count; i++) {
			maintainers.add(GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(123 + count, repository));
		}
		
		return maintainers;
	}
	
	/**
	 * create a list of packages that will be used during tests as a packages that
	 * needs to update package maintainer
	 * 
	 * @param length
	 * @param repository
	 * @param user
	 * @param packageName
	 * @return
	 */
	public static List<Package<?, ?>> GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(int length, RRepository repository, User user,
			String packageName) {

		List<RPackage> packageList = RPackageTestFixture.GET_PACKAGES_FOR_REPOSITORY_AND_USER(length, repository, user);
		
		packageList.forEach(p -> p.setName(packageName));
		if(length > 0) {
			packageList.get(0).setName(packageName + "different_name");
		}
		return packageList.stream().map(p -> (Package<?, ?>) p).collect(Collectors.toList());
	}
}
