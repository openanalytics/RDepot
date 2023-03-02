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
package eu.openanalytics.rdepot.r.api.v2.dtos;

import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RRepositoryDto extends RepositoryDto<RRepositoryDto, RRepository> {

	public RRepositoryDto(RRepository repository) {
		super(repository);
	}
	
	public RRepositoryDto() {}
	
	@Override
	public RRepository getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEntity(RRepository entity) {
//		super.set
	}

}
