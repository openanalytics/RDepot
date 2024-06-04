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

import eu.openanalytics.rdepot.base.api.v2.dtos.AccessTokenDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing {@link User User's} access token used e.g. to access the CLI.
 */
@Getter
@Setter
@Entity
@Table(name = "access_token", schema = "public")
public class AccessToken extends EventableResource {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Transient
    private String plainValue;

    public AccessToken() {
        super(InternalTechnology.instance, ResourceType.ACCESS_TOKEN);
    }

    public AccessToken(
            String name,
            String value,
            LocalDate creationDate,
            LocalDate expirationDate,
            boolean active,
            boolean deleted) {
        this();
        this.name = name;
        this.value = value;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.active = active;
        this.deleted = deleted;
    }

    public AccessToken(
            int id,
            String name,
            String value,
            LocalDate creationDate,
            LocalDate expirationDate,
            boolean active,
            boolean deleted) {
        this();
        this.id = id;
        this.name = name;
        this.value = value;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.active = active;
        this.deleted = deleted;
    }

    public AccessToken(
            int id,
            String name,
            String value,
            LocalDate creationDate,
            LocalDate expirationDate,
            boolean active,
            boolean deleted,
            User user) {
        this();
        this.id = id;
        this.name = name;
        this.value = value;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.active = active;
        this.user = user;
        this.deleted = deleted;
    }

    public AccessToken(AccessTokenDto dto) {
        this();
        this.id = dto.getId();
        this.name = dto.getName();
        this.value = dto.getValue();
        this.creationDate = LocalDate.parse(dto.getCreationDate());
        this.expirationDate = LocalDate.parse(dto.getExpirationDate());
        this.active = dto.isActive();
        this.deleted = dto.isDeleted();
    }

    public AccessToken(AccessToken that) {
        this();
        this.id = that.id;
        this.name = that.name;
        this.value = that.value;
        this.creationDate = that.creationDate;
        this.expirationDate = that.expirationDate;
        this.active = that.active;
        this.user = that.user;
        this.deleted = that.deleted;
        this.plainValue = that.plainValue;
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public IDto createSimpleDto() {
        return new AccessTokenDto(this);
    }
}
