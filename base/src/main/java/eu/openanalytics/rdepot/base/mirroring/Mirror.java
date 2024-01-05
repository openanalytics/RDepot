/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.mirroring;

import java.util.List;
import java.util.Objects;

import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;

/**
 * POJO representing mirroring configuration for a single mirror.
 * It has to be extended by technology-specific implementation.
 * @param <P> Package mirroring entry POJO
 */
public abstract class Mirror<P extends MirroredPackage> {
	
	protected String name;
	protected String uri;
	protected String syncInterval;
	protected String type;
	protected List<P> packages;
	
	public Mirror() {}
	
	public Mirror(String name, String uri, String syncInterval, String type, List<P> packages) {
		super();
		this.name = name;
		this.uri = uri;
		this.syncInterval = syncInterval;
		this.type = type;
		this.packages = packages;
	}

	public String getName() {
		return name;
	}
	
	public String getUri() {
		return uri;
	}
	
	public String getSyncInterval() {
		return syncInterval;
	}

	public String getType() {
		return type;
	}
	
	public List<P> getPackages() {
		return packages;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setSyncInterval(String syncInterval) {
		this.syncInterval = syncInterval;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPackages(List<P> packages) {
		this.packages = packages;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, packages, syncInterval, type, uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Mirror)) {
			return false;
		}
		Mirror<?> other = (Mirror<?>) obj;
		return Objects.equals(name, other.name) && Objects.equals(packages, other.packages)
				&& Objects.equals(syncInterval, other.syncInterval) && Objects.equals(type, other.type)
				&& Objects.equals(uri, other.uri);
	}
}
