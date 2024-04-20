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
package eu.openanalytics.rdepot.base.technology;

/**
 * Interface used to identify programming language or technology which is supported by a module.
 */
public interface Technology {

	/**
	 * @return current instance of technology class used by a resource
	 */
	Technology getInstance();
	
	/**
	 * @return name of the technology
	 */
	String getName();
	
	/**
	 * @return version of the technology
	 */
	String getVersion();
	
	/**
	 * Checks if current technology version is compatible with the given one.
	 */
	Boolean isCompatible(String version);
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(Object obj);
}