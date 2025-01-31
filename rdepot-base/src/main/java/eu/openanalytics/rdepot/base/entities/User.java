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
package eu.openanalytics.rdepot.base.entities;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserProjection;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(
        name = "user",
        schema = "public",
        uniqueConstraints = {@UniqueConstraint(columnNames = "login"), @UniqueConstraint(columnNames = "email")})
public class User extends EventableResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "last_logged_in_on")
    private Instant lastLoggedInOn;

    @Column(name = "created_on")
    private Instant createdOn;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<AccessToken> accessTokens = new HashSet<>(0);

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private UserSettings userSettings;

    public User() {
        super(InternalTechnology.instance, ResourceType.USER);
    }

    public User(UserDto userDto, Role role, Instant lastLoggedInOn, Instant createdOn) {
        this();
        this.id = userDto.getId();
        this.name = userDto.getName();
        this.email = userDto.getEmail();
        this.login = userDto.getLogin();
        this.active = userDto.isActive();
        this.deleted = userDto.isDeleted();
        this.lastLoggedInOn = lastLoggedInOn;
        this.role = role;
        this.createdOn = createdOn;
    }

    public User(int id, Role role, String name, String email, String login, boolean active, boolean deleted) {
        this();
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.login = login;
        this.active = active;
        this.deleted = deleted;
    }

    public User(
            int id,
            Role role,
            String name,
            String email,
            String login,
            boolean active,
            boolean deleted,
            Instant lastLoggedInOn,
            Instant createdOn) {
        this();
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.login = login;
        this.active = active;
        this.lastLoggedInOn = lastLoggedInOn;
        this.deleted = deleted;
        this.createdOn = createdOn;
    }

    public User(
            int id,
            Role role,
            String name,
            String email,
            String login,
            boolean active,
            boolean deleted,
            Set<AccessToken> accessTokens) {
        this();
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.login = login;
        this.active = active;
        this.deleted = deleted;
        this.accessTokens = accessTokens;
    }

    public User(User that) {
        this();
        this.id = that.id;
        this.role = that.role;
        this.name = that.name;
        this.email = that.email;
        this.login = that.login;
        this.active = that.active;
        this.deleted = that.deleted;
        this.userSettings = that.userSettings;
    }

    public UserProjection createDtoShort() {
        return new UserProjection(this);
    }

    @Override
    public String toString() {
        return "User (id: " + id + ", login: \"" + login + "\", email: \"" + email + "\", role: \"" + role.getName()
                + "\")";
    }

    @Override
    public IDto createSimpleDto() {
        return new UserDto(this);
    }
}
