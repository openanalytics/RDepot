/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "repository", schema = "public")
public class Repository implements java.io.Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 676842493249270089L;
	private int id;
	private int version = 0;
	private String publicationUri;
	private String name;
	private String serverAddress = "127.0.0.1";
	private boolean deleted = false;
	private boolean published = false;
	private Set<Package> packages = new HashSet<Package>(0);
	private Set<PackageMaintainer> packageMaintainers = new HashSet<PackageMaintainer>(0);
	private Set<RepositoryMaintainer> repositoryMaintainers = new HashSet<RepositoryMaintainer>(0);
	private Set<RepositoryEvent> repositoryEvents = new HashSet<RepositoryEvent>(0);
	
	public Repository(Repository that) {
		this.id = that.id;
		this.version = that.version;
		this.publicationUri = that.publicationUri;
		this.name = that.name;
		this.serverAddress = that.serverAddress;
		this.deleted = that.deleted;
		this.published = that.published;
		this.packages = that.packages;
		this.packageMaintainers = that.packageMaintainers;
		this.repositoryMaintainers = that.repositoryMaintainers;
		this.repositoryEvents = that.repositoryEvents;
	}
	
	public Repository()
	{
	}

	public Repository(int id, String publicationUri, String name,
			String serverAddress, boolean published, boolean deleted)
	{
		this.id = id;
		this.publicationUri = publicationUri;
		this.name = name;
		this.serverAddress = serverAddress;
		this.published = published;
		this.deleted = deleted;
	}
	
	public Repository(int id, String publicationUri, String name,
			String serverAddress, boolean published, boolean deleted, Set<Package> packages, 
			Set<PackageMaintainer> packageMaintainers, Set<RepositoryMaintainer> repositoryMaintainers, 
			Set<RepositoryEvent> repositoryEvents)
	{
		this.id = id;
		this.publicationUri = publicationUri;
		this.name = name;
		this.serverAddress = serverAddress;
		this.packages = packages;
		this.packageMaintainers = packageMaintainers;
		this.repositoryMaintainers = repositoryMaintainers;
		this.repositoryEvents = repositoryEvents;
		this.deleted = deleted;
		this.published = published;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Column(name = "version", nullable = false)
	public int getVersion()
	{
		return this.version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@Column(name = "publication_uri", unique = true, nullable = false)
	public String getPublicationUri()
	{
		return this.publicationUri;
	}

	public void setPublicationUri(String publicationUri)
	{
		this.publicationUri = publicationUri;
	}

	@Column(name = "name", unique = true, nullable = false)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "server_address", nullable = false)
	public String getServerAddress()
	{
		return this.serverAddress;
	}

	public void setServerAddress(String serverAddress)
	{
		this.serverAddress = serverAddress;
	}
	
	// Could this be done differently?
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "repository")
	//@JsonBackReference(value="repository-packages")
	//@JsonIgnore
	public Set<Package> getPackages()
	{
		Set<Package> packages = new HashSet<Package>();
		for(Package p : this.packages)
		{
			packages.add(p);
		}
		return packages;
	}

	@Transient
	@ElementCollection(targetClass = Package.class)
	public Set<Package> getNonDeletedPackages()
	{
		Set<Package> nonDeletedPackages = new HashSet<Package>();
		Set<Package> packages = getPackages();
		for(Package p : packages)
		{
			if(!p.isDeleted())
				nonDeletedPackages.add(p);
		}
		return nonDeletedPackages;
	}
	
	public void setPackages(Set<Package> packages)
	{
		this.packages = packages;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
	//@JsonBackReference(value="repository-package-maintainers")
	//@JsonIgnore
	public Set<PackageMaintainer> getPackageMaintainers()
	{
		return this.packageMaintainers;
	}

	public void setPackageMaintainers(Set<PackageMaintainer> packageMaintainers)
	{
		this.packageMaintainers = packageMaintainers;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
	//@JsonBackReference(value="repository-maintainers")
	@JsonIgnore
	public Set<RepositoryMaintainer> getRepositoryMaintainers()
	{
		return this.repositoryMaintainers;
	}

	public void setRepositoryMaintainers(Set<RepositoryMaintainer> repositoryMaintainers)
	{
		this.repositoryMaintainers = repositoryMaintainers;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
	//@JsonBackReference(value="repository-events")
	//@JsonIgnore
	public Set<RepositoryEvent> getRepositoryEvents()
	{
		return this.repositoryEvents;
	}

	public void setRepositoryEvents(Set<RepositoryEvent> repositoryEvents)
	{
		this.repositoryEvents = repositoryEvents;
	}
	
	@Column(name = "deleted", nullable = false)
	public boolean isDeleted()
	{
		return this.deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public boolean isPublished() 
	{
		return published;
	}
	
	@Column(name = "published", nullable = false)
	public void setPublished(boolean published) 
	{
		this.published = published;
	}
	
	@Override
	public String toString() {
		return "Name: " + this.name + ", publication URI: " + this.publicationUri + ", server address: " + this.serverAddress;		
	}
}
