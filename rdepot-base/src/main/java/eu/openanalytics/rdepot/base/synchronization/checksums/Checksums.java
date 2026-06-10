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
package eu.openanalytics.rdepot.base.synchronization.checksums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Checksums {

    private final Set<Checksum> checksums = new HashSet<>();

    public void addChecksum(Checksum checksum) {
        checksums.add(checksum);
    }

    public Checksums() {}

    /**
     * Maps all {@link Checksums}.
     * The resulting map will be like:<br/>
     * <code>
     *     {<br/>
     *     &emsp;"/opt/rdepot/generated/2/20220202/PACKAGES.gz": "123abc234bcd432"<br/>
     *     }<br/>
     * </code>
     */
    public Map<String, String> toMap() {
        final Map<String, String> result = new HashMap<>();
        checksums.forEach(checksum -> result.put(checksum.filePath(), checksum.toString()));
        return result;
    }

    public boolean contains(String checksumToCheck, String filePath) {
        return checksums.contains(new Checksum(filePath, checksumToCheck));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checksums that = (Checksums) o;
        return Objects.equals(this.checksums, that.checksums);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checksums);
    }
}
