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
package eu.openanalytics.rdepot.api.v2.dto;

import org.springframework.hateoas.EntityModel;

import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;

public class RSubmissionDto extends EntityDto<Submission> {

	private Integer id;
	private Integer userId;
	private EntityModel<RPackageDto> packageBag;
	private String changes;
	private SubmissionState state;
	
	public RSubmissionDto(Submission submission) {
		super(submission);
		id = submission.getId();
		userId = submission.getUser().getId();
		packageBag = EntityModel.of(new RPackageDto(submission.getPackage()));
		changes = submission.getChanges();
		
		if(submission.isDeleted()) { //TODO: Change in 1.7.0 to support rejected state
			state = SubmissionState.CANCELLED;
		} else if(submission.isAccepted()) {
			state = SubmissionState.ACCEPTED;
		} else {
			state = SubmissionState.WAITING;
		}
	}
	
	public RSubmissionDto() {
		
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

	public EntityModel<RPackageDto> getPackageBag() {
		return packageBag;
	}

	public void setPackageBag(EntityModel<RPackageDto> packageBag) {
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
	public Submission toEntity() {
		Submission submission = new Submission();
		submission.setId(id);
		submission.setChanges(changes);
		
		submission.setPackage(packageBag.getContent().getEntity()); //TODO: Make sure packageBag is never null
		
		User user = new User();
		user.setId(userId);
		submission.setUser(user);
		
		switch(state) {
		case ACCEPTED:
			submission.setAccepted(true);
			submission.setDeleted(false);
			break;
		case CANCELLED:
			submission.setAccepted(false);
			submission.setDeleted(true);
			break;
		default:
			submission.setAccepted(false);
			submission.setDeleted(false);
			break;
		}
		
		return submission;
	}

}
