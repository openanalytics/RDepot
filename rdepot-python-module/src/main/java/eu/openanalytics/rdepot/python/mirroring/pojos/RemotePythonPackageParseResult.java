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
package eu.openanalytics.rdepot.python.mirroring.pojos;

import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class RemotePythonPackageParseResult {

    private final Optional<RemotePythonPackage> remotePackage;
    private final IndexFileParseResult result;
    private final String error;
    private final String packageName;

    private RemotePythonPackageParseResult(IndexFileParseResult errorResult, String packageName, String error) {
        this.result = errorResult;
        this.remotePackage = Optional.empty();
        this.packageName = packageName;
        this.error = error;
    }

    private RemotePythonPackageParseResult(RemotePythonPackage remotePackage) {
        this.result = IndexFileParseResult.OK;
        this.remotePackage = Optional.of(remotePackage);
        this.packageName = remotePackage.getName();
        this.error = "";
    }

    public static RemotePythonPackageParseResult createUrlErrorResult(String packageName, String error) {
        return new RemotePythonPackageParseResult(IndexFileParseResult.MALFORMED_URL, packageName, error);
    }

    public static RemotePythonPackageParseResult createOkResult(@NonNull RemotePythonPackage remotePythonPackage) {
        return new RemotePythonPackageParseResult(remotePythonPackage);
    }
}
