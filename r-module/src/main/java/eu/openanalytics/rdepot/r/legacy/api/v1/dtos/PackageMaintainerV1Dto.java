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

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class PackageMaintainerV1Dto {
	private int id;
	private UserV1Dto user;
	private RepositoryProjection repository;
	private boolean deleted;	
	private String packagebag;
	
	public PackageMaintainerV1Dto(PackageMaintainer packageMaintainer) {
		this.id = packageMaintainer.getId();
		this.user = new UserV1Dto(packageMaintainer.getUser());
		this.repository = new RepositoryProjection((RRepository) packageMaintainer.getRepository());
		this.deleted = packageMaintainer.isDeleted();
		this.packagebag = packageMaintainer.getPackageName();
	}
	
	public PackageMaintainerV1Dto() {
	}

	static PackageMaintainerV1Dto of(PackageMaintainer packageMaintainer) {
		return new PackageMaintainerV1Dto(packageMaintainer);
	}

	public int getId() {
		return id;
	}

	public UserV1Dto getUser() {
		return user;
	}

	public RepositoryProjection getRepository(){
		return repository;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public String getPackage() {
		return packagebag;
	}
}
