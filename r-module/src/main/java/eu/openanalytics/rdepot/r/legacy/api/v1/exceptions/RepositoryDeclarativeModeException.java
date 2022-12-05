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
package eu.openanalytics.rdepot.r.legacy.api.v1.exceptions;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;

public class RepositoryDeclarativeModeException extends ApiV1Exception {

	private static final long serialVersionUID = -1425291398060329770L;

	public RepositoryDeclarativeModeException() {
		super(MessageCodes.ERROR_REPOSITORY_DECLARATIVE_MODE);
	}

}
