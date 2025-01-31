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

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for {@link Package Packages}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PackageDto extends PackageSimpleDto {

    protected String description;
    protected String author;
    protected String title;
    protected String url;

    public PackageDto(Package packageBag) {
        super(packageBag);
        this.description = packageBag.getDescription();
        this.author = packageBag.getAuthor();
        this.title = packageBag.getTitle();
        this.url = packageBag.getUrl();
    }

    @Override
    @JsonIgnore
    public Resource getEntity() {
        return this.entity;
    }
}
