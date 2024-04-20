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
package eu.openanalytics.rdepot.base.strategy.exceptions;

import eu.openanalytics.rdepot.base.exception.LocalizedException;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.Service;

import java.io.Serial;

/**
 * Thrown when one attempts to use a {@link Service} 
 * that mismatches the technology-specific implementation.
 * For example, it will happen when one tries to fetch Package object
 * of R Submission using Python Package Service.
 */
public class WrongServiceException extends LocalizedException {

	@Serial
	private static final long serialVersionUID = -4374826886373972582L;

	public WrongServiceException() {
		super(MessageCodes.WRONG_SERVICE_EXCEPTION);
	}

}
