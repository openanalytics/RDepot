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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.entities.Repository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for {@link Repository Repositories}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDto extends RepositorySimpleDto {

    private int numberOfPackages = -1;
    private String lastPublicationTimestamp = "";
    private String lastModifiedTimestamp = "";
    private boolean lastPublicationSuccessful = false;

    public RepositoryDto(
            Repository repository,
            int numberOfPackages,
            String lastPublicationTimestamp,
            String lastModifiedTimestamp,
            boolean lastPublicationSuccessful) {
        super(repository);
        this.numberOfPackages = numberOfPackages;
        this.lastPublicationSuccessful = lastPublicationSuccessful;
        this.lastPublicationTimestamp = lastPublicationTimestamp;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
}
