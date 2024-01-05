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
package eu.openanalytics.rdepot.base.messaging;

public class RefactoredMessageCodes {
	
	public static final String REPOSITORY_NOT_FOUND = "repository.not.found";
	public static final String PACKAGE_VALIDATION_ERROR = "package.validation.error";
	public static final String NO_SUITABLE_MAINTAINER_FOUND = "no.suitable.maintainer.found";
	public static final String STRATEGY_FAILURE = "strategy.failure";
	public static final String MULTIPART_FILE_VALIDATION_EXCEPTION = "multipart.file.validation.exception";
	public static final String INVALID_SOURCE = "invalid.source";
	public static final String COULD_NOT_WRITE_TO_WAITING_ROOM = "could.not.write.to.waiting.room";
	public static final String COULD_NOT_DELETE_FILE = "could.not.delete.file";
	public static final String COULD_NOT_EXTRACT_FILE = "could.not.extract.file";
	public static final String READ_PACKAGE_DESCRIPTION_EXCEPTION = "read.package.description.exception";
	public static final String SOURCE_FILE_DELETE_EXCEPTION = "source.file.delete.exception";
	public static final String COULD_NOT_MOVE_FILE = "could.not.move.file";
	public static final String COULD_NOT_MOVE_PACKAGE_SOURCE = "could.not.move.package.source";
	public static final String REVERSE_STRATEGY_FAILURE = "reverse.strategy.failure";
	public static final String WRONG_SERVICE_EXCEPTION = "wrong.service";
	public static final String ADMIN_NOT_FOUND = "admin.not.found";
	public static final String NO_ADMIN_LEFT = "no.admin.left";
	public static final String COULD_NOT_DELETE_REPOSITORY_DIRECTORY = "could.not.delete.repository.directory";
	public static final String COULD_NOT_DELETE_RESOURCE = "could.not.delete.resource";
	public static final String COULD_NOT_CREATE_FOLDER_STRUCTURE = "could.not.create.folder.structure";
	public static final String COULD_NOT_GZIP_FILE = "could.not.gzip.file";
	public static final String COULD_NOT_SYNCHRONIZE_REPOSITORY = "could.not.synchronize.repository";
	public static final String MD5_MISMATCH_EXCEPTION = "md5.mismatch";
	public static final String COULD_NOT_POPULATE_PACKAGE_FOLDER = "could.not.populate.package.folder";
	public static final String MD5_CALCULATION_EXCEPTION = "md5.calculation.error";
	public static final String COULD_NOT_LINK_FOLDERS = "could.not.link.folders";
	public static final String PACKAGE_SOURCE_NOT_FOUND = "package.source.not.found";
	public static final String COULD_NOT_CLEAN_UP_AFTER_SYNCHRONIZATION = "could.not.clean.up.after.synchronization";
	public static final String COULD_NOT_RESOLVE_EVENT_TYPE = "could.not.resolve.event.type";
	public static final String COULD_NOT_RESOLVE_RESOURCE_TYPE = "could.not.resolve.resource.type";
	public static final String REPOSITORY_PUBLICATION_EXCEPTION = "could.not.publish.repository";
	public static final String COULD_NOT_SEND_SYNCHRONIZE_REQUEST = "could.not.send.synchronize.request";
	
	//validation - multipart
	public static final String INVALID_CONTENTTYPE = "invalid.contenttype";
	public static final String ERROR_EMPTY_FILE = "error.empty.file";
	public static final String INVALID_FILENAME = "invalid.filename";
	//validation - package
	public static final String EMPTY_NAME = "empty.name";
	public static final String EMPTY_VERSION = "empty.version";
	public static final String EMPTY_DESCRIPTION = "empty.description";
	public static final String EMPTY_AUTHOR = "empty.author";
	public static final String EMPTY_LICENSE = "empty.license";
	public static final String EMPTY_TITLE = "empty.title";
	public static final String EMPTY_MD5SUM = "empty.md5sum";
	public static final String INVALID_PACKAGE_NAME = "invalid.package.name";
	public static final String INVALID_VERSION = "invalid.version";
	public static final String DUPLICATE_VERSION = "duplicate.version";
	public static final String DUPLICATE_VERSION_IGNORED = "duplicate.version";
	
