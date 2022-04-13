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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.List;

import eu.openanalytics.rdepot.model.SubmissionEvent;

public class SubmissionEventTestFixture {
	
	public static List<SubmissionEvent> GET_SUBMISSION_EVENT_TEST_FIXTURE(int count) {
		List<SubmissionEvent> result = new ArrayList<>();
		
		for(int i = 0; i < count; i++) {
			SubmissionEvent submissionEvent = new SubmissionEvent();
			submissionEvent.setId(i);
			result.add(submissionEvent);
		}
		
		return result;
	}
}

