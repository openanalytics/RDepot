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

public abstract class MirroredPackage {
	protected String name;
	protected String version;
	
	public MirroredPackage() {}
	
	public MirroredPackage(String name, String version) {
		this.name = name;
		this.version = version;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
}
