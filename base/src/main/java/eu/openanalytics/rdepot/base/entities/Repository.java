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
package eu.openanalytics.rdepot.base.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.technology.Technology;

/**
 * Entity representing a repository.
 * It should be extending depending on the requirements of the implemented technology.
 * @param <E> Extended repository entity (e.g. for R it is {@link RRepository}).
 * @param <D> Extended {@link IDto DTO} object (e.g. for R it is {@link RRepositoryDto}).
 */
@Entity
@Table(name = "repository", schema = "public")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Repository<E extends Repository<E, D>, D extends RepositoryDto<D, E>> 
	extends Resource implements IEntity<D> {	

	@Column(name = "version", nullable = false)
	private Integer version = 0;
	
	@Column(name = "publication_uri", unique = true, nullable = false)
	private String publicationUri;
	
	@Column(name = "name", unique = true, nullable = false)
	private String name;
	
	@Column(name = "server_address", nullable = false)
	private String serverAddress;
	
	@Column(name = "published", nullable = false)
	private Boolean published = false;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
	private Set<PackageMaintainer> packageMaintainers = new HashSet<PackageMaintainer>(0);
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
	private Set<RepositoryMaintainer> repositoryMaintainers = new HashSet<RepositoryMaintainer>(0);
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "repository") //TODO: Replace with "LAZY" in the future release
	private List<Package<?,?>> packages;
	
	@Transient
	protected Set<Mirror<?>> mirrors = new HashSet<>();
	
	@Transient
	private Boolean synchronizing = false;
	
	@Transient
	public Boolean isSynchronizing() {
		return synchronizing;
	}

	@Transient
	public void setSynchronizing(Boolean synchronizing) {
		this.synchronizing = synchronizing;
	}

	public Repository(Repository<E, D> that) {
		super(that.id, that.getTechnology(), ResourceType.REPOSITORY);
		this.version = that.version;
		this.publicationUri = that.publicationUri;
		this.name = that.name;
		this.serverAddress = that.serverAddress;
		this.deleted = that.deleted;
		this.published = that.published;
		this.packageMaintainers = that.packageMaintainers;
		this.repositoryMaintainers = that.repositoryMaintainers;
	}
	
	public Repository(Technology technology) {
		super(technology, ResourceType.REPOSITORY);
	}
	
	public Repository(Technology technology, D repositoryDto) {
		this(technology);
		this.id = repositoryDto.getId();
		this.version = repositoryDto.getVersion();
		this.publicationUri = repositoryDto.getPublicationUri();
		this.serverAddress = repositoryDto.getServerAddress();
		this.name = repositoryDto.getName();
		this.deleted = repositoryDto.isDeleted();
		this.published = repositoryDto.isPublished();
		this.synchronizing = repositoryDto.isSynchronizing();
	}

	public Repository(Technology technology, int id, String publicationUri, String name,
			String serverAddress, boolean published, boolean deleted) {
		super(id, technology, ResourceType.REPOSITORY);
		this.publicationUri = publicationUri;
		this.name = name;
		this.serverAddress = serverAddress;
		this.published = published;
		this.deleted = deleted;
	}
	
	public Repository(Technology technology, int id, String publicationUri, String name,
			String serverAddress, boolean published, boolean deleted, 
			Set<PackageMaintainer> packageMaintainers, Set<RepositoryMaintainer> repositoryMaintainers) {
		super(id, technology, ResourceType.REPOSITORY);
		this.publicationUri = publicationUri;
		this.name = name;
		this.serverAddress = serverAddress;
		this.packageMaintainers = packageMaintainers;
		this.repositoryMaintainers = repositoryMaintainers;
		this.deleted = deleted;
		this.published = published;
	}
	
	/**
	 * Current version of repository.
	 * It is a number incremented whenever a change affecting the repository is made.
	 */
	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	/**
	 * URI of the site where published packages can be found for the end users.
	 * It is different than {@link #getServerAddress() server address} in a sense
	 * that publication URI is not used to push packages to destination repository
	 * but rather serves as an information for the users.
	 */
	public String getPublicationUri() {
		return this.publicationUri;
	}

	public void setPublicationUri(String publicationUri) {
		this.publicationUri = publicationUri;
	}
	
	/**
	 * <b>Unique</b> name of the repository.
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Repository publication server address.
	 * It is used internally to communicate with RDepot Repo app via RESTful API 
	 * and push published repositories there.
	 */
	public String getServerAddress() {
		return this.serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public Set<PackageMaintainer> getPackageMaintainers() {
		return this.packageMaintainers;
	}

	public void setPackageMaintainers(Set<PackageMaintainer> packageMaintainers) {
		this.packageMaintainers = packageMaintainers;
	}
	
	public Set<RepositoryMaintainer> getRepositoryMaintainers() {
		return this.repositoryMaintainers;
	}

	public void setRepositoryMaintainers(Set<RepositoryMaintainer> repositoryMaintainers) {
		this.repositoryMaintainers = repositoryMaintainers;
	}
		
	/**
	 * When set to {@value true}, repository will be pushed to 
	 * @return
	 */
	public Boolean isPublished() {
		return published;
	}
		
	public void setPublished(boolean published) {
		this.published = published;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name + ", publication URI: " + this.publicationUri + ", server address: " + this.serverAddress;		
	}

	/**
	 * Mirrors defined in application.yaml
	 */
	@Transient
	public Set<Mirror<?>> getMirrors() {
		return mirrors;
	}
	
	public void setPackages(List<Package<?, ?>> packages) {
		this.packages = packages;
	}
	
	public List<Package<?, ?>> getPackages() {
		return packages;
	}
}
