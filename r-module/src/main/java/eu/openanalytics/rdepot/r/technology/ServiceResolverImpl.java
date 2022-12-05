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
package eu.openanalytics.rdepot.r.technology;

import java.util.Map;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.technology.ServiceResolver;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.technology.TechnologyNotSupported;

/**
 * Temporary implementation for service resolver.
 * It supports only R.
 */
@Component
public class ServiceResolverImpl implements ServiceResolver {

	private final Map<Technology, PackageService<?>> packageServicesByTechnology;
	private final Map<Technology, RepositoryService<?>> repositoryServicesByTechnology;
	
	public ServiceResolverImpl(Map<Technology, PackageService<?>> packageServicesByTechnology, 
			Map<Technology, RepositoryService<?>> repositoryServicesByTechnology) {
		this.packageServicesByTechnology = packageServicesByTechnology;
		this.repositoryServicesByTechnology = repositoryServicesByTechnology;
	}
	
	@Override
	public PackageService<?> packageService(Technology technology) throws TechnologyNotSupported {
		PackageService<?> service = packageServicesByTechnology.get(technology);
		
		if(service == null)
			throw new TechnologyNotSupported();
		
		return service;
	}

	@Override
	public RepositoryService<?> repositoryService(Technology technology) throws TechnologyNotSupported {
		RepositoryService<?> service = repositoryServicesByTechnology.get(technology);
		
		if(service == null)
			throw new TechnologyNotSupported();
		
		return service;
	}

}
