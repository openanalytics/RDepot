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
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageSimpleDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionSimpleDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.event.EventableResource;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.time.DateProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Getter
@Setter
@Entity
@Table(name = "submission", schema = "public")
public class Submission extends EventableResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageBag;

    @Column(name = "changes")
    private String changes;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private SubmissionState state;

    @Formula(
            "(select min(e.date) from newsfeed_event e where e.related_submission_id=id and (e.newsfeed_event_type='CREATE' or e.newsfeed_event_type='UPLOAD'))")
    private Instant createdDate;

    public Instant getCreatedDate() {
        return createdDate == null ? DateProvider.now() : createdDate;
    }

    public Submission() {
        super(InternalTechnology.instance, ResourceType.SUBMISSION);
    }

    public Submission(SubmissionDto submissionDto, Package packageBag, User submitter, Optional<User> approver) {
        this();
        this.id = submissionDto.getId();
        this.changes = submissionDto.getChanges();
        this.state = submissionDto.getState();
        this.submitter = submitter;
        approver.ifPresent(user -> this.approver = user);
        this.packageBag = packageBag;
    }

    public Submission(int id, User user, Package packageBag) {
        this();
        this.id = id;
        this.submitter = user;
        this.packageBag = packageBag;
    }

    public Submission(int id, User user, Package packageBag, String changes) {
        this();
        this.id = id;
        this.submitter = user;
        this.packageBag = packageBag;
        this.changes = changes;
    }

    public Submission(Submission that) {
        this();
        this.id = that.id;
        this.packageBag = that.packageBag;
        this.state = that.state;
        this.submitter = that.submitter;
        this.changes = that.changes;
        this.approver = that.approver;
    }

    public Package getPackage() {
        return this.packageBag;
    }

    public void setPackage(Package packagebag) {
        this.packageBag = packagebag;
    }

    @JoinColumn(name = "repository_id", nullable = false)
    public Repository getRepository() {
        return packageBag.getRepository();
    }

    @Override
    public String toString() {
        String approverLogin = "";
        if (Objects.nonNull(approver)) approverLogin = approver.getLogin();
        return "Submission (id: " + id + ", package: \""
                + this.packageBag.getName() + " "
                + this.packageBag.getVersion()
                + "\", submitter: \""
                + submitter.getLogin() + "\","
                + "\", approver: \""
                + approverLogin + "\""
                + ")";
    }

    @Override
    public Technology getTechnology() {
        return this.packageBag.getTechnology();
    }

    @Override
    public IDto createSimpleDto() {
        return new SubmissionSimpleDto(this, new PackageSimpleDto(packageBag));
    }
}
