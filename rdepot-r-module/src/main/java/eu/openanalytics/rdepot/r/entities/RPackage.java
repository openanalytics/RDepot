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
package eu.openanalytics.rdepot.r.entities;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("R")
@EqualsAndHashCode(callSuper = true)
@SecondaryTable(name = "rpackage", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class RPackage extends Package {

    @Serial
    private static final long serialVersionUID = -3373259770906796151L;

    /**
     *
     */
    @Column(name = "depends", table = "rpackage")
    private String depends = "";

    @Column(name = "imports", table = "rpackage")
    private String imports = "";

    @Column(name = "suggests", table = "rpackage")
    private String suggests = "";

    @Column(name = "system_requirements", table = "rpackage")
    private String systemRequirements = "";

    @Column(name = "license", nullable = false, table = "rpackage")
    private String license;

    @Column(name = "md5sum", nullable = false, table = "rpackage")
    private String md5sum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RRepository repository;

    @Column(name = "r_version", table = "rpackage")
    private String rVersion;

    @Column(name = "architecture", table = "rpackage")
    private String architecture;

    @Column(name = "distribution", table = "rpackage")
    private String distribution;

    @Column(name = "built", table = "rpackage")
    private String built;

    @Column(name = "enhances", table = "rpackage")
    private String enhances;

    @Column(name = "linking_to", table = "rpackage")
    private String linkingTo;

    @Column(name = "priority", table = "rpackage")
    private String priority;

    @Column(name = "needs_compilation", table = "rpackage")
    private boolean needsCompilation = false;

    @Transient
    private Boolean generateManuals;

    @Transient
    public Boolean getGenerateManuals() {
        return generateManuals;
    }

    @Transient
    public void setGenerateManuals(Boolean generateManuals) {
        this.generateManuals = generateManuals;
    }

    public void setRepository(RRepository repository) {
        this.repository = repository;
        super.setRepositoryGeneric(repository);
    }

    public RPackage(RPackage packageBag) {
        super(packageBag);
        this.depends = packageBag.depends;
        this.imports = packageBag.imports;
        this.suggests = packageBag.suggests;
        this.systemRequirements = packageBag.systemRequirements;
        this.license = packageBag.license;
        this.md5sum = packageBag.md5sum;
        this.repository = packageBag.repository;
        this.generateManuals = packageBag.generateManuals;
        this.rVersion = packageBag.rVersion;
        this.architecture = packageBag.architecture;
        this.distribution = packageBag.distribution;
        this.built = packageBag.built;
        this.enhances = packageBag.enhances;
        this.linkingTo = packageBag.linkingTo;
        this.priority = packageBag.priority;
        this.needsCompilation = packageBag.needsCompilation;
    }

    public RPackage() {
        super(RLanguage.instance);
    }

    public RPackage(RPackageDto dto, RRepository repository, Submission submission, User user) {
        super(RLanguage.instance, dto, submission, user);
        this.repository = repository;
        this.suggests = dto.getSuggests();
        this.systemRequirements = dto.getSystemRequirements();
        this.md5sum = dto.getMd5sum();
        this.license = dto.getLicense();
        this.depends = dto.getDepends();
        this.imports = dto.getImports();
        this.rVersion = dto.getRVersion();
        this.architecture = dto.getArchitecture();
        this.distribution = dto.getDistribution();
        this.built = dto.getBuilt();
        this.enhances = dto.getEnhances();
        this.linkingTo = dto.getLinkingTo();
        this.priority = dto.getPriority();
        this.needsCompilation = dto.getNeedsCompilation().equals("yes");
    }

    public RPackage(
            int id,
            RRepository repository,
            User user,
            String name,
            String description,
            String author,
            String license,
            String source,
            String title,
            String md5sum,
            boolean active,
            boolean deleted,
            boolean binary) {
        super(
                RLanguage.instance,
                id,
                repository,
                user,
                name,
                description,
                author,
                source,
                title,
                active,
                deleted,
                binary);
        this.license = license;
        this.md5sum = md5sum;
        this.repository = repository;
    }

    public RPackage(
            int id,
            RRepository repository,
            User user,
            String name,
            String description,
            String author,
            String license,
            String source,
            String title,
            String md5sum,
            boolean binary,
            String rVersion,
            String architecture,
            String distribution,
            String built,
            String enhances,
            String linkingTo,
            String priority,
            boolean needsCompilation,
            boolean active,
            boolean deleted) {
        super(
                RLanguage.instance,
                id,
                repository,
                user,
                name,
                description,
                author,
                source,
                title,
                active,
                deleted,
                binary);
        this.license = license;
        this.md5sum = md5sum;
        this.repository = repository;
        this.rVersion = rVersion;
        this.architecture = architecture;
        this.distribution = distribution;
        this.built = built;
        this.enhances = enhances;
        this.linkingTo = linkingTo;
        this.priority = priority;
        this.needsCompilation = needsCompilation;
    }
}
