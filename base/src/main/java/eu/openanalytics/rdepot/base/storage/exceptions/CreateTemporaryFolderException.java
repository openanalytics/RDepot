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
package eu.openanalytics.rdepot.base.storage.exceptions;

import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;

public class CreateTemporaryFolderException extends Exception{

	private static final long serialVersionUID = -6845318358203441406L;

	public CreateTemporaryFolderException(String prefix) {
		super(StaticMessageResolver.getMessage(RefactoredMessageCodes.COULD_NOT_CREATE_TEMPORARY_FOLDER)
				+ ": " + prefix);
	}

}