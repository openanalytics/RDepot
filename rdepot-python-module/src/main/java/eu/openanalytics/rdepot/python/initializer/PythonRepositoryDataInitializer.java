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
package eu.openanalytics.rdepot.python.initializer;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.initializer.RepositoryDataInitializer;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.python.config.declarative.PythonYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.mirroring.PypiMirror;
import eu.openanalytics.rdepot.python.mirroring.PypiMirrorSynchronizer;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonRepository;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.python.validation.PythonRepositoryValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PythonRepositoryDataInitializer
        extends RepositoryDataInitializer<
                PythonRepository, MirroredPythonRepository, MirroredPythonPackage, PypiMirror> {

    private final PythonStrategyFactory factory;
    private final UserService userService;
    private final StrategyExecutor strategyExecutor;

    public PythonRepositoryDataInitializer(
            PythonRepositoryService repositoryService,
            PythonRepositoryValidator repositoryValidator,
            PythonRepositoryDeleter repositoryDeleter,
            ThreadPoolTaskScheduler taskScheduler,
            PypiMirrorSynchronizer pypiMirrorSynchronizer,
            PythonStrategyFactory factory,
            UserService userService,
            PythonYamlDeclarativeConfigurationSource pythonYamlDeclarativeConfigurationSource,
            StrategyExecutor strategyExecutor) {
        super(
                repositoryService,
                repositoryValidator,
                repositoryDeleter,
                taskScheduler,
                pypiMirrorSynchronizer,
                pythonYamlDeclarativeConfigurationSource,
                PythonLanguage.instance);
        this.factory = factory;
        this.userService = userService;
        this.strategyExecutor = strategyExecutor;
    }

    @Override
    protected PythonRepository declaredRepositoryToEntity(
            MirroredPythonRepository declaredRepository, boolean declarative) {
        PythonRepository repository = new PythonRepository();
        repository.setName(declaredRepository.getName());
        repository.setPublicationUri(declaredRepository.getPublicationUri());
        repository.setServerAddress(declaredRepository.getServerAddress());

        if (declarative) {
            boolean deleted = false;
            boolean published = true;
            boolean requiresAuthentication = true;

            if (declaredRepository.getDeleted() != null) deleted = declaredRepository.getDeleted();
            if (declaredRepository.getPublished() != null) published = declaredRepository.getPublished();
            if (declaredRepository.getRequiresAuthentication() != null)
                requiresAuthentication = declaredRepository.getRequiresAuthentication();

            repository.setDeleted(deleted);
            repository.setPublished(published);
            repository.setRequiresAuthentication(requiresAuthentication);
        }

        return repository;
    }

    @Override
    protected void updateRepository(PythonRepository newRepository, PythonRepository existingRepository) {
        try {
            User requester = userService.findFirstAdmin();
            final Strategy<?> strategy = factory.updateRepositoryStrategy(existingRepository, requester, newRepository);
            strategyExecutor.execute(strategy);
        } catch (AdminNotFound e1) {
            log.error("When trying to create a configured repository, no valid administrators were found");
        } catch (StrategyFailure e) {
            log.error(
                    "When trying to update repository {} based on the declarative configuration, an unexpected error occurred",
                    existingRepository.getName());
        }
    }
}
