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
package eu.openanalytics.rdepot.r.storage.packagesfile;

/**
 * Represents PACKAGES file
 * @param remoteFolder directory on the remote repo
 *                     (e.g. <code>src/contrib</code> or <code>src/contrib/Archive</code>)
 * @param localPath exact location in the local localStorage
 *                  (e.g. <code>
 *                      /opt/rdepot/repositories/5/generates/20250722/src/contrib/latest/PACKAGES
 *                  </code>)
 * @param checksum calculated checksum for this PACKAGES or PACKAGES.gz file
 */
public record PackagesFileDescriptor(String remoteFolder, String localPath, String checksum) {
    /**
     * @return e.g <code>src/contrib/PACKAGES</code> or <code>src/contrib/Archive/PACKAGES</code>
     */
    public String getSubPath() {
        final String[] tokens = localPath.split("/");
        if (tokens.length < 2) {
            throw new IllegalStateException("PACKAGES file has not been populated properly. "
                    + "PACKAGES file path in local localStorage: " + localPath);
        }
        final String filename = tokens[tokens.length - 1];
        final boolean isArchive = tokens[tokens.length - 2].equals("Archive");
        if (isArchive) {
            return "Archive/" + filename;
        } else {
            return filename;
        }
    }
}
