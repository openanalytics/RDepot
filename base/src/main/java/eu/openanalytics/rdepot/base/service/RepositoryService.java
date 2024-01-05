/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.base.daos.RepositoryDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.Storage;

public abstract class RepositoryService<E extends Repository<E,?>> extends Service<E> {

	private final RepositoryDao<E> dao;
	private static final Logger logger = LoggerFactory.getLogger(RepositoryService.class);

	public RepositoryService(RepositoryDao<E> dao, Storage<E, ?> storage) {
		super(dao);
		this.dao = dao;
	}
	
	public Optional<E> findByName(String name) {
		return dao.findByName(name);
	}
	
	public Optional<E> findByNameAndDeleted(String name, boolean deleted) {
		return dao.findByNameAndDeleted(name, deleted);
	}
	
	@Override
	public void delete(E entity) {
		dao.delete(entity);
	}

	public Optional<E> findByPublicationUri(String publicationUri) {
		return dao.findByPublicationUri(publicationUri);
	}

	public Optional<E> findByServerAddress(String serverAddress) {
		return dao.findByServerAddress(serverAddress);
	}
	
	public Page<E> findAllByDeleted(boolean deleted, Pageable pageable) {
		return dao.findAllByDeleted(deleted, pageable);
	}	
	
	@Transactional
	public void incrementVersion(E repository) {
		Optional<E> currentRepositoryOpt = Optional.empty();
		int attempts = 0;
		while(attempts < 3) {
			currentRepositoryOpt = dao.findByNameAcquirePessimisticWriteLock(repository.getName());
			
			if(currentRepositoryOpt.isEmpty()) {
				logger.warn("Could not acquire lock on repository " + repository.getName() 
				+ "! Trying again...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new IllegalStateException("Error while acquiring lock on repository.", e);
				}
				attempts++;
			} else {
				break;
			}
		}
		
		E currentRepository = currentRepositoryOpt.orElseThrow(() -> 
			new IllegalStateException("Could not acquire lock on repository " 
				+ repository.getName()));
		logger.debug("Incrementing version from " + currentRepository.getVersion());
		currentRepository.setVersion(currentRepository.getVersion() + 1);
		dao.saveAndFlush(currentRepository);
	}
}
