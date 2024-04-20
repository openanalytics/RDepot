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
package eu.openanalytics.rdepot.python.initializer;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.initializer.IRepositoryDataInitializer;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.python.config.declarative.DeclarativePythonRepository;
import eu.openanalytics.rdepot.python.config.declarative.PythonYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.python.validation.PythonRepositoryValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class PythonRepositoryDataInitializer implements IRepositoryDataInitializer {
	
	private final PythonStrategyFactory factory;
	private final UserService userService;
	private final PythonRepositoryService repositoryService;
	private final PythonRepositoryValidator repositoryValidator;
	private final PythonRepositoryDeleter repositoryDeleter;
	private final PythonYamlDeclarativeConfigurationSource declarativeConfigurationSource;
	private final Technology technology = PythonLanguage.instance;

	@Override
	public void createRepositoriesFromConfig(boolean declarative) {
		final List<DeclarativePythonRepository> repositories = 
				declarativeConfigurationSource.retrieveDeclaredRepositories();
		if(repositories == null || repositories.isEmpty()) {
			log.info("There are no declared repositories for technology: " 
					+ technology.getName() + " " + technology.getVersion());
			return;
		}
		
		List<PythonRepository> existingRepositories = repositoryService.findAll();
		for(DeclarativePythonRepository declaredRepository : repositories) {
			PythonRepository newRepository = declaredRepositoryToEntity(declaredRepository, declarative);

			Optional<PythonRepository> possiblyExistingRepository = existingRepositories.stream()
					.filter(r -> r.getName().equals(newRepository.getName())).findFirst();
			if(possiblyExistingRepository.isPresent()) {
				PythonRepository existingRepository = possiblyExistingRepository.get();
				existingRepositories.remove(existingRepository);
				
				if(declarative) {
					updateRepository(newRepository, existingRepository);
				} else {
					log.warn("We tried to create one of the preconfigured repositories but "					
							+ "there already is such a repository with the following properties: " 
							+ existingRepository.toString());
				}
			} else {
				if(newRepository.isDeleted() == null) {
					newRepository.setDeleted(false);
				}
				if(newRepository.getPublished() == null) {
					newRepository.setPublished(true);
				}
				
				BindException bindException = new BindException(newRepository, newRepository.getName());
				repositoryValidator.validate(newRepository, bindException);
				
				if(!bindException.hasErrors()) {
					try {
						repositoryService.create(newRepository);
					} catch (CreateEntityException e) {
						log.error(e.getMessage(), e);
					}
				} else {
					String errorMessage = "Creating a preconfigured repository failed: ";
					for(ObjectError error : bindException.getAllErrors()) {
						errorMessage += StaticMessageResolver.getMessage(error.getCode());
					}
					
					log.error(errorMessage);
				}
			}
		}
		if(declarative) {
			log.info("Declarative mode enabled - removing all non-declared Python repositories...");
			existingRepositories.forEach(r -> {
				log.info("Removing repository: " + r.getName());
				try {
					repositoryDeleter.delete(r);
				} catch (DeleteEntityException e) {
					log.error(e.getMessage(), e);
					throw new IllegalStateException();
				}
			});
		}
	}

	protected PythonRepository declaredRepositoryToEntity(DeclarativePythonRepository declaredRepository, boolean declarative) {
		PythonRepository repository = new PythonRepository();
		repository.setName(declaredRepository.getName());
		repository.setPublicationUri(declaredRepository.getPublicationUri());
		repository.setServerAddress(declaredRepository.getServerAddress());
		
		if(declarative) {
			boolean deleted = false;
			Boolean published = true;
			
			if(declaredRepository.getDeleted() != null)
				deleted = declaredRepository.getDeleted();
			if(declaredRepository.getPublished() != null) 
				published = declaredRepository.getPublished();
			
			repository.setDeleted(deleted);
			repository.setPublished(published);
		}		
		
		return repository;
	}

	protected void updateRepository(PythonRepository newRepository, PythonRepository existingRepository) {
		try {
			User requester = userService.findFirstAdmin();
			factory.updateRepositoryStrategy(existingRepository, requester, newRepository).perform();
		} catch (AdminNotFound e1) {
			log.error("When trying to create a preconfigured repositories, we couldn't find any valid administrator");
		} catch (StrategyFailure e) {
			log.error("We tried to update " + existingRepository.getName() + " repository from preconfigured " 
					+ "repositories but unexpected error occurred");

		}
		
	}
}
