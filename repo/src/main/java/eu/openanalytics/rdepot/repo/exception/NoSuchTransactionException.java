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

public class NoSuchTransactionException extends Exception {

	private static final long serialVersionUID = -750486766509666536L;

	public NoSuchTransactionException(String repository, String id) {
		super("Chunk could not be accepted because the transaction "
				+ "which it is a part of have not been initialized properly.\n"
				+ "Transaction id: " + id + "\nRepository name: " + repository);
	}
}
