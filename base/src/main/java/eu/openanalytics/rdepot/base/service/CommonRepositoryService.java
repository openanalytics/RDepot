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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.technology.Technology;

/**
 * Repository service that aggregates repositories of all technologies.
 */
@Component
public class CommonRepositoryService {
	private final Map<Technology, RepositoryService<?>> repositoryServices;

	public CommonRepositoryService(Map<Technology, RepositoryService<?>> repositoryServicesByTechnology) {
		this.repositoryServices = repositoryServicesByTechnology;
	}

	public List<Repository<?, ?>> findAll() {
		List<Repository<?, ?>> repositories = new ArrayList<>();

		for (RepositoryService<?> service : repositoryServices.values()) {
			repositories.addAll(service.findAll());
		}

		return repositories;
	}

	public Optional<? extends Repository<?, ?>> findById(int id, Technology technology) {
		RepositoryService<?> service = repositoryServices.get(technology);

		if (service != null) {
			return service.findById(id);
		}
		return Optional.empty();
	}
	
	public Optional<? extends Repository<?,?>> findById(int id) {
		for(RepositoryService<?> service : repositoryServices.values()) {
			if(service != null) {
				Optional<? extends Repository<?,?>> repository = service.findById(id);
				if(repository.isPresent())
					return repository;
			}
		}

		return Optional.empty();
	}
}
