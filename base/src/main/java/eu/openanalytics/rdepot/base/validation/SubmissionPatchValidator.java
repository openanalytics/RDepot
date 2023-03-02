/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.validation;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.validation.exceptions.PatchValidationException;
import jakarta.json.JsonPatch;

@Component
public class SubmissionPatchValidator implements PatchValidator<Submission, SubmissionDto> {

	@Override
	public void validatePatch(JsonPatch patch, Submission submission, 
			SubmissionDto submissionDto) 
			throws PatchValidationException {
		if(!submission.getState().equals(submissionDto.getState()) 
				&& !submission.getState().equals(SubmissionState.WAITING)
			||
			submission.getUser().getId() != submissionDto.getUserId()) {
			throw new PatchValidationException(RefactoredMessageCodes.COULD_NOT_CHANGE_SUBMISSION);
		}
		for(int i = 0; i < patch.toJsonArray().size(); i++) {
			String path = patch.toJsonArray().get(i).asJsonObject().get("path").toString();
			if(path != null && !path.equals("\"/state\"")) {
				throw new PatchValidationException(RefactoredMessageCodes.COULD_NOT_CHANGE_SUBMISSION);
			}
		}
	}

}
