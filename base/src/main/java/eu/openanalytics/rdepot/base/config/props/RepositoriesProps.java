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
package eu.openanalytics.rdepot.base.config.props;

import java.util.ArrayList;
import java.util.List;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;

/**
 * Properties class used to fetch declarative mirroring configuration
 * from an application.yaml file. 
 * It has to be extended by technology-specific implementation.
 * @param <R> Repository mirroring configuration entry POJO
 * @param <P> Package mirroring configuration entry POJO
 * @param <M> Mirror POJO
 */
public abstract class RepositoriesProps<R extends MirroredRepository<P, M>, P extends MirroredPackage, M extends Mirror<P>> {
	
	protected List<R> repositories = new ArrayList<>();
	
	public RepositoriesProps() {}
	
	/**
	 * All repositories declared for mirroring configuration.
	 */
	public List<R> getRepositories() {
		return repositories;
	}
	
	public void setRepositories(List<R> repositories) {
		this.repositories = repositories;
	}
}
