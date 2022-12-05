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
package eu.openanalytics.rdepot.r.mirroring;

import java.util.List;
import java.util.Objects;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;

public class CranMirror extends Mirror<MirroredRPackage> {
	
	private Boolean generateManuals = false;
	
	public CranMirror() {}
	
	public CranMirror(String name, String uri, String syncInterval, 
			String type, List<MirroredRPackage> packages, Boolean generateManuals) {
		super(name, uri, syncInterval, type, packages);
		this.generateManuals = generateManuals;
	}

	public Boolean getGenerateManuals() {
		return generateManuals;
	}
	
	public void setGenerateManuals(Boolean generateManuals) {
		this.generateManuals = generateManuals;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(generateManuals);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof CranMirror)) {
			return false;
		}
		CranMirror other = (CranMirror) obj;
		return Objects.equals(generateManuals, other.generateManuals);
	}
}
