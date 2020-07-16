/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.warning;

import java.util.Locale;

import org.springframework.context.MessageSource;

import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Submission;

public class SubmissionAlreadyAcceptedWarning extends SubmissionWarning {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5474480351121295775L;

	public SubmissionAlreadyAcceptedWarning(MessageSource messageSource, Locale locale, Submission submission) {
		super(MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED, messageSource, locale, submission);
	}

}
