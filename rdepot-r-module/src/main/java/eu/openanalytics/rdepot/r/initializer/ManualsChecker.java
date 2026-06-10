/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.population.implementations.RLocalPopulator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ManualsChecker implements HealthIndicator {

    private final String checkManuals;
    private volatile boolean initializationComplete = false;
    private final RPackageService packageService;
    private final RLocalPopulator populator;

    public ManualsChecker(
            RPackageService packageService,
            @Value("${on-start-up.check-manuals}") String checkManuals,
            RLocalPopulator populator) {
        this.packageService = packageService;
        this.checkManuals = checkManuals;
        this.populator = populator;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void checkForManuals() {
        initializationComplete = false;
        if (Boolean.parseBoolean(checkManuals)) {
            List<RPackage> packages = packageService.findAll();
            for (RPackage packageBag : packages) {
                packageBag.setManualAvailable(populator.checkIfManualExists(packageBag.getManualPath()));
            }
        }
        initializationComplete = true;
    }

    @Override
    public Health health() {
        return initializationComplete ? Health.up().build() : Health.down().build();
    }
}
