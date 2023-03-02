/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;

/**
 * Package service that aggregates packages of all technologies.
 */
@Component
public class CommonPackageService {
	private final List<PackageService<?>> packageServices;
	
	@Autowired
	public CommonPackageService(List<PackageService<?>> packageServices) {
		this.packageServices = packageServices;
	}
	
	public List<Package<?, ?>> findAll() {
		List<Package<?,?>> packages = new ArrayList<>();
		
		for(PackageService<?> service : packageServices) {
			packages.addAll(service.findAll());
		}
		
		return packages;
	}
	
	public List<Package<?,?>> findAllByNameAndRepository(String name, Repository<?,?> repository) {
		List<Package<?,?>> packages = new ArrayList<>();
		
		for(PackageService<?> service : packageServices) {
			packages.addAll(service.findAllByNameAndRepository(name, repository));
		}
		
		return packages;
	}
	
	public List<Package<?,?>> findAllByRepository(Repository<?,?> repository) {
		List<Package<?,?>> packages = new ArrayList<>();
		
		for(PackageService<?> service : packageServices) {
			packages.addAll(service.findAllByRepository(repository));
		}
		
		return packages;
	}
	
	
}
