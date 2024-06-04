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
package eu.openanalytics.rdepot.r.api.v2.dtos;

import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.r.entities.RRepository;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringExclude;

@NoArgsConstructor
public class RRepositoryDto extends RepositoryDto {
    @ToStringExclude
    private RRepository entity;

    public RRepositoryDto(RRepository repository, int numberOfPackages) {
        super(repository, numberOfPackages);
        this.entity = repository;
    }

    @Override
    public Resource getEntity() {
        return entity;
    }
}
