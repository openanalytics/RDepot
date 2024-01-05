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

import eu.openanalytics.rdepot.base.entities.Submission;

public class SubmissionProjection {
	private boolean deleted;
	private String changes;
	private boolean accepted;
	private int id;
	
	public SubmissionProjection(Submission submission) {
		this.id = submission.getId();
		this.changes = submission.getChanges();
		if(submission.getState() == null) {
			this.accepted = false;
			this.deleted = false;
		} else {
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
	}

	public boolean isDeleted() {
		return deleted;
	}

	public String getChanges() {
		return changes;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public int getId() {
		return id;
	}

}
