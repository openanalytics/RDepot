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

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringExclude;

/**
 * Simplified Data Transfer Object for {@link Package Packages}
 */
@Data
@NoArgsConstructor
public class PackageSimpleDto implements IDto {

    protected Integer id;
    protected UserProjection user;
    protected RepositoryProjection repository;
    protected SubmissionProjection submission;
    protected String name;
    protected String version;
    protected String source;
    protected Boolean active;
    protected Boolean deleted;
    private String technology;

    @ToStringExclude
    protected Package entity;

    public PackageSimpleDto(Package packageBag) {
        this.id = packageBag.getId();
        this.user = new UserProjection(packageBag.getUser());
        this.repository = new RepositoryProjection(packageBag.getRepository());
        this.submission = new SubmissionProjection(packageBag.getSubmission());
        this.version = packageBag.getVersion();
        this.name = packageBag.getName();
        this.source = packageBag.getSource();
        this.active = packageBag.isActive();
        this.deleted = packageBag.isDeleted();
        this.technology = packageBag.getTechnology().getName();
        this.entity = packageBag;
    }

    @Override
    public Resource getEntity() {
        return entity;
    }
}
