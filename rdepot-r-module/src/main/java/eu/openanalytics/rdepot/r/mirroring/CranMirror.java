/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.mirroring;

import java.util.List;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CranMirror extends Mirror<MirroredRPackage> {
	
	private Boolean generateManuals = false;
	
	@Override
	public void setPackages(List<MirroredRPackage> packages) {
		for(MirroredRPackage packageBag : packages) {
			if(packageBag.getGenerateManuals() == null) {
				packageBag.setGenerateManuals(
						getGenerateManuals() != null && getGenerateManuals());
			}
		}

		super.setPackages(packages);
	}
}
