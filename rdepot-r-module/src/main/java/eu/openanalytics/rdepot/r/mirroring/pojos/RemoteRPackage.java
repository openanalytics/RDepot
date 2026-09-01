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
package eu.openanalytics.rdepot.r.mirroring.pojos;

import eu.openanalytics.rdepot.base.entities.enums.HashMethod;
import eu.openanalytics.rdepot.base.mirroring.pojos.RemotePackage;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

public class RemoteRPackage extends RemotePackage {

    private final String md5sum;

    @Getter
    private final boolean archive;

    private RemoteRPackage(String name, String version, String md5sum, boolean archive) {
        super(name, version);
        this.md5sum = md5sum;
        this.archive = archive;
    }

    public RemoteRPackage(String name, String version, String md5sum) {
        this(name, version, md5sum, Strings.isEmpty(md5sum));
    }

    @Override
    public HashMethod getHashMethod() {
        return HashMethod.MD5;
    }

    @Override
    public String getHash() {
        return md5sum;
    }
}
