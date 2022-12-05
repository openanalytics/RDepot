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

import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RepositoryMaintainerV1Dto {
	private int id;
	private UserV1Dto user;
	private RepositoryV1Dto repository;
	private boolean deleted;
	
	public RepositoryMaintainerV1Dto() { }
	
	public RepositoryMaintainerV1Dto(RepositoryMaintainer repositoryMaintainer) {
		this.id = repositoryMaintainer.getId();
		this.user = new UserV1Dto(repositoryMaintainer.getUser());
		this.repository = new RepositoryV1Dto((RRepository) repositoryMaintainer.getRepository());
		this.deleted = repositoryMaintainer.isDeleted();
	}		

	static RepositoryMaintainerV1Dto of(RepositoryMaintainer repositoryMaintainer) {
		return new RepositoryMaintainerV1Dto(repositoryMaintainer);
	}		

	public int getId() {
		return id;
	}

	public UserV1Dto getUser() {
		return user;
	}

	public RepositoryV1Dto getRepository() {
		return repository;
	}

	public boolean isDeleted() {
		return deleted;
	}
}
