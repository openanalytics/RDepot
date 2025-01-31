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
package eu.openanalytics.rdepot.r.api.v2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.r.entities.RPackage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RPackageDto extends PackageDto {

    private String depends;
    private String imports;
    private String suggests;
    private String systemRequirements;
    private String license;
    private String md5sum;
    private String built;
    private String rVersion;
    private String architecture;
    private String distribution;
    private String enhances;
    private String linkingTo;
    private String priority;
    private String needsCompilation;

    public RPackageDto(RPackage packageBag) {
        super(packageBag);
        this.depends = packageBag.getDepends();
        this.imports = packageBag.getImports();
        this.suggests = packageBag.getSuggests();
        this.systemRequirements = packageBag.getSystemRequirements();
        this.license = packageBag.getLicense();
        this.md5sum = packageBag.getMd5sum();
        this.built = packageBag.getBuilt();
        this.rVersion = packageBag.getRVersion();
        this.architecture = packageBag.getArchitecture();
        this.distribution = packageBag.getDistribution();
        this.enhances = packageBag.getEnhances();
        this.linkingTo = packageBag.getLinkingTo();
        this.priority = packageBag.getPriority();
        this.needsCompilation = packageBag.isNeedsCompilation() ? "yes" : "no";
    }

    @Override
    @JsonIgnore
    public RPackage getEntity() {
        return (RPackage) entity;
    }
}
