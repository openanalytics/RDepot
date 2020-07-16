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
package eu.openanalytics.rdepot.messaging;

public final class MessageCodes 
{
	public final static String ERROR_SUBMISSION_NOT_FOUND = "submission.notfound";
	public final static String WARNING_SUBMISSION_ALREADY_ACCEPTED = "submission.alreadyaccepted";
	public final static String ERROR_SUBMISSION_USER_CHANGE_DUPLICATE = "submission.user.change.duplicate";
	public final static String ERROR_SUBMISSION_PACKAGE_CHANGE_DUPLICATE = "submission.package.change.duplicate";

	
	public final static String ERROR_PACKAGE_EVENT_NOT_FOUND = "package.event.notfound";
	
	public final static String ERROR_PACKAGE_DEACTIVATE = "package.deactivate.error";
	public final static String ERROR_PACKAGE_ACTIVATE = "package.activate.error";

//	public final static String WARNING_PACKAGE_ALREADY_ACTIVE = "package.already.active.warning";
	public static final String ERROR_REPOSITORY_EDIT = "repository.edit.error";

	
	public final static String ERROR_PACKAGE_EDIT = "package.edit.error";
	public final static String ERROR_PACKAGE_CREATE = "package.create.error";
	public final static String ERROR_PACKAGE_DELETE = "package.delete.error";
	public final static String ERROR_PACKAGE_READ_VIGNETTE = "package.read.vignette.error";
	public final static String ERROR_PACKAGE_NOT_FOUND = "package.notfound";
	public final static String ERROR_PACKAGE_ALREADY_MAINTAINED = "package.alreadymaintained";
	public final static String ERROR_PACKAGE_EMPTY_NAME = "package.empty.name";
	public final static String ERROR_PACKAGE_INVALID_VERSION = "package.invalid.version";
	public final static String ERROR_PACKAGE_EMPTY_DESCRIPTION = "package.empty.description";
	public final static String ERROR_PACKAGE_EMPTY_AUTHOR = "package.empty.author";
	public final static String ERROR_PACKAGE_EMPTY_TITLE = "package.empty.title";
	public final static String ERROR_PACKAGE_EMPTY_LICENSE = "package.empty.license";
	public static final String ERROR_PACKAGE_EMPTY_MD5SUM = "package.empty.md5sum";
	public static final String ERROR_PACKAGE_EMPTY_SOURCE = "package.empty.source";
//	public static final String ERROR_PACKAGE_ACTIVE_CHANGE_DUPLICATE = "package.active.change.duplicate";
//	public static final String ERROR_PACKAGE_SOURCE_CHANGE_DUPLICATE = "package.source.change.duplicate";
	public static final String ERROR_PACKAGE_ALREADY_UPLOADED = "package.already.uploaded";
	public static final String ERROR_PACKAGE_ALREADY_ACCEPTED = "package.already.accepted";
	public static final String ERROR_PACKAGE_STORAGE_SOURCE_NOTFOUND = "package.storage.source.notfound";
	
	public final static String ERROR_PACKAGEMAINTAINER_NOT_FOUND = "packagemaintainer.notfound";
	
	public final static String ERROR_PACKAGEMAINTAINER_EVENT_NOT_FOUND = "packagemaintainer.event.notfound";
	
	public final static String ERROR_REPOSITORY_NOT_FOUND = "repository.notfound";
	public final static String ERROR_REPOSITORY_DUPLICATE = "repository.duplicate";
	public final static String ERROR_REPOSITORY_PUBLICATIONURI_CHANGE_DUPLICATE = "repository.publicationuri.change.duplicate";
	public final static String ERROR_REPOSITORY_VERSION_CHANGE_DUPLICATE = "repository.version.change.duplicate";
	public final static String ERROR_REPOSITORY_SERVERADDRESS_CHANGE_DUPLICATE = "repository.serveraddress.change.duplicate";
	public final static String ERROR_REPOSITORY_NAME_CHANGE_DUPLICATE = "repository.name.change.duplicate";
	public final static String ERROR_REPOSITORY_ALREADY_PUBLISHED = "repository.alreadypublished";
	public final static String ERROR_REPOSITORY_ALREADY_UNPUBLISHED = "repository.alreadyunpublished";
	
	public final static String ERROR_REPOSITORYMAINTAINER_NOT_FOUND = "repositorymaintainer.notfound";
	public final static String ERROR_REPOSITORYMAINTAINER_DUPLICATE = "repositorymaintainer.error.duplicate";
	
	public final static String ERROR_USER_NOT_FOUND = "user.notfound";
	public final static String ERROR_USER_NOT_CAPABLE = "user.notcapable";
	public final static String ERROR_USER_NOT_AUTHORIZED = "user.notauthorized";
	public final static String ERROR_USER_ALREADY_ACTIVE = "user.already.active";
	public final static String ERROR_USER_ALREADY_INACTIVE = "user.already.inactive";
	public final static String ERROR_USER_LASTLOGGEDINON_CHANGE_DUPLICATE = "user.lastloggedinon.change.duplicate";
	public static final String ERROR_USER_ROLE_CHANGE_DUPLICATE = "user.role.change.duplicate";
	
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

