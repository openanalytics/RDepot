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
package eu.openanalytics.rdepot.base.storage.exceptions;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;

import java.io.Serial;

/**
 * Thrown when a file cannot be downloaded 
 * or saved in the destination directory.
 */
public class DownloadFileException extends Exception {

	@Serial
	private static final long serialVersionUID = -2410858161724074910L;

	public DownloadFileException(String url) {
		super(StaticMessageResolver.getMessage(MessageCodes.ERROR_DOWNLOAD_FILE)
				+ ": " + url);
	}
}
