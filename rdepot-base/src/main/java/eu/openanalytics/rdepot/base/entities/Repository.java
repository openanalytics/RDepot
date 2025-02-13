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
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositorySimpleDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.time.DateProvider;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a repository.
 * It should be extending depending on the requirements of the implemented technology.
 */
@Getter
@Setter
@Entity
@Table(name = "repository", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "resource_technology",
        discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "varchar default 'Repository'")
public abstract class Repository extends EventableResource implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Repository() {
        super(InternalTechnology.instance, ResourceType.REPOSITORY);
    }

    @Column(name = "resource_technology", insertable = false, updatable = false)
    protected String resourceTechnology;

    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "publication_uri", unique = true, nullable = false)
    private String publicationUri;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "server_address", nullable = false)
    private String serverAddress;

    @Column(name = "published", nullable = false)
    private Boolean published = false;

    @Column(name = "last_publication_timestamp")
    private Instant lastPublicationTimestamp = null;

    @Column(name = "last_modified_timestamp")
    private Instant lastModifiedTimestamp = DateProvider.now();

    @Column(name = "last_publication_successful")
    private boolean lastPublicationSuccessful = false;

    @Column(name = "requires_authentication", nullable = false)
    private Boolean requiresAuthentication;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
    private Set<RepositoryMaintainer> repositoryMaintainers = new HashSet<>(0);

    @Transient
    private Boolean synchronizing = false;

    @Transient
    public Boolean isSynchronizing() {
        return synchronizing;
    }

    @Transient
    public void setSynchronizing(Boolean synchronizing) {
        this.synchronizing = synchronizing;
    }

    protected Repository(Repository that) {
        super(that.id, that.getTechnology(), ResourceType.REPOSITORY);
        this.version = that.version;
        this.publicationUri = that.publicationUri;
        this.name = that.name;
        this.serverAddress = that.serverAddress;
        this.deleted = that.deleted;
        this.published = that.published;
        this.repositoryMaintainers = that.repositoryMaintainers;
        this.resourceTechnology = that.resourceTechnology;
        this.synchronizing = that.synchronizing;
        this.lastPublicationSuccessful = that.lastPublicationSuccessful;
        this.lastModifiedTimestamp = that.lastModifiedTimestamp;
        this.lastPublicationTimestamp = that.lastPublicationTimestamp;
        this.requiresAuthentication = that.getRequiresAuthentication();
    }

    protected Repository(Technology technology) {
        super(technology, ResourceType.REPOSITORY);
    }

    protected <D extends RepositoryDto> Repository(
            Technology technology, D repositoryDto, Instant lastPublicationTimestamp, Instant lastModifiedTimestamp) {
        this(technology);
        this.id = repositoryDto.getId();
        this.version = repositoryDto.getVersion();
        this.publicationUri = repositoryDto.getPublicationUri();
        this.serverAddress = repositoryDto.getServerAddress();
        this.name = repositoryDto.getName();
        this.deleted = repositoryDto.isDeleted();
        this.published = repositoryDto.isPublished();
        this.requiresAuthentication = repositoryDto.getRequiresAuthentication();
        this.synchronizing = repositoryDto.isSynchronizing();
        this.lastPublicationTimestamp = lastPublicationTimestamp;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", name: " + this.name + ", publication URI: " + this.publicationUri + ", server address: "
                + this.serverAddress;
    }

    @Override
    public IDto createSimpleDto() {
        return new RepositorySimpleDto(this);
    }
}
