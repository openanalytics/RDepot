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
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the result of parsing remote packages based on a list of configured packages in YAML files*
 */
@Getter
@Setter
@NoArgsConstructor
public class ParseResult {
    /**
     * Hash of the remote package
     */
    private Optional<String> hash = Optional.empty();
    /**
     * Extracted URL from the remote repository for downloading the package
     */
    private Optional<String> downloadUrl = Optional.empty();
    /**
     * Result of parsing a given package
     */
    private IndexFileParseResult parseResult;
    /**
     * Version of the package, configured in the YAML file
     */
    private String version;

    public ParseResult(IndexFileParseResult result, String version) {
        this.parseResult = result;
        this.version = version;
    }
}
