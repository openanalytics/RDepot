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
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * This object represents the relationship between a {@link User user}
 * and a {@link Repository repository}.
 * It allows user to have control over a particular kind of repository
 * and packages within.
 */
@Getter
@Setter
@Entity
@Table(name = "repository_maintainer", schema = "public")
public class RepositoryMaintainer extends EventableResource implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 8173718643254959397L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    public RepositoryMaintainer() {
        super(InternalTechnology.instance, ResourceType.REPOSITORY_MAINTAINER);
    }

    public RepositoryMaintainer(RepositoryMaintainerDto dto, Repository repository, User user) {
        this();
        this.id = dto.getId();
        this.deleted = dto.getDeleted();
        this.repository = repository;
        this.user = user;
    }

    public RepositoryMaintainer(int id, User user, Repository repository, boolean deleted) {
        this();
        this.id = id;
        this.user = user;
        this.repository = repository;
        this.deleted = deleted;
    }

    public RepositoryMaintainer(RepositoryMaintainer that) {
        this();
        this.id = that.id;
        this.deleted = that.isDeleted();
        this.repository = that.getRepository();
        this.user = that.getUser();
    }

    @Override
    public String toString() {
        return "Repository Maintainer (id: " + id + ", repository: \"" + repository.getName() + "\", user: \""
                + user.getLogin() + "\")";
    }

    @Override
    public IDto createSimpleDto() {
        return new RepositoryMaintainerDto(this);
    }
}
