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
package eu.openanalytics.rdepot.base.entities;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * This object represents the relationship between a {@link User user}
 * and {@link Package packages} in a specific {@link Repository repository}
 * that have certain name.
 * It allows user to have control over a particular kind of package.
 */
@Getter
@Setter
@Entity
@Table(name = "package_maintainer", schema = "public")
public class PackageMaintainer extends EventableResource {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @Column(name = "package", unique = false, nullable = false)
    private String packageName;

    public PackageMaintainer() {
        super(InternalTechnology.instance, ResourceType.PACKAGE_MAINTAINER);
    }

    public PackageMaintainer(PackageMaintainerDto dto, Repository repository, User user) {
        this();
        this.id = dto.getId();
        this.deleted = dto.getDeleted();
        this.packageName = dto.getPackageName();
        this.user = user;
        this.repository = repository;
    }

    public PackageMaintainer(int id, User user, Repository repository, String package_, boolean deleted) {
        this();
        this.id = id;
        this.user = user;
        this.repository = repository;
        this.packageName = package_;
        this.deleted = deleted;
    }

    public PackageMaintainer(PackageMaintainer that) {
        this();
        this.id = that.id;
        this.deleted = that.deleted;
        this.packageName = that.packageName;
        this.repository = that.repository;
        this.user = that.user;
    }

    @Override
    public String toString() {
        final String userLogin = user == null ? "null" : user.getLogin();
        final String repositoryName = repository == null ? "null" : repository.getName();
        return "Package Maintainer (id: " + id + ", user: \"" + userLogin
                + "\", package: \"" + packageName + "\", repository: \""
                + repositoryName + "\")";
    }

    @Override
    public IDto createSimpleDto() {
        return new PackageMaintainerDto(this);
    }
}
