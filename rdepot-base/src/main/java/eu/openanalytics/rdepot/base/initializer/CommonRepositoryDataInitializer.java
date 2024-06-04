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
package eu.openanalytics.rdepot.base.initializer;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * It triggers {@link IRepositoryDataInitializer Repository Initializers}
 * for all existing technologies and makes initialization status part of healthcheck,
 * i.e. healthcheck will keep failing until all initializers completed their tasks.
 * It is usually called at the start of application.
 */
@Component
public class CommonRepositoryDataInitializer implements HealthIndicator {

    private final List<IRepositoryDataInitializer> repositoryDataInitializers;

    private final String declarative;
    private volatile boolean initializationComplete = false;

    public CommonRepositoryDataInitializer(
            List<IRepositoryDataInitializer> repositoryDataInitializers, @Value("${declarative}") String declarative) {
        this.repositoryDataInitializers = repositoryDataInitializers;
        this.declarative = declarative;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void createRepositoriesFromConfig() {
        initializationComplete = false;
        createRepositoriesFromConfig(Boolean.parseBoolean(declarative));
        initializationComplete = true;
    }

    /**
     * Creates {@link eu.openanalytics.rdepot.base.entities.Repository Repositories}
     * from declared YAML configuration files.
     * It will trigger initializers for all existing technologies.
     * @param declarative should be true if declarative mode is enabled
     */
    public void createRepositoriesFromConfig(boolean declarative) {
        // Although it does not matter this much in production environment
        // it makes it much easier for testing if the order of created repositories is preserved.
        // This line ensures that by simply sorting classes by name.
        repositoryDataInitializers.sort(Comparator.comparing(a -> a.getClass().getName()));
        //
        repositoryDataInitializers.forEach(i -> i.createRepositoriesFromConfig(declarative));
    }

    @Override
    public Health health() {
        return initializationComplete ? Health.up().build() : Health.down().build();
    }
}
