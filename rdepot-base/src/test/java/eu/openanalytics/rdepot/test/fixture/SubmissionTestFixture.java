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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import java.util.ArrayList;
import java.util.List;

public class SubmissionTestFixture {

    public static final String CHANGES = "Some changes 123";

    public static List<Submission> GET_FIXTURE_SUBMISSIONS(User user, Package packageBag, int count) {
        List<Submission> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Submission submission = new Submission(i, user, packageBag);
            submission.setChanges(CHANGES);
            submission.setState(SubmissionState.ACCEPTED);
            result.add(submission);
        }

        return result;
    }

    public static Submission GET_FIXTURE_SUBMISSION(User user, Package packageBag) {
        return GET_FIXTURE_SUBMISSIONS(user, packageBag, 1).get(0);
    }

    public static SubmissionDto GET_FIXTURE_SUBMISSION_DTO(Submission submission, Package packageBag) {
        PackageDto packageDto = new PackageDto(packageBag);
        return new SubmissionDto(submission, packageDto);
    }

    public static List<SubmissionDto> GET_FIXTURE_SUBMISSION_DTOS(List<Submission> submissions) {
        List<SubmissionDto> submissionDtos = new ArrayList<SubmissionDto>();
        submissions.forEach(submission -> {
            PackageDto packageDto = new PackageDto(submission.getPackageBag());
            submissionDtos.add(new SubmissionDto(submission, packageDto));
        });
        return submissionDtos;
    }
}
