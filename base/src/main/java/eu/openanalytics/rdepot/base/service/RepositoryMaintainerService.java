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
package eu.openanalytics.rdepot.base.service;

import java.util.List;
import java.util.Optional;

import eu.openanalytics.rdepot.base.daos.RepositoryMaintainerDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;

@org.springframework.stereotype.Service
public class RepositoryMaintainerService extends Service<RepositoryMaintainer>{

	private final RepositoryMaintainerDao dao;
	
	public RepositoryMaintainerService(RepositoryMaintainerDao dao) {
		super(dao);
		this.dao = dao;
	}

	public List<RepositoryMaintainer> findByRepository(Repository<?, ?> repository) {			
		return dao.findByRepository(repository);
	}
	
	public Optional<RepositoryMaintainer> findByRepositoryAndUserAndDeleted(Repository<?,?> repository, User user, boolean deleted) {
		return dao.findByRepositoryAndUserAndDeleted(repository, user, deleted);
	}
	
	public List<RepositoryMaintainer> findByUserWithoutDeleted(User user) {
		return dao.findByUserAndDeleted(user, false);
	}
	
	public List<RepositoryMaintainer> findByUserAndRepository(User user, Repository<?, ?> repository) {				
		return dao.findByUserAndRepository(user, repository);
	}
	
	public List<RepositoryMaintainer> findByRepositoryNonDeleted(Repository<?,?> repository) {
		return dao.findByRepositoryAndDeleted(repository, false);
	}
}
