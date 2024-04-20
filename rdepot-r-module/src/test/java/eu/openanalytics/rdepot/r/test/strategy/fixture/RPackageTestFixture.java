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
package eu.openanalytics.rdepot.r.test.strategy.fixture;

import java.util.ArrayList;
import java.util.List;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.test.fixture.RSubmissionTestFixture;

public class RPackageTestFixture {
	
	public static int id = 123;
	
	public static RPackage GET_PACKAGE_FOR_REPOSITORY_AND_USER(RRepository repository, User user) {
		return GET_PACKAGE_FOR_REPOSITORY_AND_USER(id++, repository, user);
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
		packageBag.setSource("/some/source/package_1.2.3.tar.gz");
		Submission submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		packageBag.setSubmission(submission);
		return packageBag;
	}
	
	/**
	 * Returns the following packages:
	 * <ol>
	 *   <li>"Abc" version: "1.0.0"</li>
	 *   <li>"Abc" version: "1.0.1"</li>
	 *   <li>"Abc" version: "1.2.3"</li>
	 *   <li>"X-Y-Z" version: "0.0.1"</li>
	 *   <li>"X-Y-Z" version: "0.0.3"</li>
	 *   <li>"D E F" version: "0.0.3"</li>
	 * </ol>
	 * @param repository
	 * @param user
	 * @return
	 */
	public static List<RPackage> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(RRepository repository, User user) {
		final ArrayList<RPackage> packages = new ArrayList<>();
		
		// Package ABC
		final String PACKAGE_NAME_ABC = "Abc";
		final RPackage abcVersion1 = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcVersion1.setName(PACKAGE_NAME_ABC);
		abcVersion1.setVersion("1.0.0");
		final RPackage abcVersion2 = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcVersion2.setName(PACKAGE_NAME_ABC);
		abcVersion2.setVersion("1.0.1");
		final RPackage abcLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcLatest.setName(PACKAGE_NAME_ABC);
		abcLatest.setVersion("1.2.3");
		
		packages.add(abcVersion1);
		packages.add(abcVersion2);
		packages.add(abcLatest);
		
		// Package XYZ
		final String PACKAGE_NAME_XYZ = "X-Y-Z";
		final RPackage xyzVersion1 = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		xyzVersion1.setName(PACKAGE_NAME_XYZ);
		xyzVersion1.setVersion("0.0.1");
		
		final RPackage xyzLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		xyzLatest.setName(PACKAGE_NAME_XYZ);
		xyzLatest.setVersion("0.0.3");
		
		packages.add(xyzVersion1);
		packages.add(xyzLatest);
		
		// Package DEF
		final String PACKAGE_NAME_DEF = "D E F";
		final RPackage defLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		defLatest.setName(PACKAGE_NAME_DEF);
		defLatest.setVersion("3.2.1");
		packages.add(defLatest);
		
		return packages;
	}
	
	public static List<RPackage> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(RRepository repository, User user) {
		final ArrayList<RPackage> packages = new ArrayList<>();
		
		// Package ABC
		final String PACKAGE_NAME_ABC = "Abc";
		final RPackage abcLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcLatest.setName(PACKAGE_NAME_ABC);
		abcLatest.setVersion("1.2.3");
		
		packages.add(abcLatest);
		
		// Package XYZ
		final String PACKAGE_NAME_XYZ = "X-Y-Z";
		final RPackage xyzLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		xyzLatest.setName(PACKAGE_NAME_XYZ);
		xyzLatest.setVersion("0.0.3");

		packages.add(xyzLatest);
		
		// Package DEF
		final String PACKAGE_NAME_DEF = "D E F";
		final RPackage defLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		defLatest.setName(PACKAGE_NAME_DEF);
		defLatest.setVersion("3.2.1");
		packages.add(defLatest);
		
		return packages;
	}
	
	public static List<RPackage> GET_PACKAGES_FOR_REPOSITORY_AND_USER(int count, RRepository repository, User user) {
		List<RPackage> packages = new ArrayList<>();
		
		for(int i = 0; i < count; i++) {
			packages.add(GET_PACKAGE_FOR_REPOSITORY_AND_USER(123 + i, repository, user));
		}
		
		return packages;
	}
	
	/**
	 * 
	 * @param names should follow the convention: id_packagename_1.2.3
	 * @return
	 */
	public static List<RPackage> GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
			RRepository repository, String sourcePrefix, String... names) {
		List<RPackage> packages = new ArrayList<>();
		
		User user = UserTestFixture.GET_ADMIN();
		
		for(int i = 0; i < names.length; i++) {
			String name = names[i];
			String[] tokens = name.split("_");
			String id = tokens[0];
			String packageName = tokens[1];
			String version = tokens[2];
			
			RPackage packageBag = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
			packageBag.setId(Integer.valueOf(id));
			packageBag.setName(packageName);
			packageBag.setAuthor("Author of package " + name);
			packageBag.setVersion(version);
			packageBag.setActive(true);
			packageBag.setDeleted(false);
			packageBag.setSource(sourcePrefix + "/" + packageName + "_" + version + ".tar.gz");
			packages.add(packageBag);
		}
		
		return packages;
	}
}
