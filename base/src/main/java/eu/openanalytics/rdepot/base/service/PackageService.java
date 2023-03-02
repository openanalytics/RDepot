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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.rdepot.base.daos.PackageDao;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.comparators.PackageComparator;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;

public abstract class PackageService<E extends Package<E,?>> extends Service<E>  {

	private final PackageDao<E> dao;
	private static final Logger logger = LoggerFactory.getLogger(PackageService.class);
	
	public PackageService(PackageDao<E> dao) {
		super(dao);
		this.dao = dao;
	}
	
	@Override
	public E create(E entity) throws CreateEntityException {
		deleteSameVersion(entity);
		return super.create(entity);
	}
	
	public List<E> findAllByRepository(Repository<?,?> repository) {
		return dao.findByRepositoryAndDeleted(repository, false);
	}
	
	public List<E> findActiveByRepository(Repository<?,?> repository) {
		return dao.findByRepositoryAndDeletedAndActive(repository, false, true);
	}
	
	private void deleteSameVersion(E entity) {
		List<E> samePackages = dao.findAllByNameAndVersionAndRepositoryAndDeleted
				(entity.getName(), 
				entity.getVersion(), 
				entity.getRepository(), 
				false);
		
		if(!samePackages.isEmpty())
			logger.debug("Found non-deleted packages of the same name, version and repository.");
		
		samePackages.forEach(p -> {
			p.setDeleted(true);
			p.setActive(false);
		});
	}
	
	public List<E> findAllByNameAndRepository(String name, Repository<?,?> repository) {
		return dao.findByNameAndRepositoryAndDeleted(name, repository, false);
	}
	
	public void deleteAllForRepository(Repository<?,?> repository) throws DeleteEntityException {
		for(E packageBag : findAllByRepository(repository)) {
			delete(packageBag);
		}
	}
	
	public List<E> findByNameAndVersionAndRepository(String name, String version, Repository<?, ?> repository) {
		return dao.findByNameAndVersionAndRepository(name, version, repository);
	}
	
	public Optional<E> findByNameAndVersionAndRepositoryAndDeleted(String name, String version, Repository<?, ?> repository, Boolean deleted) {
		return dao.findByNameAndVersionAndRepositoryAndDeleted(name, version, repository, deleted);
	}
	
	public Optional<E> findNonDeletedNewestByNameAndRepository(String name, Repository<?,?> repository) {
		List<E> packages = dao.findByNameAndRepositoryAndDeleted(name, repository, false);
		if(packages.isEmpty())
			return Optional.empty();
		
		return Optional.of(Collections.max(packages, new PackageComparator<E>()));
	}
	
	public Optional<E> findNonDeletedByNameAndVersionAndRepository(
			String name, String version, Repository<?,?> repository) {
		List<E> packages = dao.findAllByNameAndVersionAndRepositoryAndDeleted(name, version, repository, false);
		
		if(packages.isEmpty())
			return Optional.empty();
		
		return Optional.of(packages.get(0));
	}
	
	public Set<E> filterLatest(Set<E> packages) {
		LinkedHashMap<String, E> latestVersionMap = new LinkedHashMap<>();
		
		for(E packageBag : packages) {
			E latest = latestVersionMap.get(packageBag.getName());
			
			if(latest == null || packageBag.compareTo(latest) > 0) {
				latestVersionMap.put(packageBag.getName(), packageBag);
			}
		}
		
		LinkedHashSet<E> latestPackageSet = 
				new LinkedHashSet<>(latestVersionMap.values());
		packages.removeAll(latestPackageSet);
		
		return latestPackageSet;
	}
	
	public List<E> findAllNonDeletedAndAccepted() {
		return dao.findByDeletedAndSubmissionState(false, SubmissionState.ACCEPTED);
	}
}
