/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.integrationtest.manager.v2;

public enum RequestType {
	GET("get"),
	GET_WITH_NEW_PATCH("getWithNewPatch"),
	GET_UNAUTHENTICATED("getUnauthenticated"),
	GET_UNAUTHORIZED("getUnauthorized"),
	PATCH("patch"),
	PATCH_UNAUTHENTICATED("patchUnautenticated"),
	PATCH_UNAUTHORIZED("patchUnauthorized"),
	POST("post"),
	POST_UNAUTHENTICATED("postUnauthenticated"),
	POST_UNAUTHORIZED("postUnauthorized"),
	POST_MULTIPART("postMultipart"),
	DELETE("delete"),
	DELETE_UNAUTHENTICATED("deleteUnauthenticated"),
	DELETE_UNAUTHORIZED("deleteUnauthorized");
	
	private final String requestType;
	
	RequestType(String requestType){
		this.requestType = requestType;
	}
	
	public String getValue() {
		return requestType;
	}
}
