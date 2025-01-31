/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringExclude;

/**
 * Data Transfer Object for {@link RepositoryMaintainer Repository Maintainers}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryMaintainerDto implements IDto {
    private Integer id = 0;
    private UserProjection user;
    private RepositoryProjection repository;
    private Boolean deleted = false;

    @ToStringExclude
    private RepositoryMaintainer entity;

    public RepositoryMaintainerDto(RepositoryMaintainer repositoryMaintainer) {
        this.entity = repositoryMaintainer;
        this.id = repositoryMaintainer.getId();
        this.user = new UserProjection(repositoryMaintainer.getUser());
        this.repository = new RepositoryProjection(repositoryMaintainer.getRepository());
        this.deleted = repositoryMaintainer.isDeleted();
    }

    @Override
    public RepositoryMaintainer getEntity() {
        return entity;
    }
}