	//PACKAGES
	public static final String COULD_NOT_PARSE_PACKAGES_FILE = "could.not.parse.packages.file";
	
	//validation - repository maintainer
	public static final String EMPTY_USER = "empty.user";
	public static final String EMPTY_REPOSITORY = "empty.repository";
	public static final String REPOSITORYMAINTAINER_DUPLICATE = "repositorymaintainer.duplicate";
	
	//validation - package maintainer
	public static final String EMPTY_PACKAGE = "empty.package";
	public static final String PACKAGE_ALREADY_MAINTAINED = "package.already.maintained";
	public static final String USER_PERMISSIONS_NOT_SUFFICIENT = "user.permissions.not.sufficient";
	//validation - user
	public static final String ERROR_EMPTY_EMAIL = "empty.email";
	public static final String ERROR_EMPTY_LOGIN = "empty.login";
	public static final String ERROR_EMPTY_NAME = "empty.name";
	public static final String ERROR_INVALID_EMAIL = "invalid.email";
	public static final String ERROR_DUPLICATE_LOGIN = "duplicate.login";
	public static final String ERROR_DUPLICATE_EMAIL = "duplicate.email";
	public static final String ERROR_USER_NOT_FOUND = "user.not.found";

	
	//validation - repository
	public static final String EMPTY_PUBLICATIONURI = "empty.publicationuri";
	public static final String EMPTY_SERVERADDRESS = "empty.serveraddress";
	public static final String ERROR_DUPLICATE_NAME = "repository.duplicate.name";
	public static final String ERROR_DUPLICATE_PUBLICATIONURI = "repository.duplicate.publicationuri";
	public static final String DUPLICATE_SERVERADDRESS = "repository.duplicate.serveraddress";
	
	
	public static final String TECHNOLOGY_NOT_SUPPORTED = "technology.not.supported";
	
	public static final String COULD_NOT_CREATE_TEMPORARY_FOLDER = "could.not.create.temporary.folder";
	public static final String ERROR_DOWNLOAD_FILE = "error.download.file";
	public static final String UPDATE_PACKAGE_EXCEPTION = "error.update.package";
	public static final String NO_SUCH_PACKAGE_ERROR = "error.no.such.package";
	public static final String COULD_NOT_DOWNLOAD_PACKAGES_FILE = "could.not.download.packages.file";
	public static final String RESOLVE_RELATED_ENTITIES_EXCEPTION = "could.not.resolve.related.entities";
	
	//R-related!
	public static final String COULD_NOT_GET_REFERENCE_MANUAL = "could.not.get.reference.manual";
	public static final String COULD_NOT_GET_VIGNETTE = "could.not.get.vignette";
	public static final String ERROR_ORGANIZE_PACKAGES_IN_STORAGE = "error.organize.packages.in.storage";
	//security
	public static final String ROLE_NOT_FOUND = "role.not.found";
	public static final String USER_SOFT_DELETED = "user.soft.deleted";
	public static final String USER_INACTIVE = "user.inactive";

	
	//source
	public static final String COULD_NOT_FIND_SOURCE = "could.not.find.source";
	
	//services
	public static final String COULD_NOT_CREATE_ENTITY = "could.not.create.entity";
	public static final String COULD_NOT_DELETE_ENTITY = "could.not.delete.entity";
	public static final String NO_SUITABLE_REPOSITORY_FOUND = "no.suitable.repository.found";
	public static final String COULD_NOT_CALCULATE_MD5_SUM = "could.not.calculate.md5.sum";
	public static final String COULD_NOT_PARSE_PACKAGE_PROPERTIES = "could.not.parse.package.properties";
	public static final String COULD_NOT_CALCULATE_CHECKSUM = "could.not.calculate.checksum";
	public static final String COULD_NOT_GENERATE_MANUAL = "could.not.generate.manual";
	
	public static final String UNKNOWN_ERROR = "unknown.internal.server.error";
	
	public static final String FORBIDDEN_UPDATE = "forbidden.update";
	public static final String COULD_NOT_CHANGE_SUBMISSION = "could.not.change.submission";
	
	//email
//	public static final String NEW_SUBMISSION_EMAIL_SUBJECT = "new.submission.email.subject";


}
