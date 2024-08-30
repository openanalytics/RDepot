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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.springframework.hateoas.EntityModel;

/**
 * DTO class that should be extended by technology-specific Submission DTO
 * and/or a generic one. It contains information common for every submission.
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractSubmissionDto<T extends PackageSimpleDto> implements IDto {

    protected Integer id;
    protected EntityModel<T> packageBag;

    @JsonIgnore
    protected T packageBagDto;

    protected UserProjection submitter;
    protected UserProjection approver;
    protected String changes;
    protected SubmissionState state;
    protected String created;

    @ToStringExclude
    protected Submission entity;

    protected String technology;

    protected AbstractSubmissionDto(Submission submission, T packageDto) {
        this.entity = submission;
        this.id = submission.getId();
        this.packageBagDto = packageDto;
        this.packageBag = EntityModel.of(packageDto);
        this.changes = submission.getChanges();
        this.state = submission.getState();
        this.technology = submission.getTechnology().getName();
        if (Objects.nonNull(submission.getSubmitter())) {
            this.submitter = new UserProjection(submission.getSubmitter());
        } else {
            this.submitter = null;
        }
        if (Objects.nonNull(submission.getApprover())) {
            this.approver = new UserProjection(submission.getApprover());
        } else {
            this.approver = null;
        }
        this.created = submission.getCreatedDate().toString();
    }

    @Override
    @JsonIgnore
    public Submission getEntity() {
        return entity;
    }
}
