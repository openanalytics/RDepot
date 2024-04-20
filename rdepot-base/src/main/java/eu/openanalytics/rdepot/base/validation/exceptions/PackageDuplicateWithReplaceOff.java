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
package eu.openanalytics.rdepot.base.validation.exceptions;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.exception.LocalizedException;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import lombok.Getter;

import java.io.Serial;

/**
 * Thrown when there already is such a {@link Package} in a given {@link Repository}.
 * In principle should not generate an error, rather a warning.
 */
@Getter
public class PackageDuplicateWithReplaceOff extends LocalizedException {

	@Serial
	private static final long serialVersionUID = -2811800471533644657L;
	private final Submission submission;
	
	public PackageDuplicateWithReplaceOff(Submission submission) {
		super(
				StaticMessageResolver.getMessage(
						MessageCodes.DUPLICATE_VERSION_REPLACE_OFF)
			);
		this.submission = submission;
	}
}
