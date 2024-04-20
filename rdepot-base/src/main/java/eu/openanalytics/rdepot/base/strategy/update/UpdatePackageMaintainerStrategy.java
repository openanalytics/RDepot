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
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;

/**
 * Updates {@link PackageMaintainer Package Maintainer}.
 * If the {@link Repository} is updated, 
 * all maintainers for all packages inside will be refreshed.
 */
public class UpdatePackageMaintainerStrategy extends UpdateStrategy<PackageMaintainer> {

	private final CommonPackageService packageService;
	private final BestMaintainerChooser bestMaintainerChooser;

	public UpdatePackageMaintainerStrategy(PackageMaintainer resource, 
			NewsfeedEventService eventService,
			Service<PackageMaintainer> service, 
			User requester, 
			PackageMaintainer updatedMaintainer,
			CommonPackageService packageService, 
			BestMaintainerChooser bestMaintainerChooser) {
		super(resource, service, eventService, requester, updatedMaintainer, new PackageMaintainer(resource));
		this.packageService = packageService;
		this.bestMaintainerChooser = bestMaintainerChooser;
	}

	@Override
	protected PackageMaintainer actualStrategy() throws StrategyFailure {
		if (!resource.getPackageName().equals(updatedResource.getPackageName()))
			updatePackage(resource, updatedResource.getPackageName());
		if (resource.getRepository().getId() != updatedResource.getRepository().getId())
			updateRepository(resource, updatedResource.getRepository());
		if (!resource.isDeleted() && updatedResource.isDeleted())
			softDelete(resource);

		return resource;
	}

	private void softDelete(PackageMaintainer maintainer) throws StrategyFailure {
		maintainer.setDeleted(true);
		try {
			for (Package packageBag : packageService.findAllByNameAndRepository(maintainer.getPackageName(),
					maintainer.getRepository())) {
				packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
			}
		} catch (NoSuitableMaintainerFound e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e);
		}

		changedValues.add(new EventChangedVariable("deleted", "false", "true"));
	}

	private void updateRepository(PackageMaintainer resource, Repository repository) throws StrategyFailure {
		resource.setRepository(repository);
		
		List<Package> packagesToRefresh = Stream
				.of(packageService.findAllByRepository(oldResourceCopy.getRepository()),
						packageService.findAllByRepository(resource.getRepository()))
				.flatMap(Collection::stream).collect(Collectors.toList());
		
		refreshPackageMaintainers(packagesToRefresh, resource.getPackageName());
		
		changedValues.add(new EventChangedVariable(
				"repository", oldResourceCopy.getRepository().toString(), repository.toString()));
	}

	private void updatePackage(PackageMaintainer resource, String packageName) throws StrategyFailure {
		resource.setPackageName(packageName);
		List<Package> packages = packageService.findAllByRepository(resource.getRepository());
		refreshPackageMaintainers(new ArrayList<>(packages), resource.getPackageName());
		refreshPackageMaintainers(new ArrayList<>(packages), oldResourceCopy.getPackageName());
		
		changedValues.add(new EventChangedVariable(
				"package", oldResourceCopy.getPackageName(), packageName));
	}

	private void refreshPackageMaintainers(List<Package> packages, String packageName) throws StrategyFailure {
		for (Package packageBag : packages) {
			logger.debug(packageBag.getName());
			logger.debug("resource: " + packageName);
			if (packageBag.getName().equals(packageName)) {
				try {
					packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
				} catch (NoSuitableMaintainerFound e) {
					logger.error(e.getMessage(), e);
					throw new StrategyFailure(e);
				}
			}
		}
	}

	@Override
	protected void postStrategy() throws StrategyFailure {}

	@Override
	public void revertChanges() throws StrategyReversionFailure {}
	
	@Override
	protected NewsfeedEvent generateEvent(PackageMaintainer resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
	}
}