	public final static String ERROR_EMAIL_SEND_EXCEPTION = "email.send.exception";
	
	public final static String WARNING_SUBMISSION_EDIT = "submission.edit.warning";
	
	public final static String WARNING_PACKAGE_DUPLICATE = "package.duplicate";
	public final static String WARNING_PACKAGE_ALREADY_ACCEPTED = "package.alreadyaccepted";
	public final static String WARNING_PACKAGE_ALREADY_ACTIVATED = "package.alreadyactivated";
	public final static String WARNING_PACKAGE_ALREADY_DEACTIVATED = "package.alreadydeactivated";
	public final static String WARNING_PACKAGE_ALREADY_DELETED = "package.alreadydeleted";
	
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
	public static final String ERROR_REPOSITORY_CREATE = "repository.create.error";
	public static final String ERROR_REPOSITORY_DELETE = "repository.delete.error";
	public static final String WARNING_REPOSITORY_ALREADY_UNPUBLISHED = "warning.repository.already.unpublished";
	public static final String WARNING_REPOSITORY_VERSION_CHANGE_DUPLICATE = "warning.repository.version.change.duplicate";
	public static final String WARNING_REPOSITORY_PUBLICATIONURI_CHANGE_DUPLICATE = "warning.repository.publicationuri.change.duplicate";
	public static final String WARNING_REPOSITORY_SERVERADDRESS_CHANGE_DUPLICATE = "repository.serveraddress.change.duplicate";
	public static final String WARNING_REPOSITORY_NAME_CHANGE_DUPLICATE = "warning.repository.name.change.duplicate";
	public static final String ERROR_SUBMISSION_ACCEPT = "error.submission.accept";
	public static final String ERROR_SUBMISSION_CREATE = "error.submission.create";
	public static final String WARNING_SUBMISSION_CREATE = "warning.submission.create";
	public static final String ERROR_SUBMISSION_DELETE = "error.submission.delete";
	public static final String WARNING_SUBMISSION_DELETE = "warning.submission.delete";
	public static final String ERROR_SUBMISSION_EDIT = "error.submission.edit";
	public static final String ERROR_USER_CREATE = "error.user.create";
	public static final String ERROR_USER_DELETE = "error.user.delete";
	public static final String ERROR_USER_EDIT = "error.user.edit";
	public static final String ERROR_USER_ACTIVATE = "error.user.activate";
	public static final String ERROR_USER_DEACTIVATE = "error.user.deactivate";
	public static final String SUCCESS_SUBMISSION_CREATED = "success.submission.create";
	public static final String ERROR_REPOSITORY_PUBLISH = "error.repository.publish";
	public static final String WARNING_SUBMISSION_NEEDS_TO_BE_ACCEPTED = "warning.submission.needstobeaccepted";
	public static final String WARNING_SUBMISSION_ALREADY_DELETED = "warning.submission.already.deleted";
	public static final String ERROR_STORAGE_CREATE_FOLDER_STRUCTURE = "error.storage.create.folder.structure";
	public static final String ERROR_STORAGE_LINK_FOLDERS = "error.storage.link.folders";
	public static final String ERROR_STORAGE_COPY = "error.storage.file.copy";
	public static final String ERROR_STORAGE_GZIP = "error.storage.file.gzip";
	public static final String ERROR_STORAGE_MD5SUM_CALCULATE = "error.storage.md5sum.calculate";
	public static final String ERROR_STORAGE_EXTRACT_FILE = "error.storage.file.extract";
	public static final String ERROR_STORAGE_FILE_DELETE = "error.storage.file.delete";
	public static final String ERROR_STORAGE_GET_FILE_IN_BYTES = "error.storage.get.file.in.bytes";
	public static final String ERROR_STORAGE_MOVE_FILE = "error.storage.move.file";
	public static final String ERROR_STORAGE_WRITE_TO_DISK_FROM_MULTIPART = "error.write.to.disk.from.multipart";
	public static final String ERROR_MANUAL_CREATE = "error.manual.create";
	public static final String ERROR_PACKAGE_SOURCE_DELETE = "error.package.source.delete";
	public static final String ERROR_PACKAGE_STORAGE_GET_IN_BYTES = "error.package.storage.get.in.bytes";
	public static final String ERROR_READ_PACKAGE_DESCRIPTION = "error.read.package.descritpion";
	public static final String ERROR_PACKAGE_DESCRIPTION_NOT_FOUND = "error.package.description.not.found";
	public static final String ERROR_MD5_MISMATCH = "error.md5.mismatch";
	public static final String ERROR_POPULATE_PACKAGE_FOLDER = "error.populate.package.folder";
	public static final String ERROR_UPLOAD_TO_REMOTE_SERVER = "error.upload.to.remote.server";
	public static final String ERROR_MOVE_PACKAGE_SOURCE = "error.move.package.source";
	public static final String ERROR_PACKAGEMAINTAINER_CREATE = "error.package.maintainer.create";
	public static final String ERROR_PACKAGEMAINTAINER_DELETE = "error.package.maintainer.delete";
	public static final String ERROR_PACKAGEMAINTAINER_EDIT = "error.package.maintainer.edit";
}
