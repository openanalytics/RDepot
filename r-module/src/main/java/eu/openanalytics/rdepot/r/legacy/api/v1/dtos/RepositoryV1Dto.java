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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RepositoryV1Dto{
	
	private int id;
	private int version;
	private String publicationUri;
	private String name;
	private String serverAddress;
	private boolean deleted;
	private boolean published;
	private List<PackageV1Dto> packages;
	private List<RepositoryMaintainerProjection> repositoryMaintainers;
	
	public RepositoryV1Dto(RRepository repository) {
		this.id = repository.getId();
		this.version = repository.getVersion();
		this.publicationUri = repository.getPublicationUri();
		this.name = repository.getName();
		this.serverAddress = repository.getServerAddress();
		this.deleted = repository.isDeleted();
		this.published = repository.isPublished();
		
		Comparator<PackageV1Dto> sortPackagesByNameAndThenById
		 = Comparator.comparing(PackageV1Dto::getName)
		            .thenComparingInt(PackageV1Dto::getId);
		
		Comparator<RepositoryMaintainerProjection> sortRepositoryMaintainersById
			= Comparator.comparing(RepositoryMaintainerProjection::getId);
		
		if(repository.getPackages() != null) {
			this.packages = repository.getPackages()
					.stream()
					.map(packagebag -> new PackageV1Dto((RPackage) packagebag))
					.sorted(sortPackagesByNameAndThenById)
					.collect(Collectors.toList());
				
		} else {
			this.packages = null;
		}
				
		if(repository.getRepositoryMaintainers() != null) {
			this.repositoryMaintainers = 
				repository.getRepositoryMaintainers().stream()
				.map(RepositoryMaintainerProjection::of)
				.sorted(sortRepositoryMaintainersById)
				.collect(Collectors.toList());			
		} else {
			this.repositoryMaintainers = null;
		}
	}
	
	static RepositoryV1Dto of(RRepository repository) {
		return new RepositoryV1Dto(repository);
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public int getVersion() {
		return version;
	}

	public String getPublicationUri() {
		return publicationUri;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isPublished() {
		return published;
	}

	public List<PackageV1Dto> getPackages() {
		return packages;
	}

	public List<RepositoryMaintainerProjection> getRepositoryMaintainers() {
		return repositoryMaintainers;
	}
}