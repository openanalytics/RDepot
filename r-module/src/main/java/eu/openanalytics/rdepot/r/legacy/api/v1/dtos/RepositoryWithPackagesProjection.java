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
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RepositoryWithPackagesProjection {
	private int id;	
	private String name;
	private String publicationUri;
	private List<PackageProjectionShort> packages;
	
	public RepositoryWithPackagesProjection(RRepository repository) {
		this.id = repository.getId();		
		this.name = repository.getName();
		this.publicationUri = repository.getPublicationUri();
		this.packages = repository.getPackages()
				.stream()
				.map(package_ -> new PackageProjectionShort((RPackage) package_))
				.collect(Collectors.toList());
	}
	
	public RepositoryWithPackagesProjection(RepositoryV1Dto repository) {
		this.id = repository.getId();
		this.name = repository.getName();
		this.publicationUri = repository.getPublicationUri();
	}
	
	public static RepositoryWithPackagesProjection of(RRepository repository) {
		return new RepositoryWithPackagesProjection(repository);
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPublicationUri() {
		return publicationUri;
	}

	public List<PackageProjectionShort> getPackages() {
		return packages;
	}
}
