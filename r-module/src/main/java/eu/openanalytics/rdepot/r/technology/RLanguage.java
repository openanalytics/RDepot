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
package eu.openanalytics.rdepot.r.technology;

import java.util.Objects;

import eu.openanalytics.rdepot.base.mediator.deletion.PackageDeleter;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;

public class RLanguage implements Technology {
	
	public static RLanguage instance;
	
	static {
		instance = new RLanguage();
	}

	@Override
	public Technology getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return "R";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public Boolean isCompatible(String version) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Class<? extends RepositoryService<?>> getRepositoryServiceClass() {
		return RRepositoryService.class;
	}

	@Override
	public Class<? extends PackageService<?>> getPackageServiceClass() {
		return RPackageService.class;
	}
	
	@Override
	public Class<? extends PackageDeleter<?>> getPackageDeleterClass() {
		return RPackageDeleter.class;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getName(), getVersion());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!obj.getClass().isAssignableFrom(RLanguage.class)) return false;
		RLanguage that = (RLanguage) obj;
		return this.getName().equals(that.getName()) 
				&& this.getVersion().equals(that.getVersion());
	}
}
