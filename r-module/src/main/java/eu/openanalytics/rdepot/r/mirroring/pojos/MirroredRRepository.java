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
package eu.openanalytics.rdepot.r.mirroring.pojos;

import java.util.Set;

import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.technology.RLanguage;

public class MirroredRRepository extends MirroredRepository<MirroredRPackage, CranMirror> {

	public MirroredRRepository() {
		this.technology = RLanguage.instance;
	}
	
	public MirroredRRepository(String name, String publicationUri, String serverAddress, 
			Set<CranMirror> mirrors) {
		super(name, publicationUri, serverAddress, mirrors, RLanguage.instance);
	}

}
