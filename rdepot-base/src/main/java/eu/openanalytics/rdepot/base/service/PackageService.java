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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.daos.PackageDao;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.comparators.PackageComparator;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class PackageService<E extends Package> extends Service<E>  {

	private final PackageDao<E> dao;
	
	public PackageService(PackageDao<E> dao) {
		super(dao);
		this.dao = dao;
	}
	
	@Override
	public E create(E entity) throws CreateEntityException {
		deleteSameVersion(entity);
		return super.create(entity);
	}
	
	public List<E> findAllByRepository(Repository repository) {
		return dao.findByRepositoryGenericAndDeleted(repository, false);
	}

	public int countByRepository(Repository repository) {
		return dao.countByRepositoryGenericAndDeleted(repository, false);
	}
	
	public List<E> findAllByRepositoryIncludeDeleted(Repository repository) {
		return dao.findByRepositoryGeneric(repository);
	}
	
	public List<E> findActiveByRepository(Repository repository) {
		return dao.findByRepositoryGenericAndDeletedAndActive(repository, false, true);
	}
	
	private void deleteSameVersion(E entity) {
		List<E> samePackages = dao.findAllByNameAndVersionAndRepositoryGenericAndDeleted
				(entity.getName(), 
				entity.getVersion(), 
				entity.getRepository(), 
				false);
		
		if(!samePackages.isEmpty())
			log.debug("Found non-deleted packages of the same name, version and repository.");
		
		samePackages.forEach(p -> {
			p.setDeleted(true);
			p.setActive(false);
		});
	}
	
	public List<E> findAllByNameAndRepository(String name, Repository repository) {
		return dao.findByNameAndRepositoryGenericAndDeleted(name, repository, false);
	}
	
	public Optional<E> findByNameAndVersionAndRepositoryAndDeleted(String name, String version, Repository repository, Boolean deleted) {
		return dao.findByNameAndVersionAndRepositoryGenericAndDeleted(name, version, repository, deleted);
	}
	
	public Optional<E> findNonDeletedNewestByNameAndRepository(String name, Repository repository) {
		List<E> packages = dao.findByNameAndRepositoryGenericAndDeleted(name, repository, false);
		if(packages.isEmpty())
			return Optional.empty();
		
		return Optional.of(Collections.max(packages, new PackageComparator<>()));
	}
	
	public Optional<E> findNonDeletedByNameAndVersionAndRepository(
			String name, String version, Repository repository) {
		List<E> packages = dao.findAllByNameAndVersionAndRepositoryGenericAndDeleted(name, version, repository, false);
		
		if(packages.isEmpty())
			return Optional.empty();
		
		return Optional.of(packages.get(0));
	}
	
	/**
	 * Filters out packages meant to be archived.
	 * It does not modify given set, rather returns a new one.
	 */
	public Set<E> filterLatest(Set<E> packages) {
		LinkedHashMap<String, E> latestVersionMap = new LinkedHashMap<>();
		
		for(E packageBag : packages) {
			E latest = latestVersionMap.get(packageBag.getName());
			
			if(latest == null || packageBag.compareTo(latest) > 0) {
				latestVersionMap.put(packageBag.getName(), packageBag);
			}
		}
		
		return new LinkedHashSet<>(latestVersionMap.values());
	}
}
