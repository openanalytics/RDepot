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
package eu.openanalytics.rdepot.python.api.v2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PythonPackageDto extends PackageDto {

    private String authorEmail;
    private String classifiers;
    private String homePage;
    private String keywords;
    private String license;
    private String maintainer;
    private String maintainerEmail;
    private String platform;
    private String projectUrl;
    private String providesExtra;
    private String requiresDist;
    private String requiresExternal;
    private String requiresPython;
    private String summary;
    private String hash;

    public PythonPackageDto(PythonPackage packageBag) {
        super(packageBag);
        this.authorEmail = packageBag.getAuthorEmail();
        this.classifiers = packageBag.getClassifiers();
        this.homePage = packageBag.getHomePage();
        this.keywords = packageBag.getKeywords();
        this.license = packageBag.getLicense();
        this.maintainer = packageBag.getMaintainer();
        this.maintainerEmail = packageBag.getMaintainerEmail();
        this.platform = packageBag.getPlatform();
        this.projectUrl = packageBag.getProjectUrl();
        this.providesExtra = packageBag.getProvidesExtra();
        this.requiresDist = packageBag.getRequiresDist();
        this.requiresPython = packageBag.getRequiresPython();
        this.summary = packageBag.getSummary();
        this.summary = packageBag.getSummary();
        this.hash = packageBag.getHash();
    }

    @Override
    @JsonIgnore
    public PythonPackage getEntity() {
        return (PythonPackage) entity;
    }
}
