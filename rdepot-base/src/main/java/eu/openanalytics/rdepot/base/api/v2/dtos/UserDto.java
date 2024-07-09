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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.time.DateProvider;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringExclude;

/**
 * Data Transfer Object for {@link User Users}
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto implements IDto {

    private Integer id;
    private String name;
    private String email;
    private String login;
    private Boolean active;
    private String lastLoggedInOn;
    private String createdOn;
    private Boolean deleted;
    private Integer roleId;
    private String role;

    @ToStringExclude
    private User entity;

    private UserSettingsProjection userSettings;

    public UserDto(User user) {
        this.entity = user;
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.login = user.getLogin();
        this.active = user.isActive();
        this.lastLoggedInOn =
                user.getLastLoggedInOn() != null ? DateProvider.instantToTimestamp(user.getLastLoggedInOn()) : "";
        this.createdOn = user.getCreatedOn() != null ? DateProvider.instantToTimestamp(user.getCreatedOn()) : "";
        this.roleId = user.getRole().getId();
        this.role = user.getRole().getName();
        this.deleted = user.isDeleted();
        this.userSettings = user.getUserSettings() != null ? new UserSettingsProjection(user.getUserSettings()) : null;
    }

    public Boolean isActive() {
        return active;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    @Override
    public User getEntity() {
        return entity;
    }
}
