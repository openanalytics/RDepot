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
package eu.openanalytics.rdepot.base.strategy.create;

import java.util.Optional;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;

public class CreateRepositoryMaintainerStrategy 
	extends CreateStrategy<RepositoryMaintainer> {

	private final CommonPackageService packageService;
	private final BestMaintainerChooser bestMaintainerChooser;
	private final RepositoryMaintainerService repositoryMaintainerService;
	
	public CreateRepositoryMaintainerStrategy(RepositoryMaintainer resource, 
			NewsfeedEventService newsfeedEventService,
			RepositoryMaintainerService service, 
			User requester, 
			CommonPackageService packageService,
			BestMaintainerChooser bestMaintainerChooser) {
		super(resource, service, requester, newsfeedEventService);
		this.packageService = packageService;
		this.repositoryMaintainerService = service;
		this.bestMaintainerChooser = bestMaintainerChooser;
	}

	@Override
	protected RepositoryMaintainer actualStrategy() throws StrategyFailure {
		RepositoryMaintainer maintainer;
		Optional<RepositoryMaintainer> existingMaintainer = repositoryMaintainerService.findByRepositoryAndUserAndDeleted(resource.getRepository(), resource.getUser(), true);
		if(existingMaintainer.isPresent()) {
			maintainer = existingMaintainer.get();
			maintainer.setDeleted(false);
		} else {
			maintainer = super.actualStrategy();
		}
		for(Package<?,?> packageBag : packageService.findAllByRepository(resource.getRepository())) {
			try {
				packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
				//TODO: Do we need to republish repository after new maintainer is chosen?
				} catch (NoSuitableMaintainerFound e) {
					logger.error(e.getMessage(), e);
					throw new StrategyFailure(e);
				}
			}
		return maintainer;
	}
	
	@Override
	protected void postStrategy() throws StrategyFailure { }

	@Override
	protected NewsfeedEvent generateEvent(RepositoryMaintainer resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.CREATE, resource);
	}
}