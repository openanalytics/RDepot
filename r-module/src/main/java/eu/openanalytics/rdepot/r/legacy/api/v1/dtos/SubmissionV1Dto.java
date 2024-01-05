/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

import java.time.LocalDate;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.r.entities.RPackage;

public class SubmissionV1Dto {

	private int id;
	private String changes;
	private boolean accepted;
	private boolean deleted;
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate uploadDate;
	private PackageProjection packageBag;
	private UserProjection user;
	private SubmissionState state;
	
	public SubmissionV1Dto(Submission submission) {
		this.id = submission.getId();
		this.changes = submission.getChanges();
		if(submission.getState() == null) {
			this.accepted = false;
			this.deleted = false;
		} else {
			this.state = submission.getState();
			switch(submission.getState()) {
				case ACCEPTED:	
					this.accepted = true;
					this.deleted = false;
					break;
				case WAITING:
					this.accepted = false;
					this.deleted = false;
					break;
				case CANCELLED:
					this.accepted = false;
					this.deleted = true;
					break;	
				case REJECTED:
					this.accepted = false;
					this.deleted = true;
					break;
				default:
					this.accepted = false;
					this.deleted = false;
					break;
			}
		}
		Optional<NewsfeedEvent> tmpDate =submission.getEvents().stream()
				.filter(e -> e.getType() == NewsfeedEventType.UPLOAD)
				.findFirst();
		//TODO tmpDate to remove
		if (tmpDate.isPresent()) {
				this.uploadDate = tmpDate.get().getDate();
			}else {
				this.uploadDate = null;
			}
		
		this.packageBag = new PackageProjection((RPackage) submission.getPackage());
		this.user = new UserProjection(submission.getUser());
	}
	
	static SubmissionV1Dto of(Submission submission) {
		return new SubmissionV1Dto(submission);
	}
	
	public String getChanges() {
		return changes;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public int getId() {
		return id;
	}
	public LocalDate getUploadDate() {
		return uploadDate;
	}
	public PackageProjection getPackage() {
		return packageBag;
	}
	public UserProjection getUser() {
		return user;
	}
	public SubmissionState getState() {
		return state;
	}
}
