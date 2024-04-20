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
package eu.openanalytics.rdepot.python.test.strategy.fixture;

import java.util.ArrayList;
import java.util.List;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.test.fixture.PythonSubmissionTestFixture;

public class PythonPackageTestFixture {

public static int id = 123;
	
	public static PythonPackage GET_PACKAGE_FOR_REPOSITORY_AND_USER(PythonRepository repository, User user) {
		return GET_PACKAGE_FOR_REPOSITORY_AND_USER(id++, repository, user);
	}
	
	public static PythonPackage GET_PACKAGE_FOR_REPOSITORY_AND_USER(int id, PythonRepository repository, User user) {
		
		PythonPackage packageBag = new PythonPackage();
		packageBag.setId(id);
		packageBag.setActive(true);
		packageBag.setAuthor("Scientist no. " + id * 2);
		packageBag.setDeleted(false);
		packageBag.setDescription("Example package it is");
		packageBag.setLicense("Apache License");
		packageBag.setName("Test Package no. " + id);
		packageBag.setUrl("http://example.com");
		packageBag.setUser(user);
		packageBag.setSource("/some/source/package_1.2.3.tar.gz");
		packageBag.setSummary("Summary of the test package");
		packageBag.setAuthorEmail("testauthor@oa.com");
		packageBag.setRepository(repository);
		Submission submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		packageBag.setSubmission(submission);
		
		return packageBag;
	}
	
	/**
	 * Returns the following packages:
	 * <ol>
	 *   <li>"Abc" version: "1.0.0"</li>
	 *   <li>"Abc" version: "1.0.1"</li>
	 *   <li>"Abc" version: "1.2.0"</li>
	 *   <li>"X-Y-Z" version: "0.0.1"</li>
	 *   <li>"X-Y-Z" version: "0.0.3"</li>
	 *   <li>"D E F" version: "0.0.3"</li>
	 * </ol>
	 * @param repository
	 * @param user
	 * @return
	 */
	public static List<PythonPackage> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(PythonRepository repository, User user){
		final ArrayList<PythonPackage> packages = new ArrayList<>();
		
		//Package Abc
		final String PACKAGE_NAME_ABC = "Abc";
		final PythonPackage abcVersion1 = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcVersion1.setName(PACKAGE_NAME_ABC);
		abcVersion1.setVersion("1.0.0");
		final PythonPackage abcVersion2 = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcVersion2.setName(PACKAGE_NAME_ABC);
		abcVersion2.setVersion("1.0.1");
		final PythonPackage abcLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcLatest.setName(PACKAGE_NAME_ABC);
		abcLatest.setVersion("1.2.0");
		
		packages.add(abcVersion1);
		packages.add(abcVersion2);
		packages.add(abcLatest);
		
		//Package X-Y-Z
		final String PACKAGE_NAME_XYZ = "X-Y-Z";
		final PythonPackage xyzVersion1 = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		xyzVersion1.setName(PACKAGE_NAME_XYZ);
		xyzVersion1.setVersion("0.0.1");
		final PythonPackage xyzLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		xyzLatest.setName(PACKAGE_NAME_XYZ);
		xyzLatest.setVersion("0.0.3");
		
		packages.add(xyzVersion1);
		packages.add(xyzLatest);
		
		//Package DEF
		final String PACKAGE_NAME_DEF = "D E F";
		final PythonPackage defLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		defLatest.setName(PACKAGE_NAME_DEF);
		defLatest.setVersion("3.2.1");
		packages.add(defLatest);
		
		return packages;
	}
	
	
	public static List<PythonPackage> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(PythonRepository repository, User user){
		
		final ArrayList<PythonPackage> packages = new ArrayList<>();
		
		//Package Abc
		final String PACKAGE_NAME_ABC = "Abc";
		final PythonPackage abcLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		abcLatest.setName(PACKAGE_NAME_ABC);
		abcLatest.setVersion("1.2.0");
		packages.add(abcLatest);
		
		//Package X-Y-Z
		final String PACKAGE_NAME_XYZ = "X-Y-Z";
		final PythonPackage xyzLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		xyzLatest.setName(PACKAGE_NAME_XYZ);
		xyzLatest.setVersion("0.0.3");
		packages.add(xyzLatest);
		
		//Package DEF
		final String PACKAGE_NAME_DEF = "D E F";
		final PythonPackage defLatest = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		defLatest.setName(PACKAGE_NAME_DEF);
		defLatest.setVersion("3.2.1");
		packages.add(defLatest);
		
		return packages;
	}
	
	/**
	 * 
	 * @param names should follow the convention: id_packagename_1.2.3
	 * @return
	 */
	public static List<PythonPackage> GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
			PythonRepository repository, String souceprefix, String... names){
		List<PythonPackage> packages = new ArrayList<>();
		
		User user = UserTestFixture.GET_ADMIN();
		
		for(int i = 0; i < names.length; i++) {
			String name = names[i];
			String[] tokens = name.split("_");
			String id = tokens[0];
			String packageName = tokens[1];
			String version = tokens[2];
			
			PythonPackage packageBag = GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
			packageBag.setId(Integer.valueOf(id));
			packageBag.setName(packageName);
			packageBag.setAuthor("Author of the package: " + name);
			packageBag.setVersion(version);
			packageBag.setActive(true);
			packageBag.setDeleted(false);
			packageBag.setSource(souceprefix + "/" + packageName + "_" + version + ".tar.gz"); 
			packages.add(packageBag);
		}
		
		return packages;
	}
	
}
