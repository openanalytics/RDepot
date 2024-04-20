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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;

public class PackagesTestFixture {
	public static final String NAME = "TestPackage";
	public static final String DESCRIPTION = "Simple test package";
	public static final String AUTHOR = "Albert Einstein";
	public static final String LICENSE = "Some license";
	public static final String SOURCE = "test_package.tar.gz";
	public static final String TITLE = "Test Package";
	public static final String HASH = "1234567";
	public static final String VERSION = "4.5.6";
	public static final Boolean ACTIVATED = true;
	public static final Boolean DELETED = false;
	
	public static List<PythonPackage> GET_FIXTURE_PACKAGES(PythonRepository repository, User user,
			int packageCount, int idShift){
		List<PythonPackage> packages = new ArrayList<>();
		
		for(int i = idShift; i < packageCount + idShift; i++) {
			PythonPackage packageBag = new PythonPackage(
					i, 
					repository, 
					user, 
					NAME + Integer.toString(i), 
					DESCRIPTION + Integer.toString(i), 
					AUTHOR + Integer.toString(i), 
					LICENSE + Integer.toString(i), 
					Integer.toString(i) + SOURCE, 
					TITLE + Integer.toString(i), 
					HASH + Integer.toString(i), 
					ACTIVATED, 
					DELETED);
		
			Submission submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
			submission.setId(i);
			submission.setCreatedDate(DateProvider.getCurrentDate());
			packageBag.setSubmission(submission);
			packageBag.setVersion(VERSION);
			packages.add(packageBag);
		}
		
		return packages;
	}
	
	public static PythonPackage GET_EXAMPLE_PACKAGE() {
		PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		PythonPackage packageBag = GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);
		
		return packageBag;
	}
	
	public static Page<PythonPackage> GET_EXAMPLE_PACKAGES_PAGED (PythonRepository repository, User user){
		return new PageImpl<>(GET_FIXTURE_PACKAGES(repository, user, 3, 100));
	}
	
	public static Page<PythonPackage> GET_EXAMPLE_PACKAGES_PAGED_DELETED (){
		PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		List<PythonPackage> packages = GET_FIXTURE_PACKAGES(repository, user, 3, 100);
		packages.forEach(p -> p.setDeleted(true));
		
		return new PageImpl<>(packages);
	}
	
	public static PythonPackage GET_FIXTURE_PACKAGE(PythonRepository repository, User user, Integer id) {
		return GET_FIXTURE_PACKAGES(repository, user, 1, id).get(0);
	}
	
	public static PythonPackage GET_FIXTURE_PACKAGE(PythonRepository repository, User user) {
		return GET_FIXTURE_PACKAGE(repository, user, 0);
	}
	
	public static PackageDto GET_EXAMPLE_PACKAGE_DTO(Package packageBag) {
		PackageDto packageDto = new PackageDto(packageBag);
		return packageDto;
	}
	
	public static List<PackageDto> GET_EXAMPLE_PACKAGE_DTOS(List<Submission> submissions) {
		List<PackageDto> packageDtos = new ArrayList<PackageDto>();
		submissions.forEach(submission ->{
			packageDtos.add(new PackageDto(submission.getPackageBag()));
		});
		return packageDtos;
	}
	
	public static PackageDto GET_EXAMPLE_PACKAGE_DTO() {
		PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		Package packageBag = GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);
		
		return new PackageDto(packageBag);
	}
}
