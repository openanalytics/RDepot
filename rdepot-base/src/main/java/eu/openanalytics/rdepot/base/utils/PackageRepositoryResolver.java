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
package eu.openanalytics.rdepot.base.utils;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Package;

/**
 * Resolves technology-specific {@link Repository} for technology-specific {@link Package}.
 * @param <R> Technology-specific Repository type
 * @param <P> Technology-specific Package type
 */
public interface PackageRepositoryResolver<R extends Repository, P extends Package> {
	
	/**
	 * Resolves technology-specific {@link Repository} for technology-specific {@link Package}.
	 * It has to be implemented by technology extension in order 
	 * to enable the use of technology-specific properties in child component classes.
	 * It allows it without requiring the {@link Package} abstract entity
	 * to include generic {@link Repository} type in its definition,
	 * therefore de-coupling these two elements.
	 */
	R getRepositoryForPackage(P packageBag);
}
