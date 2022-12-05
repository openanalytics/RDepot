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
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class DTOConverter {

	public static RepositoryV1Dto convertRepository(RRepository entity) {
		return RepositoryV1Dto.of(entity);
	}
	
	public static List<RepositoryV1Dto> convertRepositories(List<RRepository> entities) {
		return entities.stream().map(RepositoryV1Dto::of).collect(Collectors.toList());
	}
	
	public static PackageV1Dto convertPackage(RPackage entity) {
		return PackageV1Dto.of(entity);
	}
	
	public static List<PackageV1Dto> convertPackages(List<RPackage> entities) {
		return entities.stream().map(PackageV1Dto::of).collect(Collectors.toList());
	}
	
	public static RoleV1Dto	convertRole(Role entity) {
		return RoleV1Dto.of(entity);
	}
	
	public static List<RoleV1Dto> convertRoles(List<Role> entities) {
		return entities.stream().map(RoleV1Dto::of).collect(Collectors.toList());
	}
	
	public static SubmissionV1Dto convertSubmission(Submission entity) {
		return SubmissionV1Dto.of(entity);
	}
	
	public static List<SubmissionV1Dto> convertSubmissions(List<Submission> entities) {
		return entities.stream().map(SubmissionV1Dto::of).collect(Collectors.toList());
	}
	
	public static PackageMaintainerV1Dto convertPackageMaintainer(PackageMaintainer entity) {
		return PackageMaintainerV1Dto.of(entity);
	}
	
	public static List<PackageMaintainerV1Dto> convertPackageMaintainers(List<PackageMaintainer> entities) {
		return entities.stream().map(PackageMaintainerV1Dto::of).collect(Collectors.toList());
	}
	
	public static RepositoryMaintainerV1Dto convertRepositoryMaintainer(RepositoryMaintainer entity) {
		return RepositoryMaintainerV1Dto.of(entity);
	}
	
	public static List<RepositoryMaintainerV1Dto> convertRepositoryMaintainers(List<RepositoryMaintainer> entities) {
		return entities.stream().map(RepositoryMaintainerV1Dto::of).collect(Collectors.toList());
	}
	
	public static UserV1Dto convertUser(User entity) {
		return UserV1Dto.of(entity);
	}
	
	public static List<UserV1Dto> convertUsers(List<User> entities) {
		return entities.stream().map(UserV1Dto::of).collect(Collectors.toList());
	}
}
