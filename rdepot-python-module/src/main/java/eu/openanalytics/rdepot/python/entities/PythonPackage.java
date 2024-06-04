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
package eu.openanalytics.rdepot.python.entities;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonPackageDto;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("Python")
@SecondaryTable(name = "pythonpackage", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class PythonPackage extends Package {

    private static final long serialVersionUID = -3373259770906796151L;

    @Column(name = "author_email", table = "pythonpackage")
    private String authorEmail = "";

    @Column(name = "classifier", table = "pythonpackage")
    private String classifiers = "";

    @Column(name = "description_content_type", table = "pythonpackage")
    private String descriptionContentType = "";

    @Column(name = "home_page", table = "pythonpackage")
    private String homePage = "";

    @Column(name = "keywords", table = "pythonpackage")
    private String keywords = "";

    @Column(name = "license", table = "pythonpackage")
    private String license = "";

    @Column(name = "maintainer", table = "pythonpackage")
    private String maintainer = "";

    @Column(name = "maintainer_email", table = "pythonpackage")
    private String maintainerEmail = "";

    @Column(name = "platform", table = "pythonpackage")
    private String platform = "";

    @Column(name = "project_url", table = "pythonpackage")
    private String projectUrl = "";

    @Column(name = "provides_extra", table = "pythonpackage")
    private String providesExtra = "";

    @Column(name = "requires_dist", table = "pythonpackage")
    private String requiresDist = "";

    @Column(name = "requires_external", table = "pythonpackage")
    private String requiresExternal = "";

    @Column(name = "requires_python", table = "pythonpackage")
    private String requiresPython = "";

    @Column(name = "summary", table = "pythonpackage")
    private String summary = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private PythonRepository repository;

    @Column(name = "hash", nullable = false, table = "pythonpackage")
    private String hash;

    public PythonPackage(PythonPackage packageBag) {
        super(packageBag);
        this.repository = packageBag.repository;
    }

    public PythonPackage() {
        super(PythonLanguage.instance);
    }

    public void setRepository(PythonRepository repository) {
        this.repository = repository;
        setRepositoryGeneric(repository);
    }

    public PythonPackage(
            int id,
            PythonRepository repository,
            User user,
            String name,
            String description,
            String author,
            String license,
            String source,
            String title,
            String hash,
            boolean active,
            boolean deleted) {
        super(PythonLanguage.instance, id, repository, user, name, description, author, source, title, active, deleted);
        this.license = license;
        this.hash = hash;
        this.repository = repository;
    }

    public PythonPackage(PythonPackageDto dto, PythonRepository repository, Submission submission, User user) {
        super(PythonLanguage.instance, dto, submission, user);
        this.repository = repository;
        this.authorEmail = dto.getAuthorEmail();
        this.classifiers = dto.getClassifiers();
        this.descriptionContentType = dto.getDescriptionContentType();
        this.homePage = dto.getHomePage();
        this.keywords = dto.getKeywords();
        this.license = dto.getLicense();
        this.maintainer = dto.getMaintainer();
        this.maintainerEmail = dto.getMaintainerEmail();
        this.platform = dto.getPlatform();
        this.projectUrl = dto.getProjectUrl();
        this.providesExtra = dto.getProvidesExtra();
        this.requiresDist = dto.getRequiresDist();
        this.requiresPython = dto.getRequiresPython();
        this.summary = dto.getSummary();
        this.summary = dto.getSummary();
        this.hash = dto.getHash();
    }

    @Override
    public String getTitle() {
        return this.summary;
    }

    @Override
    public String getAuthor() {
        return super.getAuthor() == null || super.getAuthor().isBlank() ? this.authorEmail : super.getAuthor();
    }

    @Override
    public String getUrl() {
        return this.projectUrl == null || this.projectUrl.isBlank() ? this.homePage : this.projectUrl;
    }
}
