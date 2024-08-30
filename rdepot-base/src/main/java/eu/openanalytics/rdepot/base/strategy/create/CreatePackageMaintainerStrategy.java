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
package eu.openanalytics.rdepot.base.strategy.create;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import java.util.Objects;
import java.util.Optional;

/**
 * Creates {@link PackageMaintainer Package Maintainer}.
 */
public class CreatePackageMaintainerStrategy extends CreateStrategy<PackageMaintainer> {

    private final CommonPackageService packageService;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final PackageMaintainerService packageMaintainerService;

    public CreatePackageMaintainerStrategy(
            PackageMaintainer resource,
            NewsfeedEventService newsfeedEventService,
            PackageMaintainerService service,
            User requester,
            CommonPackageService packageService,
            BestMaintainerChooser bestMaintainerChooser) {
        super(resource, service, requester, newsfeedEventService);
        this.packageService = packageService;
        this.packageMaintainerService = service;
        this.bestMaintainerChooser = bestMaintainerChooser;
    }

    @Override
    protected PackageMaintainer actualStrategy() throws StrategyFailure {
        PackageMaintainer maintainer;
        Optional<PackageMaintainer> packageMaintainer = packageMaintainerService.findByPackageAndRepositoryAndDeleted(
                resource.getPackageName(), resource.getRepository());
        if (packageMaintainer.isPresent()) {
            maintainer = packageMaintainer.get();
            maintainer.setDeleted(false);
        } else {
            maintainer = super.actualStrategy();
        }
        try {
            for (Package packageBag : packageService.findAllByRepository(maintainer.getRepository())) {
                if (Objects.equals(packageBag.getName(), maintainer.getPackageName())) {
                    packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
                }
            }
        } catch (NoSuitableMaintainerFound e) {
            throw new StrategyFailure(e);
        }
        return maintainer;
    }

    @Override
    protected NewsfeedEvent generateEvent(PackageMaintainer resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.CREATE, resource);
    }
}
