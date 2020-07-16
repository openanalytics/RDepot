/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.test.component;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public abstract class BaseComponentTest {
	
	public static WebArchive createBaseDeployment() {
		File[] mockitoLibraries = Maven.resolver().resolve("org.mockito:mockito-core:3.1.0").withTransitivity().asFile();
		return ShrinkWrap.create(WebArchive.class, "rdepot-test.war")
				.addAsLibraries(mockitoLibraries)
				.addPackage("eu.openanalytics.rdepot.service")
				.addPackage("eu.openanalytics.rdepot.repository")
				.addPackage("eu.openanalytics.rdepot.storage")
				.addPackage("eu.openanalytics.rdepot.model")
				.addPackage("eu.openanalytics.rdepot.validator")
				.addPackage("eu.openanalytics.rdepot.comparator")
				.addPackage("eu.openanalytics.rdepot.exception")
				.addPackage("eu.openanalytics.rdepot.messaging");
	}
	
	
}
