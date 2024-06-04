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
package eu.openanalytics.rdepot.base.email;

import eu.openanalytics.rdepot.base.entities.Submission;

/**
 * Keeps users informed about current events via e-mail.
 * Can be disabled via a proper configuration flag in the application.yaml file.
 */
public interface EmailService {

    /**
     * Sends an e-mail informing that the submission can be now accepted or rejected.
     */
    void sendAcceptSubmissionEmail(Submission submission);

    /**
     * Sends an e-mail informing that the submission has been cancelled by the submitter.
     */
    void sendCancelledSubmissionEmail(Submission submission);
}
