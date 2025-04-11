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
package eu.openanalytics.rdepot.r.initializer;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.initializer.RepositoryDataInitializer;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.r.config.declarative.RYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RRepositoryDataInitializer
        extends RepositoryDataInitializer<RRepository, MirroredRRepository, MirroredRPackage, CranMirror> {

    private final RStrategyFactory factory;
    private final UserService userService;
    private final StrategyExecutor strategyExecutor;

    public RRepositoryDataInitializer(
            RRepositoryService repositoryService,
            RRepositoryValidator repositoryValidator,
            RRepositoryDeleter repositoryDeleter,
            ThreadPoolTaskScheduler taskScheduler,
            CranMirrorSynchronizer cranMirrorSynchronizer,
            RStrategyFactory factory,
            UserService userService,
            RYamlDeclarativeConfigurationSource rYamlDeclarativeConfigurationSource,
            StrategyExecutor strategyExecutor) {
        super(
                repositoryService,
                repositoryValidator,
                repositoryDeleter,
                taskScheduler,
                cranMirrorSynchronizer,
                rYamlDeclarativeConfigurationSource,
                RLanguage.instance);
        this.factory = factory;
        this.userService = userService;
        this.strategyExecutor = strategyExecutor;
    }

    @Override
    protected RRepository declaredRepositoryToEntity(MirroredRRepository declaredRepository, boolean declarative) {
        RRepository repository = new RRepository();
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
    protected void updateRepository(RRepository newRepository, RRepository existingRepository) {
        try {
            final User requester = userService.findFirstAdmin();
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
