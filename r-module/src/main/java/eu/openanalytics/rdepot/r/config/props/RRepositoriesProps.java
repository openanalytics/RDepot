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
package eu.openanalytics.rdepot.r.config.props;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.config.props.RepositoriesProps;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;

@Component
@ConfigurationProperties
public class RRepositoriesProps 
	extends RepositoriesProps<MirroredRRepository, MirroredRPackage, CranMirror> {

	public RRepositoriesProps() {}
	
	public void setRepositories(List<MirroredRRepository> repositories) {
		
		for(MirroredRRepository repository : repositories) {
			for(CranMirror mirror : repository.getMirrors()) {
				for(MirroredRPackage packageBag : mirror.getPackages()) {
					if(packageBag.getGenerateManuals() == null) {
						packageBag.setGenerateManuals(mirror.getGenerateManuals() == null ? 
										false : mirror.getGenerateManuals());
					}
				}
			}
		}
		
		super.setRepositories(repositories);
	}
}
