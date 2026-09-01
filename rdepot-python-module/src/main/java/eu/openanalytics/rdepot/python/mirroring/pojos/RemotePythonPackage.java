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

import eu.openanalytics.rdepot.base.entities.enums.HashMethod;
import eu.openanalytics.rdepot.base.mirroring.pojos.RemotePackage;
import lombok.Getter;

@Getter
public class RemotePythonPackage extends RemotePackage {

    private final String downloadUrl;
    private final String hash;

    public RemotePythonPackage(String name, String version, String downloadUrl, String hash) {
        super(name, version);
        this.downloadUrl = downloadUrl;
        this.hash = hash;
    }

    @Override
    public HashMethod getHashMethod() {
        return null;
    }

    @Override
    public String getHash() {
        return hash;
    }
}
