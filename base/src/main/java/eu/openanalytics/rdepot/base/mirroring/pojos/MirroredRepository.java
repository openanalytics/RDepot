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
package eu.openanalytics.rdepot.base.mirroring.pojos;

import java.util.HashSet;
import java.util.Set;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.technology.Technology;

public abstract class MirroredRepository<P extends MirroredPackage, M extends Mirror<P>> {
	protected String name;
	protected String publicationUri;
	protected String serverAddress;
	protected Boolean deleted;
	protected Boolean published;
	protected Set<M> mirrors = new HashSet<>();
	protected Technology technology;
	
	public MirroredRepository() {}
	
	public MirroredRepository(String name, String publicationUri, String serverAddress,
			Boolean deleted, Boolean published,	Technology technology) {
		this(name, publicationUri, serverAddress, deleted, published, new HashSet<>(), technology);
	}
	
	public MirroredRepository(String name, String publicationUri, String serverAddress, 
			Boolean deleted, Boolean published,	Set<M> mirrors, Technology technology) {
		super();
		this.name = name;
		this.publicationUri = publicationUri;
		this.serverAddress = serverAddress;
		this.deleted = deleted;
		this.published = published;
		this.mirrors = mirrors;
		this.technology = technology;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPublicationUri() {
		return publicationUri;
	}
	
	public void setPublicationUri(String publicationUri) {
		this.publicationUri = publicationUri;
	}
	
	public String getServerAddress() {
		return serverAddress;
	}
	
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public Boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	public Boolean isPublished() {
		return published;
	}
	
	public void setPublished(Boolean published) {
		this.published = published;
	}
	
	public Set<M> getMirrors() {
		return mirrors;
	}
	
	public void setMirrors(Set<M> mirrors) {
		this.mirrors = mirrors;
	}
	
	public Technology getTechnology() {
		return technology;
	}
	
	public void setTechnology(Technology technology) {
		this.technology = technology;
	}
}
