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
package eu.openanalytics.rdepot.r.mirroring.pojos;

import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.r.entities.RPackage;

public class MirroredRPackage extends MirroredPackage {

	private Boolean generateManuals;
	
	public MirroredRPackage() {}
	
	public MirroredRPackage(String name, String version, Boolean generateManuals) {
		super(name, version);
		this.generateManuals = generateManuals;
	}
	
	public Boolean getGenerateManuals() {
		return generateManuals;
	}
	
	public void setGenerateManuals(Boolean generateManuals) {
		this.generateManuals = generateManuals;
	}
	
	public RPackage toPackageEntity() {
		RPackage entity = new RPackage();
		entity.setName(name);
		entity.setVersion(version);
		entity.setGenerateManuals(generateManuals);
		
		return entity;
	}
}
