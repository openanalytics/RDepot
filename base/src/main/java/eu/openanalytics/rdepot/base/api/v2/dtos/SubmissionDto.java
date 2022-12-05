/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.api.v2.dtos;

import org.springframework.hateoas.EntityModel;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;

public class SubmissionDto implements IDto<Submission> {

	private Integer id;
	private Integer userId;
	private EntityModel<PackageDto<?, ?>> packageBag;
	private String changes;
	private SubmissionState state;
	private Submission entity;
	
	public SubmissionDto(Submission submission) {
		this.entity = submission;
		id = submission.getId();
		userId = submission.getUser().getId();
		packageBag = EntityModel.of(submission.getPackage().createDto());
		changes = submission.getChanges();
		state = submission.getState();
//		if(submission.isDeleted()) { //TODO: Change in 1.7.0 to support rejected state
//			state = SubmissionState.CANCELLED;
//		} else if(submission.isAccepted()) {
//			state = SubmissionState.ACCEPTED;
//		} else {
//			state = SubmissionState.WAITING;
//		}
	}
	
	public SubmissionDto() {
		
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public EntityModel<PackageDto<?, ?>> getPackageBag() {
		return packageBag;
	}

	public void setPackageBag(EntityModel<PackageDto<?, ?>> packageBag) {
		this.packageBag = packageBag;
	}

	public String getChanges() {
		return changes;
	}

	public void setChanges(String changes) {
		this.changes = changes;
	}

	public SubmissionState getState() {
		return state;
	}

	public void setState(SubmissionState state) {
		this.state = state;
	}

	@Override
	public Submission getEntity() {
		return entity;
	}
	
	@Override
	public void setEntity(Submission entity) {
		this.entity = entity;
	}
}
