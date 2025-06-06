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
import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.time.DateProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for {@link AccessToken Access Tokens}.
 */
@Data
@NoArgsConstructor
public class AccessTokenDto implements IDto {
    private AccessToken entity;
    private int id;
    private String name;
    private String value;
    private String creationDate;
    private String expirationDate;
    private boolean active;
    private boolean deleted;
    private UserProjection user;
    private String lastUsed;

    public AccessTokenDto(AccessToken entity) {
        this.entity = entity;
        this.id = entity.getId();
        this.name = entity.getName();
        this.value = entity.getPlainValue();
        this.creationDate = DateProvider.instantToTimestamp(entity.getCreationDate());
        this.expirationDate = DateProvider.instantToTimestamp(entity.getExpirationDate());
        this.active = entity.isActive();
        this.deleted = entity.isDeleted();
        this.user = new UserProjection(entity.getUser());
        this.lastUsed = entity.getLastUsed() != null ? DateProvider.instantToTimestamp(entity.getLastUsed()) : "";
    }

    @Override
    @JsonIgnore
    public AccessToken getEntity() {
        return entity;
    }
}
