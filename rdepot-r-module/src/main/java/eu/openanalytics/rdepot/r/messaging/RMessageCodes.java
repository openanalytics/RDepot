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
package eu.openanalytics.rdepot.r.messaging;

public class RMessageCodes {
    public static final String ERROR_MISSING_DATA_FOR_BINARY_PACKAGE = "error.missing.data.for.binary.package";
    public static final String ERROR_PARAMETERS_NOT_ALLOWED_FOR_NON_BINARY_PACKAGE =
            "error.parameters.not.allowed.for.non.binary.package";
    public static final String ERROR_CLEAN_FS = "error.clean.fs";
    public static final String READ_R_PACKAGE_DESCRIPTION_EXCEPTION = "read.r.package.description.exception";
    public static final String EMPTY_AUTHOR = "empty.author";
    public static final String EMPTY_TITLE = "empty.title";
    public static final String EMPTY_MD5SUM = "empty.md5sum";

    // validation - binary package
    public static final String R_VERSION_NOT_ALLOWED = "r.version.not.allowed";
    public static final String INVALID_R_VERSION = "invalid.r.version";
    public static final String ARCHITECTURE_NOT_ALLOWED = "architecture.not.allowed";
    public static final String INVALID_ARCHITECTURE = "invalid.architecture";
    public static final String DISTRIBUTION_NOT_ALLOWED = "distribution.not.allowed";
    public static final String EMPTY_BUILT = "empty.built";

    public static final String COULD_NOT_PARSE_PACKAGES_FILE = "could.not.parse.packages.file";
    public static final String UPDATE_PACKAGE_EXCEPTION = "error.update.package";
    public static final String COULD_NOT_DOWNLOAD_PACKAGES_FILE = "could.not.download.packages.file";
    public static final String COULD_NOT_GET_REFERENCE_MANUAL = "could.not.get.reference.manual";
    public static final String COULD_NOT_GET_VIGNETTE = "could.not.get.vignette";
    public static final String COULD_NOT_GENERATE_MANUAL = "could.not.generate.manual";
    public static final String GENERATE_MANUAL_NOT_SUPPORTED = "generate.manual.not.supported";
    public static final String COULD_NOT_GENERATE_PACKAGES_FILE = "could.not.generate.packages.file";
}
