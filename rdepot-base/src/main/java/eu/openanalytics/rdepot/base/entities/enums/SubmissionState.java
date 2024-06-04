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
package eu.openanalytics.rdepot.base.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Determines state of a given
 * {@link eu.openanalytics.rdepot.base.entities.Submission Submission}.
 * It was introduced with API v2 and is reflected by deleted and accepted
 * properties of the related {@link eu.openanalytics.rdepot.base.entities.Package Package}.
 */
@Getter
@AllArgsConstructor
public enum SubmissionState {
    /**
     * Submission was either uploaded by a person with the right to accept it
     * or was accepted afterward.
     * Its corresponding package must have the accepted flag set to true.
     * The deleted flag does not matter here.
     * In this state, package files are already put in the repository storage directories
     * (moved from the "waiting room").
     */
    ACCEPTED("accepted"), // deleted = false; accepted = true; OR deleted = true; accepted = true;

    /**
     * Submission was uploaded by a person without the right to accept it.
     * In this state, package files will be waiting in so-called "waiting room"
     * until they are accepted (or deleted) by admin or maintainer.
     * Package's accepted flag must be set to false as well as the deleted flag.
     */
    WAITING("waiting"), // deleted = false; accepted = false;

    /**
     * Submission was uploaded by a person without the right to accept it
     * and was cancelled by the same person or rejected by admin or maintainer.
     * In this state, package files are put in the trash directory.
     * Package's deleted flag is set to true, accepted flag is set to false.
     */
    CANCELLED("cancelled"), // deleted = true; accepted = false;

    /**
     * Submission was uploaded by a person without the right to accept it
     * and was rejected by admin or maintainer (*not* cancelled by the author!).
     * Currently, this state is not implemented, CANCELLED serves its purpose.
     */
    REJECTED("rejected"); // deleted = true for future release

    private final String value;
}
