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
package eu.openanalytics.rdepot.base.initializer;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryDeleter;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import eu.openanalytics.rdepot.base.runnable.SynchronizeMirrorTask;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.validation.RepositoryValidator;

public abstract class RepositoryDataInitializer<
	E extends Repository<E, ?>,
	R extends MirroredRepository<P, M>, 
	P extends MirroredPackage, 
	M extends Mirror<P>> 
	implements IRepositoryDataInitializer {
	
	private final Logger logger = LoggerFactory.getLogger(RepositoryDataInitializer.class);
	private final RepositoryService<E> repositoryService;
	private final RepositoryValidator<E> repositoryValidator;
	private final RepositoryDeleter<E, ?> repositoryDeleter;
	private final ThreadPoolTaskScheduler taskScheduler;
	private final MirrorSynchronizer<R, P, M> mirrorService;
	
	public RepositoryDataInitializer(RepositoryService<E> repositoryService,
			RepositoryValidator<E> repositoryValidator,
			RepositoryDeleter<E, ?> repositoryDeleter,
			ThreadPoolTaskScheduler taskScheduler,
			MirrorSynchronizer<R, P, M> mirrorService) {
		this.repositoryService = repositoryService;
		this.repositoryValidator = repositoryValidator;
		this.repositoryDeleter = repositoryDeleter;
		this.taskScheduler = taskScheduler;
		this.mirrorService = mirrorService;
	}
	
	/**
	 * Creates an entity from repository declared in the configuration.
	 * @param declaredRepository
	 * @return
	 */
	protected abstract E declaredRepositoryToEntity(R declaredRepository);
	
	protected void createRepositoriesFromConfig(List<R> repositories, boolean declarative) {
		List<E> existingRepositories = repositoryService.findAll(); 
		for(R declaredRepository : repositories) {
			E newRepository = declaredRepositoryToEntity(declaredRepository);
			
			Optional<E> possiblyExistingRepository = existingRepositories.stream()
					.filter(r -> r.getName().equals(newRepository.getName())).findFirst();
			if(possiblyExistingRepository.isPresent()) {
				E existingRepository = possiblyExistingRepository.get();
				existingRepositories.remove(existingRepository);
				
				if(Boolean.valueOf(declarative)) {
					if(existingRepository.isDeleted() == null) {
						existingRepository.setDeleted(false);
					}
					if(existingRepository.isPublished() == null) {
						existingRepository.setPublished(true); //TODO: Isn't it safer to set default to true?
					}
					updateRepository(newRepository, existingRepository);
				} else {
					logger.warn("We tried to create one of the preconfigured repositories but "					
							+ "there already is such a repository with the following properties: " 
							+ existingRepository.toString());
				}
			} else {
				if(newRepository.isDeleted() == null) {
					newRepository.setDeleted(false);
				}
				if(newRepository.isPublished() == null) {
					newRepository.setPublished(true); //TODO: Isn't it safer to set default to true?
				}
				
				BindException bindException = new BindException(newRepository, newRepository.getName());
				repositoryValidator.validate(newRepository, bindException);
				
				if(!bindException.hasErrors()) {
					try {
						repositoryService.create(newRepository);
					} catch (CreateEntityException e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					String errorMessage = "Creating a preconfigured repository failed: ";
					for(ObjectError error : bindException.getAllErrors()) {
						errorMessage += StaticMessageResolver.getMessage(error.getCode());
					}
					
					logger.error(errorMessage);
				}
			}
			
			if(Boolean.valueOf(declarative)) {
				existingRepositories.forEach(r -> {
					try {
						repositoryDeleter.delete(r);
					} catch (DeleteEntityException e) {
						logger.error(e.getMessage(), e);
						throw new IllegalStateException();
					}
				});
			}
		}
	}
	
	protected void scheduleMirroring(List<R> repositories) {
		for(R declaredRepository : repositories) {
			for(M mirror : declaredRepository.getMirrors()) {
				if(!mirror.getSyncInterval().isEmpty()) {
					logger.info("Scheduling mirroring for " + declaredRepository.getName() 
					+ " with sync interval: " + mirror.getSyncInterval());
					
					CronTrigger cronTrigger = new CronTrigger(mirror.getSyncInterval());
					taskScheduler.schedule(
							new SynchronizeMirrorTask<R, P, M>(mirrorService, 
									mirror, declaredRepository), cronTrigger);
				}
			}
		}
	}
	
	protected abstract void updateRepository(E newRepository, E existingRepository);
}
