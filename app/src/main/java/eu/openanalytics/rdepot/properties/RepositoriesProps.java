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
package eu.openanalytics.rdepot.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.model.Mirror;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Package;

@Component
@ConfigurationProperties
public class RepositoriesProps {
	
	private List<Repository> repositories;

	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<Repository> repositories) {
		
		for(Repository repository : repositories) {
			for(Mirror mirror : repository.getMirrors()) {
				for(Package packageBag : mirror.getPackages()) {
					if(packageBag.getGenerateManuals() == null) {
						packageBag.setGenerateManuals(
								mirror.getGenerateManuals() == null ? 
										false : mirror.getGenerateManuals());
					}
				}
			}
		}
		
		this.repositories = repositories;
	}
}
