/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.initializer;

import eu.openanalytics.rdepot.base.config.declarative.DeclarativeConfigurationSource;
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
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.validation.RepositoryValidator;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

@Slf4j
@AllArgsConstructor
public abstract class RepositoryDataInitializer<
                E extends Repository,
                R extends MirroredRepository<P, M>,
                P extends MirroredPackage,
                M extends Mirror<P>>
        implements IRepositoryDataInitializer {

    private final RepositoryService<E> repositoryService;
    private final RepositoryValidator<E> repositoryValidator;
    private final RepositoryDeleter<E, ?> repositoryDeleter;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final MirrorSynchronizer<R, P, M> mirrorService;
    private final DeclarativeConfigurationSource<R, P, M> declarativeConfigurationSource;
    private final Technology technology;

    /**
     * Creates an entity from repository declared in the configuration.
     */
    protected abstract E declaredRepositoryToEntity(R declaredRepository, boolean declarative);

    @Override
    public void createRepositoriesFromConfig(boolean declarative) {
        final List<R> repositories = declarativeConfigurationSource.retrieveDeclaredRepositories();
        if (repositories == null || repositories.isEmpty()) {
            log.info(
                    "There are no declared repositories for technology: {} {}",
                    technology.getName(),
                    technology.getVersion());
            return;
        }

        List<E> undeclaredRepositories = manageDeclaredRepositories(repositories, declarative);

        if (declarative) {
            undeclaredRepositories.forEach(r -> {
                log.info(
                        "Declarative mode enabled - deleting undeclared repository '{}'...",
                        r.getTechnology().getName());
                try {
                    repositoryDeleter.delete(r);
                } catch (DeleteEntityException e) {
                    log.error(e.getMessage(), e);
                    throw new IllegalStateException();
                }
            });
            log.info("All undeclared repositories deleted successfully.");
        }
        scheduleMirroring(repositories);
    }

    private List<E> manageDeclaredRepositories(List<R> repositories, boolean declarative) {
        List<E> remainingRepositories = repositoryService.findAll();
        for (R declaredRepository : repositories) {
            E newRepository = declaredRepositoryToEntity(declaredRepository, declarative);

            Optional<E> possiblyExistingRepository = remainingRepositories.stream()
                    .filter(r -> r.getName().equals(newRepository.getName()))
                    .findFirst();
            if (possiblyExistingRepository.isPresent()) {
                E existingRepository = possiblyExistingRepository.get();

                // remove declared repository from the "remaining" list
                remainingRepositories.remove(existingRepository);

                if (declarative) {
                    updateRepository(newRepository, existingRepository);
                } else {
                    log.warn(
                            "We tried to create one of the preconfigured repositories but there already is such a repository with the following properties: {}",
                            existingRepository);
                }
            } else {
                if (newRepository.isDeleted() == null) {
                    newRepository.setDeleted(false);
                }
                if (newRepository.getPublished() == null) {
                    newRepository.setPublished(true);
                }
                validateNewRepository(newRepository);
            }
        }
        // return the remaining (i.e. undeclared) repositories
        return remainingRepositories;
    }

    private void validateNewRepository(E newRepository) {
        BindException bindException = new BindException(newRepository, newRepository.getName());
        repositoryValidator.validate(newRepository, bindException);

        if (!bindException.hasErrors()) {
            try {
                log.debug("Creating {} repository {}", newRepository.getTechnology(), newRepository);
                E created = repositoryService.create(newRepository);
                log.debug("Created repository {}", created.toString());
            } catch (CreateEntityException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            StringBuilder errorMessage = new StringBuilder("Creating a preconfigured repository failed: ");
            for (ObjectError error : bindException.getAllErrors()) {
                errorMessage.append(StaticMessageResolver.getMessage(error.getCode()));
            }

            log.error(errorMessage.toString());
        }
    }

    protected void scheduleMirroring(List<R> repositories) {
        log.info("Scheduling mirroring for declared repositories...");
        for (R declaredRepository : repositories) {
            for (M mirror : declaredRepository.getMirrors()) {
                if (mirror.getSyncInterval().isEmpty()) continue;
                log.info(
                        "Scheduling mirroring for {} with sync interval: {}",
                        declaredRepository.getName(),
                        mirror.getSyncInterval());

                CronTrigger cronTrigger = new CronTrigger(mirror.getSyncInterval());
                taskScheduler.schedule(
                        new SynchronizeMirrorTask<>(mirrorService, declaredRepository, mirror), cronTrigger);
            }
        }
        log.info("Mirroring scheduled for all repositories.");
    }

    protected abstract void updateRepository(E newRepository, E existingRepository);
}
