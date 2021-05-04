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

public final class MessageCodes {
	public final static String ERROR_SUBMISSION_NOT_FOUND = "submission.notfound";
	public final static String WARNING_SUBMISSION_ALREADY_ACCEPTED = "submission.alreadyaccepted";
	
	public final static String ERROR_PACKAGE_EVENT_NOT_FOUND = "package.event.notfound";
	
	public final static String ERROR_PACKAGE_DEACTIVATE = "package.deactivate.error";
	public final static String ERROR_PACKAGE_ACTIVATE = "package.activate.error";

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
	public static final String ERROR_PACKAGE_STORAGE_SOURCE_NOTFOUND = "package.storage.source.notfound";
	
	public final static String ERROR_PACKAGEMAINTAINER_NOT_FOUND = "packagemaintainer.notfound";
	
	public final static String ERROR_PACKAGEMAINTAINER_EVENT_NOT_FOUND = "packagemaintainer.event.notfound";
	
	public final static String ERROR_REPOSITORY_NOT_FOUND = "repository.notfound";
	
	public final static String ERROR_REPOSITORYMAINTAINER_NOT_FOUND = "error.repositorymaintainer.notfound";
	public final static String ERROR_REPOSITORYMAINTAINER_DUPLICATE = "error.repositorymaintainer.duplicate";
	
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
	public final static String ERROR_FORM_DUPLICATE_SERVERADDRESS = "form.duplicate.serveraddress";

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
	public static final String ERROR_STORAGE_WRITE_TO_DISK = "error.write.to.disk";
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
	public static final String ERROR_NO_ADMIN_LEFT = "error.no.admin.left";

	public static final String ERROR_AUTHENTICATION_INACTIVE_USER = "error.authentication.inactive.user";
	public static final String ERROR_AUTHENTICATION_DELETED_USER = "error.authentication.deleted.user";
	public static final String ERROR_AUTHENTICATION_USER_CREATION = "error.authentication.user.creation";
	public static final String ERROR_AUTHENTICATION_USER_EDITION = "error.authentication.user.edition";

	public static final String ERROR_DELETE_FROM_REMOTE_SERVER = "error.delete.from.remote.server";
	public static final String ERROR_SYNCHRONIZE_REPOSITORY_WITH_REMOTE_SERVER = "error.synchronize.repository.with.remote.server";
	public static final String ERROR_INVALID_SERVER_ADDRESS = "error.invalid.server.address";
	public static final String ERROR_STORE_ON_REMOTE_SERVER = "error.store.on.remote.server";
	public static final String ERROR_PACKAGE_GET_REFERENCE_MANUAL = "package.get.reference.manual";
	public static final String SUCCESS_SUBMISSION_DELETED = "submission.deleted";
	public static final String ERROR_REPOSITORY_CREATE_DISABLED = "repository.create.disabled";
	public static final String ERROR_REPOSITORY_EDIT_DISABLED = "repository.edit.disabled";
	public static final String ERROR_REPOSITORY_DECLARATIVE_MODE = "repository.declarative.mode";
	public static final String ERROR_REPOSITORY_INVALID_ID = "repository.invalid.id";
	public static final String SUCCESS_USER_UPDATED = "user.updated";
	public static final String ERROR_REPOSITORYMAINTAINER_EDIT = "error.repositorymaintainer.edit";
	public static final String ERROR_REPOSITORYMAINTAINER_DELETE = "error.repositorymaintainer.delete";	
	public static final String ERROR_NO_SUCH_PACKAGE = "error.no.such.package";
	public static final String ERROR_UPDATE_PACKAGE = "error.update.package";
	public static final String ERROR_DOWNLOAD_PACKAGES_FILE = "error.download.packages.file";
	public static final String ERROR_CLEAN_FS = "error.clean.fs";
	public static final String ERROR_SYNCHRONIZE_MIRROR = "error.synchronize.mirror";
	public static final String SUCCESS_REPOSITORY_SYNCHRONIZATION_STARTED = "success.repository.synchronization.started";
	public static final String ERROR_DOWNLOAD_FILE = "error.download.file";
	public static final String ERROR_STORAGE_CREATE_TEMPORARY_FOLDER = "error.storage.create.temporary.folder";
	public static final String WARNING_SYNCHRONIZATION_IN_PROGRESS = "warning.synchronization.in.progress";
	public static final String ERROR_ACCESS_DENIED = "error.access.denied"; 
}
