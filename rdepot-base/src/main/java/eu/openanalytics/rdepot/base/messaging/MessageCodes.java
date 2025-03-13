/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCodes {

    // API - Success messages
    public static final String SUCCESS_REQUEST_PROCESSED = "success.request.processed";
    public static final String SUCCESS_RESOURCE_CREATED = "success.resource.created";

    // API - Errors of unknown origin
    public static final String ERROR_APPLY_PATCH = "error.apply.patch";
    public static final String ERROR_DELETE_RESOURCE = "error.delete.resource";
    public static final String ERROR_CREATE_RESOURCE = "error.create.resource";
    public static final String ERROR_GET_SYNCHRONIZATION_STATUS = "error.get.synchronization.status";

    // API - Not found errors
    public static final String ERROR_SUBMISSION_NOT_FOUND = "submission.notfound";
    public static final String ERROR_PACKAGE_NOT_FOUND = "package.notfound";
    public static final String ERROR_PACKAGEMAINTAINER_NOT_FOUND = "error.packagemaintainer.notfound";
    public static final String ERROR_REPOSITORY_NOT_FOUND = "repository.notfound";
    public static final String ERROR_REPOSITORYMAINTAINER_NOT_FOUND = "error.repositorymaintainer.notfound";
    public static final String ERROR_USER_NOT_FOUND = "user.notfound";
    public static final String ERROR_EVENT_NOT_FOUND = "event.notfound";

    // API - Auth errors
    public static final String ERROR_USER_NOT_AUTHORIZED = "user.not.authorized";
    public static final String ERROR_USER_NOT_AUTHENTICATED = "error.user.not.authenticated";
    public static final String ERROR_ACCESS_TOKEN_NOT_FOUND = "error.access.token.not.found";

    // API - Miscellaneous errors
    public static final String ERROR_PACKAGE_GET_REFERENCE_MANUAL = "package.get.reference.manual";
    public static final String ERROR_REPOSITORY_DECLARATIVE_MODE = "repository.declarative.mode";
    public static final String ERROR_MANUAL_NOT_FOUND = "error.manual.not.found";
    public static final String ERROR_VIGNETTE_NOT_FOUND = "error.vignette.not.found";
    public static final String ERROR_DOWNLOAD_VIGNETTE = "error.download.vignette";
    public static final String DELETING_REPOSITORIES_DISABLED = "deleting.repositories.disabled";
    public static final String DELETING_PACKAGES_DISABLED = "deleting.packages.disabled";
    public static final String EDITING_DELETED_RESOURCE_NOT_POSSIBLE = "editing.deleted.resource.not.possible";

    // API - Validation
    public static final String ERROR_VALIDATION = "error.validation";
    public static final String ERROR_MALFORMED_PATCH = "error.malformed.patch";
    public static final String ERROR_INVALID_SUBMISSION = "error.invalid.submission";

    // Storage

    public static final String REPOSITORY_NOT_FOUND = "repository.not.found";
    public static final String NO_SUITABLE_MAINTAINER_FOUND = "no.suitable.maintainer.found";
    public static final String STRATEGY_FAILURE = "strategy.failure";
    public static final String MULTIPART_FILE_VALIDATION_EXCEPTION = "multipart.file.validation.exception";
    public static final String INVALID_SOURCE = "invalid.source";
    public static final String COULD_NOT_WRITE_TO_WAITING_ROOM = "could.not.write.to.waiting.room";
    public static final String COULD_NOT_DELETE_FILE = "could.not.delete.file";
    public static final String COULD_NOT_EXTRACT_FILE = "could.not.extract.file";
    public static final String SOURCE_FILE_DELETE_EXCEPTION = "source.file.delete.exception";
    public static final String COULD_NOT_MOVE_FILE = "could.not.move.file";
    public static final String COULD_NOT_MOVE_PACKAGE_SOURCE = "could.not.move.package.source";
    public static final String REVERSE_STRATEGY_FAILURE = "reverse.strategy.failure";
    public static final String WRONG_SERVICE_EXCEPTION = "wrong.service";
    public static final String ADMIN_NOT_FOUND = "admin.not.found";
    public static final String NO_ADMIN_LEFT = "no.admin.left";
    public static final String COULD_NOT_DELETE_REPOSITORY_DIRECTORY = "could.not.delete.repository.directory";
    public static final String COULD_NOT_CREATE_FOLDER_STRUCTURE = "could.not.create.folder.structure";
    public static final String COULD_NOT_GZIP_FILE = "could.not.gzip.file";
    public static final String COULD_NOT_SYNCHRONIZE_REPOSITORY = "could.not.synchronize.repository";
    public static final String MD5_MISMATCH_EXCEPTION = "md5.mismatch";
    public static final String COULD_NOT_POPULATE_PACKAGE_FOLDER = "could.not.populate.package.folder";
    public static final String MD5_CALCULATION_EXCEPTION = "md5.calculation.error";
    public static final String COULD_NOT_LINK_FOLDERS = "could.not.link.folders";
    public static final String COULD_NOT_CLEAN_UP_AFTER_SYNCHRONIZATION = "could.not.clean.up.after.synchronization";
    public static final String COULD_NOT_RESOLVE_EVENT_TYPE = "could.not.resolve.event.type";
    public static final String COULD_NOT_RESOLVE_RESOURCE_TYPE = "could.not.resolve.resource.type";
    public static final String COULD_NOT_SEND_SYNCHRONIZE_REQUEST = "could.not.send.synchronize.request";
    public static final String EMPTY_ARCHIVE = "empty.archive";

    // validation - multipart
    public static final String INVALID_CONTENTTYPE = "invalid.contenttype";
    public static final String ERROR_EMPTY_FILE = "error.empty.file";
    public static final String INVALID_FILENAME = "invalid.filename";
    // validation - package
    public static final String EMPTY_NAME = "empty.name";
    public static final String EMPTY_VERSION = "empty.version";
    public static final String EMPTY_DESCRIPTION = "empty.description";

    public static final String EMPTY_LICENSE = "empty.license";

    public static final String INVALID_PACKAGE_NAME = "invalid.package.name";
    public static final String INVALID_VERSION = "invalid.version";
    public static final String DUPLICATE_VERSION_REPLACE_ON = "duplicate.version.replace.on";
    public static final String DUPLICATE_VERSION_REPLACE_OFF = "duplicate.version.replace.off";

    // PACKAGES

    // validation - repository maintainer
    public static final String EMPTY_USER = "empty.user";
    public static final String EMPTY_REPOSITORY = "empty.repository";
    public static final String REPOSITORYMAINTAINER_DUPLICATE = "repositorymaintainer.duplicate";

    // validation - package maintainer
    public static final String EMPTY_PACKAGE = "empty.package";
    public static final String PACKAGE_ALREADY_MAINTAINED = "package.already.maintained";
    public static final String USER_PERMISSIONS_NOT_SUFFICIENT = "user.permissions.not.sufficient";

    // validation - user
    public static final String ERROR_EMPTY_EMAIL = "empty.email";
    public static final String ERROR_EMPTY_LOGIN = "empty.login";
    public static final String ERROR_EMPTY_NAME = "empty.name";
    public static final String ERROR_INVALID_EMAIL = "invalid.email";
    public static final String ERROR_DUPLICATE_LOGIN = "duplicate.login";
    public static final String ERROR_DUPLICATE_EMAIL = "duplicate.email";

    // validation - repository
    public static final String EMPTY_PUBLICATIONURI = "empty.publicationuri";
    public static final String EMPTY_SERVERADDRESS = "empty.serveraddress";
    public static final String ERROR_DUPLICATE_NAME = "repository.duplicate.name";
    public static final String ERROR_DUPLICATE_PUBLICATIONURI = "repository.duplicate.publicationuri";
    public static final String DUPLICATE_SERVERADDRESS = "repository.duplicate.serveraddress";

    // validation - user settings
    public static final String LANGUAGE_NOT_SUPPORTED = "language.not.supported";
    public static final String THEME_NOT_SUPPORTED = "theme.not.supported";
    public static final String PAGE_SIZE_LOWER_THAN_ONE_ELEMENT = "page.size.lower.than.one.element";
    public static final String PAGE_SIZE_BIGGER_THAN_MAX_LIMIT = "page.size.bigger.than.max.limit";
    public static final String COULD_NOT_CREATE_TEMPORARY_FOLDER = "could.not.create.temporary.folder";
    public static final String ERROR_DOWNLOAD_FILE = "error.download.file";
    public static final String NO_SUCH_PACKAGE_ERROR = "error.no.such.package";
    public static final String RESOLVE_RELATED_ENTITIES_EXCEPTION = "could.not.resolve.related.entities";
    public static final String INVALID_REPOSITORY_NAME = "invalid.repository.name";

    // R-related
    public static final String ERROR_ORGANIZE_PACKAGES_IN_STORAGE = "error.organize.packages.in.storage";

    // security
    public static final String ROLE_NOT_FOUND = "role.not.found";
    public static final String USER_SOFT_DELETED = "user.soft.deleted";
    public static final String USER_INACTIVE = "user.inactive";

    // source
    public static final String COULD_NOT_FIND_SOURCE = "could.not.find.source";

    // services
    public static final String COULD_NOT_CREATE_ENTITY = "could.not.create.entity";
    public static final String COULD_NOT_DELETE_ENTITY = "could.not.delete.entity";
    public static final String COULD_NOT_PARSE_PACKAGE_PROPERTIES = "could.not.parse.package.properties";
    public static final String COULD_NOT_CALCULATE_CHECKSUM = "could.not.calculate.checksum";

    public static final String FORBIDDEN_UPDATE = "forbidden.update";
    public static final String COULD_NOT_CHANGE_SUBMISSION = "could.not.change.submission";
    // access tokens
    public static final String TOKEN_NAME_MUST_BE_NOT_BLANK = "token.name.must.be.not.blank";
    public static final String TOKEN_NUMBER_OF_DAYS_MUST_BE_GREATER_THAN_0 =
            "token.number.of.days.must.be.greater.than.0";
    public static final String TOKEN_NUMBER_OF_DAYS_MUST_BE_LESS_THAN_366 =
            "token.number.of.days.must.be.less.than.366";
    public static final String DEACTIVATED_TOKEN_COULD_NOT_BE_CHANGED = "deactivated.token.could.not.be.changed";
    // declarative
    public static final String INVALID_REPOSITORY_DECLARATION = "invalid.repository.declaration";
    public static final String UNRECOGNIZED_QUERY_PARAMETER = "unrecognized.query.parameter";
    public static final String DECLARED_REPOSITORY_TECHNOLOGY_MISMATCH = "declared.repository.technology.mismatch";
    public static final String INTERNAL_ERROR = "internal.error";
    public static final String BAD_REQUEST = "bad.request";
    public static final String METHOD_NOT_ALLOWED = "method.not.allowed";
    public static final String NOT_ACCEPTABLE = "not.acceptable";
    public static final String UNSUPPORTED_MEDIA_TYPE = "unsupported.media.type";

    public static final String PACKAGE_PROCESSING_ERROR = "package.processing.error";
    public static final String PACKAGE_CREATE_ERROR = "package.create.error";
    public static final String SUBMISSION_CREATE_ERROR = "submission.create.error";
    public static final String WARNING_PACKAGE_DUPLICATE = "warning.package.duplicate";
    public static final String WARNING_REPLACING_PACKAGES_DISABLED = "warning.replacing.packages.disabled";
    public static final String WARNING_SYNCHRONIZATION_FAILURE = "warning.synchronization.failure";
    public static final String WARNING_UNKNOWN = "warning.unknown";
}
