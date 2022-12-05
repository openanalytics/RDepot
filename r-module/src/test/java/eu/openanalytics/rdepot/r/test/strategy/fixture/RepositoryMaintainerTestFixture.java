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
package eu.openanalytics.rdepot.r.test.strategy.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RepositoryMaintainerTestFixture {
	
	public static RepositoryMaintainer GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(Repository<?,?> repository) {
		RepositoryMaintainer maintainer = new RepositoryMaintainer();
		maintainer.setId(123);
		maintainer.setRepository(repository);
		maintainer.setUser(UserTestFixture.GET_REPOSITORY_MAINTAINER());
		return maintainer;
	}
	public static List<RepositoryMaintainer> GET_REPOSITORY_MAINTAINERS_FOR_REPOSITORY(int count, Repository<?,?> repository) {
		List<RepositoryMaintainer> maintainers = new ArrayList<>();
		for(int i = 0; i < count; i++) {
			RepositoryMaintainer maintainer = new RepositoryMaintainer();
			maintainer.setId(123 + i);
			maintainer.setRepository(repository);
			maintainer.setUser(UserTestFixture.GET_REPOSITORY_MAINTAINER());
			
			maintainers.add(maintainer);
		}
		
		return maintainers;
	}
}
