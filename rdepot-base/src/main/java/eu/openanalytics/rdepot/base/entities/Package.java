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
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageSimpleDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import eu.openanalytics.rdepot.base.technology.Technology;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a package.
 * It should be extending depending on the requirements of the implemented technology.
 */
@Getter
@Setter
@Entity
@Table(name = "package", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@EqualsAndHashCode(
        callSuper = true,
        exclude = {"submission"})
@DiscriminatorColumn(
        name = "resource_technology",
        discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "varchar default 'Package'")
public abstract class Package extends EventableResource implements Comparable<Package>, Serializable {

    protected Package() {
        super(InternalTechnology.instance, ResourceType.PACKAGE);
    }

    @Serial
    private static final long serialVersionUID = 2298415552029766827L;

    @Column(name = "resource_technology", insertable = false, updatable = false)
    protected String resourceTechnology;

    @Column(name = "version", nullable = false)
    private String version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false, insertable = false, updatable = false)
    private Repository repositoryGeneric;

    @ManyToOne
    @JoinColumn(name = "user_maintainer_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "packageBag")
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url = "";

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "binary_package", nullable = false, table = "package")
    private boolean binary;

    protected Package(Package packageBag) {
        super(packageBag.id, packageBag.getTechnology(), ResourceType.PACKAGE);
        this.user = packageBag.user;
        this.name = packageBag.name;
        this.description = packageBag.description;
        this.author = packageBag.author;
        this.url = packageBag.url;
        this.source = packageBag.source;
        this.title = packageBag.title;
        this.active = packageBag.active;
        this.deleted = packageBag.deleted;
        this.submission = packageBag.submission;
        this.version = packageBag.version;
        this.resourceTechnology = packageBag.resourceTechnology;
        this.repositoryGeneric = packageBag.repositoryGeneric;
        this.binary = packageBag.binary;
    }

    protected Package(Technology technology) {
        super(technology, ResourceType.PACKAGE);
    }

    protected Package(Technology technology, PackageDto packageDto, Submission submission, User user) {
        this(technology);
        this.submission = submission;
        this.user = user;
        this.id = packageDto.getId();
        this.name = packageDto.getName();
        this.description = packageDto.getDescription();
        this.author = packageDto.getAuthor();
        this.url = packageDto.getUrl();
        this.source = packageDto.getSource();
        this.title = packageDto.getTitle();
        this.active = packageDto.getActive();
        this.deleted = packageDto.getDeleted();
        this.version = packageDto.getVersion();
        this.binary = packageDto.isBinary();
    }

    protected Package(
            Technology technology,
            int id,
            Repository repository,
            User user,
            String name,
            String description,
            String author,
            String source,
            String title,
            boolean active,
            boolean deleted,
            boolean binary) {
        super(id, technology, ResourceType.PACKAGE);
        this.user = user;
        this.name = name;
        this.description = description;
        this.author = author;
        this.title = title;
        this.source = source;
        this.active = active;
        this.deleted = deleted;
        this.repositoryGeneric = repository;
    }

    protected Package(
            Technology technology,
            int id,
            Repository repository,
            User user,
            String name,
            String description,
            String author,
            String url,
            String source,
            String title,
            boolean active,
            boolean deleted,
            boolean binary,
            Submission submission) {
        super(id, technology, ResourceType.PACKAGE);
        this.user = user;
        this.name = name;
        this.description = description;
        this.author = author;
        this.url = url;
        this.source = source;
        this.title = title;
        this.active = active;
        this.submission = submission;
        this.deleted = deleted;
        this.repositoryGeneric = repository;
    }

    @Transient
    public String getFileName() {
        int pathLength = this.source.split("/").length;
        return this.source.split("/")[pathLength - 1];
    }

    @Override
    public String toString() {
        return "Package [id: "
                + id + ", technology: \""
                + getTechnology().getName() + " "
                + getTechnology().getVersion() + "\", name: \""
                + name + "\", version: \""
                + version + "\"]";
    }

    public abstract Repository getRepository();

    protected void setRepositoryGeneric(Repository repositoryGeneric) {
        this.repositoryGeneric = repositoryGeneric;
    }

    @Override
    public int compareTo(Package that) {
        if (!this.getTechnology().equals(that.getTechnology())) {
            throw new IllegalArgumentException(
                    "Trying to compare packages of different technologies " + "is like comparing apples with oranges.");
        }
        if (!this.name.equals(that.getName())) {
            throw new IllegalArgumentException("Trying to compare package " + that.getName() + " with package "
                    + this.name + " is like comparing apples with oranges.");
        }

        String[] theseSplitDots = this.version.split("[-.]");
        String[] thoseSplitDots = that.getVersion().split("[-.]");
        int length;
        if (theseSplitDots.length - thoseSplitDots.length > 0) {
            length = thoseSplitDots.length;
        } else {
            length = theseSplitDots.length;
        }

        int thisNumber, thatNumber;
        for (int i = 0; i < length; i++) {
            thisNumber = Integer.parseInt(theseSplitDots[i]);
            thatNumber = Integer.parseInt(thoseSplitDots[i]);
            if (thisNumber > thatNumber) {
                return 1;
            }
            if (thatNumber > thisNumber) {
                return -1;
            }
        }

        if (theseSplitDots.length == thoseSplitDots.length) {
            return 0;
        }

        if (theseSplitDots.length - thoseSplitDots.length > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public IDto createSimpleDto() {
        return new PackageSimpleDto(this);
    }
}
