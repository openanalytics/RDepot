/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.repo.exception;

import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;

public class RepositoryVersionMismatchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6950465626649080331L;

	public RepositoryVersionMismatchException(SynchronizeRepositoryRequestBody requestBody) {
		super("Repository has been altered since the request was sent. Try again.\n"
				+ "Expected version: " + requestBody.getVersionBefore()
				+ "\nPage: " + requestBody.getPage());
	}
}
