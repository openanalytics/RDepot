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
package eu.openanalytics.rdepot.python.mirroring;

import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizationStatusCoordinator;
import eu.openanalytics.rdepot.base.mirroring.RemotePackageMapper;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.RemotePythonPackage;
import org.springframework.stereotype.Component;

@Component
public class PythonRemotePackageMapper
        extends RemotePackageMapper<MirroredPythonPackage, RemotePythonPackage, PypiMirror> {

    protected PythonRemotePackageMapper(MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator) {
        super(mirrorSynchronizationStatusCoordinator);
    }

    @Override
    public MirroredPythonPackage convertRemoteToExpected(RemotePythonPackage remotePackage, PypiMirror mirror) {
        return new MirroredPythonPackage(remotePackage.getName(), remotePackage.getVersion());
    }

    private String normalize(String name) {
        return name.replaceAll("[-_.]", "-").toLowerCase();
    }

    @Override
    protected boolean namesMatch(MirroredPythonPackage expectedPackage, RemotePythonPackage remotePackage) {
        return normalize(expectedPackage.getName()).equals(normalize(remotePackage.getName()));
    }
}
