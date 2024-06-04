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

import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Newsfeed Event contains information about what happened in the system.
 * For example, whenever a user uploads a package,
 * it is registered and then displayed in the Newsfeed.
 * Events are created on per-request basis.
 * Therefore, if a strategy leads to certain side effect actions,
 * these actions are not registered with their separate events.
 */
@Getter
@Setter
@Entity
@Table(name = "newsfeed_event", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class NewsfeedEvent extends Resource {

    @Column(name = "time", nullable = false)
    private LocalDateTime time = LocalDateTime.now();

    @Column(name = "date", nullable = false)
    private LocalDate date = time.toLocalDate();

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "related_packagemaintainer_id")
    private PackageMaintainer packageMaintainer;

    @ManyToOne
    @JoinColumn(name = "related_repositorymaintainer_id")
    private RepositoryMaintainer repositoryMaintainer;

    @ManyToOne
    @JoinColumn(name = "related_user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "related_submission_id")
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "related_repository_id")
    private Repository repository;

    @ManyToOne
    @JoinColumn(name = "related_package_id")
    private Package packageBag;

    @ManyToOne
    @JoinColumn(name = "related_accesstoken_id")
    private AccessToken accessToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsfeed_event_type", nullable = false)
    private NewsfeedEventType type;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "relatedNewsfeedEvent")
    private Set<EventChangedVariable> eventChangedVariables = new HashSet<>();

    public NewsfeedEvent() {
        super(InternalTechnology.instance, ResourceType.EVENT);
    }

    public NewsfeedEvent(User author, NewsfeedEventType type) {
        this();
        this.author = author;
        this.type = type;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, PackageMaintainer packageMaintainer) {
        this(author, type);
        this.packageMaintainer = packageMaintainer;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, RepositoryMaintainer repositoryMaintainer) {
        this(author, type);
        this.repositoryMaintainer = repositoryMaintainer;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, Submission submission) {
        this(author, type);
        this.submission = submission;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, User user) {
        this(author, type);
        this.user = user;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, Repository repository) {
        this(author, type);
        this.repository = repository;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, Package packageBag) {
        this(author, type);
        this.packageBag = packageBag;
    }

    public NewsfeedEvent(User author, NewsfeedEventType type, AccessToken accessToken) {
        this(author, type);
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return null;
    }

    /**
     * Returns related resource, depending on what the event was used for.
     * The resource can be assigned only once, during construction.
     * This way erroneous assignment of multiple resources is prevented.
     * @return related resource
     */
    @Transient
    public EventableResource getRelatedResource() {
        if (packageMaintainer != null) return packageMaintainer;
        else if (repositoryMaintainer != null) return repositoryMaintainer;
        else if (user != null) return user;
        else if (submission != null) return submission;
        else if (packageBag != null) return packageBag;
        else if (repository != null) return repository;
        else if (accessToken != null) return accessToken;
        else return null;
    }
}
