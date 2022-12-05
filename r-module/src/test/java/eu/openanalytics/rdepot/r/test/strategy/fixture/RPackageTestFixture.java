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

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RPackageTestFixture {
	
	public static RPackage GET_PACKAGE_FOR_REPOSITORY_AND_USER(RRepository repository, User user) {
		return GET_PACKAGE_FOR_REPOSITORY_AND_USER(123, repository, user);
	}
	
	public static RPackage GET_PACKAGE_FOR_REPOSITORY_AND_USER(int id, RRepository repository, User user) {
		RPackage packageBag = new RPackage();
		packageBag.setId(id);
		packageBag.setActive(true);
		packageBag.setAuthor("Scientist no. " + id * 2);
		packageBag.setDeleted(false);
		packageBag.setDepends("Some packages it depends on");
		packageBag.setDescription("Example package it is");
		packageBag.setGenerateManuals(false);
		packageBag.setImports("Some imports");
		packageBag.setLicense("Apache License");
		packageBag.setMd5sum("mD5sUm");
		packageBag.setName("Test Package no. " + id);
		packageBag.setRepository(repository);
		packageBag.setUrl("http://example.com");
		packageBag.setUser(user);
		
		return packageBag;
	}
	
	public static List<RPackage> GET_PACKAGES_FOR_REPOSITORY_AND_USER(int count, RRepository repository, User user) {
		List<RPackage> packages = new ArrayList<>();
		
		for(int i = 0; i < count; i++) {
			packages.add(GET_PACKAGE_FOR_REPOSITORY_AND_USER(123 + i, repository, user));
		}
		
		return packages;
	}
}
