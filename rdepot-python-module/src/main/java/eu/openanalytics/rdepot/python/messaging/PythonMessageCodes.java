/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.python.messaging;

public class PythonMessageCodes {
    public static final String EMPTY_HASH = "empty.hash";
    public static final String READ_PYTHON_PROPERTIES_FILE_EXCEPTION = "read.python.properties.file.exception";
    public static final String COULD_NOT_PARSE_PACKAGE_INDEX_FILE = "could.not.parse.package.index.file";
    public static final String COULD_NOT_PARSE_REPOSITORY_INDEX_FILE = "could.not.parse.repository.index.file";
    public static final String COULD_NOT_FIND_DIST_INFO_FOLDER = "could.not.find.dist.info.folder";
    // binary properties
    public static final String EMPTY_COMPATIBILITY_TAGS = "empty.compatibility.tags";
    public static final String INDEX_DOWNLOAD_ERROR = "index.download.error";
}
