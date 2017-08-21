/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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
package eu.openanalytics.rdepot.messaging;

public final class MessageCodes 
{
	public final static String ERROR_SUBMISSION_NOT_FOUND = "submission.notfound";
	public final static String ERROR_SUBMISSION_ALREADY_ACCEPTED = "submission.alreadyaccepted";
	
	public final static String ERROR_PACKAGE_EVENT_NOT_FOUND = "package.event.notfound";
	
	public final static String ERROR_PACKAGE_NOT_FOUND = "package.notfound";
	public final static String ERROR_PACKAGE_ALREADY_MAINTAINED = "package.alreadymaintained";
	public final static String ERROR_PACKAGE_EMPTY_NAME = "package.empty.name";
	public final static String ERROR_PACKAGE_INVALID_VERSION = "package.invalid.version";
	public final static String ERROR_PACKAGE_EMPTY_DESCRIPTION = "package.empty.description";
	public final static String ERROR_PACKAGE_EMPTY_AUTHOR = "package.empty.author";
	public final static String ERROR_PACKAGE_EMPTY_TITLE = "package.empty.title";
	public final static String ERROR_PACKAGE_EMPTY_LICENSE = "package.empty.license";
	public static final String ERROR_PACKAGE_EMPTY_MD5SUM = "package.empty.md5sum";
	
	public final static String ERROR_PACKAGEMAINTAINER_NOT_FOUND = "packagemaintainer.notfound";
	
	public final static String ERROR_PACKAGEMAINTAINER_EVENT_NOT_FOUND = "packagemaintainer.event.notfound";
	
	public final static String ERROR_REPOSITORY_NOT_FOUND = "repository.notfound";
	
	public final static String ERROR_REPOSITORYMAINTAINER_NOT_FOUND = "repositorymaintainer.notfound";
	public final static String ERROR_REPOSITORYMAINTAINER_DUPLICATE = "repositorymaintainer.error.duplicate";
	
	public final static String ERROR_USER_NOT_FOUND = "user.notfound";
	public final static String ERROR_USER_NOT_CAPABLE = "user.notcapable";
	public final static String ERROR_USER_NOT_AUTHORIZED = "user.notauthorized";
	
	public final static String ERROR_PASSWORD_DOES_NOT_MATCH = "password.doesnotmatch";
	public final static String ERROR_PASSWORD_INVALID = "password.invalid";
	
	public final static String ERROR_ROLE_NOT_FOUND = "role.notfound";
	
	public final static String ERROR_ADMIN_NOT_FOUND = "admin.notfound";
	
	public static final String ERROR_EVENT_NOT_FOUND = "event.notfound";
	
	public final static String ERROR_FORM_INVALID_CONTENTTYPE = "form.invalid.contenttype";
	public final static String ERROR_FORM_INVALID_FILENAME = "form.invalid.filename";
	public final static String ERROR_FORM_INVALID_EMAIL = "form.invalid.email";
	public final static String ERROR_FORM_EMPTY_FILE = "form.empty.file";
	public final static String ERROR_FORM_EMPTY_PACKAGE = "form.empty.package";
	public final static String ERROR_FORM_EMPTY_NAME = "form.empty.name";
	public final static String ERROR_FORM_EMPTY_LOGIN = "form.empty.login";
	public final static String ERROR_FORM_EMPTY_EMAIL = "form.empty.email";
	public final static String ERROR_FORM_EMPTY_PASSWORD = "form.empty.password";
	public final static String ERROR_FORM_EMPTY_SERVERADDRESS = "form.empty.serveraddress";
	public final static String ERROR_FORM_EMPTY_PUBLICATIONURI = "form.empty.publicationuri";
	public final static String ERROR_FORM_EMPTY_PUBLICACCESSURI = "form.empty.publicaccessuri";
	public final static String ERROR_FORM_DUPLICATE_NAME = "form.duplicate.name";
	public final static String ERROR_FORM_DUPLICATE_LOGIN = "form.duplicate.login";
	public final static String ERROR_FORM_DUPLICATE_EMAIL = "form.duplicate.email";
	public final static String ERROR_FORM_DUPLICATE_PUBLICATIONURI = "form.duplicate.publicationuri";

	
	public final static String WARNING_PACKAGE_DUPLICATE = "package.duplicate";
	public final static String WARNING_PACKAGE_ALREADY_ACTIVATED = "package.alreadyactivated";
	public final static String WARNING_PACKAGE_ALREADY_DEACTIVATED = "package.alreadydeactivated";
	
	public final static String WARNING_USER_ALREADY_DEACTIVATED = "user.alreadydeactivated";
	public final static String WARNING_USER_ALREADY_ACTIVATED = "user.alreadyactivated";
	
	
	public final static String SUCCESS_SUBMISSION_ACCEPTED = "submission.accepted";
	public final static String SUCCESS_SUBMISSION_CANCELED = "submission.canceled";
	
	public final static String SUCCESS_PACKAGE_ACTIVATED = "package.activated";
	public final static String SUCCESS_PACKAGE_DEACTIVATED = "package.deactivated";
	public final static String SUCCESS_PACKAGE_DELETED = "package.deleted";
	public final static String SUCCESS_PACKAGE_UPDATED = "package.updated";
	
	public final static String SUCCESS_USER_CREATED = "user.created";
	public final static String SUCCESS_USER_ACTIVATED = "user.activated";
	public final static String SUCCESS_USER_DEACTIVATED = "user.deactivated";
	public final static String SUCCESS_USER_DELETED = "user.deleted";
	
	public final static String SUCCESS_PASSWORD_CHANGED = "password.changed";
	
	public final static String SUCCESS_PACKAGEMAINTAINER_CREATED = "packagemaintainer.created";
	public final static String SUCCESS_PACKAGEMAINTAINER_UPDATED = "packagemaintainer.updated";
	public final static String SUCCESS_PACKAGEMAINTAINER_DELETED = "packagemaintainer.deleted";
	
	public final static String SUCCESS_REPOSITORY_CREATED = "repository.created";
	public final static String SUCCESS_REPOSITORY_UPDATED = "repository.updated";
	public final static String SUCCESS_REPOSITORY_DELETED = "repository.deleted";
	public static final String SUCCESS_REPOSITORY_PUBLISHED = "repository.published";
	public static final String SUCCESS_REPOSITORY_UNPUBLISHED = "repository.unpublished";
	
	public final static String SUCCESS_REPOSITORYMAINTAINER_CREATED = "repositorymaintainer.created";
	public final static String SUCCESS_REPOSITORYMAINTAINER_UPDATED = "repositorymaintainer.updated";
	public final static String SUCCESS_REPOSITORYMAINTAINER_DELETED = "repositorymaintainer.deleted";
	
}
