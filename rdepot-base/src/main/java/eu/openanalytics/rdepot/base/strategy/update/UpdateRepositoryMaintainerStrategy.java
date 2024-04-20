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
package eu.openanalytics.rdepot.base.strategy.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;

/**
 * Updates {@link RepositoryMaintainer Repository Maintainer}.
 * If situation requires it, 
 * the maintainer will be updated for every {@link Package} inside.
 */
public class UpdateRepositoryMaintainerStrategy extends UpdateStrategy<RepositoryMaintainer> {

	private final BestMaintainerChooser bestMaintainerChooser;
	private final CommonPackageService packageService;
	
	public UpdateRepositoryMaintainerStrategy(RepositoryMaintainer resource,
			NewsfeedEventService eventService,
			RepositoryMaintainerService service, User requester, 
			RepositoryMaintainer updatedResource,
			CommonPackageService packageService,
			BestMaintainerChooser bestMaintainerChooser) {
		super(resource, service, eventService, requester, updatedResource, new RepositoryMaintainer(resource));
		this.packageService = packageService;
		this.bestMaintainerChooser = bestMaintainerChooser;
	}

	@Override
	protected RepositoryMaintainer actualStrategy() throws StrategyFailure {
		if(resource.getRepository().getId() != updatedResource.getRepository().getId())
			updateRepository(resource, updatedResource.getRepository());
		if(resource.getUser().getId() != updatedResource.getUser().getId())
			resource.setUser(updatedResource.getUser());
		if(!resource.isDeleted() && updatedResource.isDeleted())
			softDelete(resource);
		
		return resource;
	}

	private void softDelete(RepositoryMaintainer resource) throws StrategyFailure {
		resource.setDeleted(true);
		try {
			bestMaintainerChooser.refreshMaintainerForPackages(
                    new ArrayList<>(packageService.findAllByRepository(resource.getRepository()))
				);
		} catch (NoSuitableMaintainerFound e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e);
		}
		
		changedValues.add(new EventChangedVariable("deleted", "false", "true"));
	}

	private void updateRepository(RepositoryMaintainer resource, Repository repository) 
			throws StrategyFailure {
		Repository oldRepository = resource.getRepository();
		resource.setRepository(repository);
		List<Package> packages = Stream.of(
				packageService.findAllByRepository(oldRepository),
				packageService.findAllByRepository(repository))
			.flatMap(Collection::stream).collect(Collectors.toList());
		
		try {
			bestMaintainerChooser.refreshMaintainerForPackages(packages);
		} catch(NoSuitableMaintainerFound e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e);
		}
		
		changedValues.add(new EventChangedVariable(
				"repository", oldRepository.toString(), repository.toString()));
	}

	@Override
	protected void postStrategy() throws StrategyFailure {}

	@Override
	public void revertChanges() throws StrategyReversionFailure {}

	@Override
	protected NewsfeedEvent generateEvent(RepositoryMaintainer resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
	}

}
