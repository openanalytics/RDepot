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
package eu.openanalytics.rdepot.base.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Allows mocking Date for testing.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateProvider {

    /**
     * -- SETTER --
     *   This method should only be used in Unit Tests.
     *   It makes the test date "current".
     */
    @Setter
    private static Instant testDate = null;

    /**
     * Current date or mocked test date if specified earlier
     * using {@link #setTestDate(Instant)} method.
     */
    public static Instant now() {
        if (testDate != null) {
            return testDate;
        }
        return Instant.now();
    }

    /**
     * Current date or mocked test date if specified earlier
     * using {@link #setTestDate(Instant)} method, in "yyyyMMdd" format.
     */
    public static String getCurrentDateStamp() {
        final DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
        final Instant currentDate = now();
        return dtf.format(currentDate.atZone(ZoneId.of("UTC")));
    }

    public static String instantToTimestamp(@NonNull Instant instant) {
        final DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
        return dtf.format(instant);
    }

    public static String instantToDatestampWithoutHyphens(@NonNull Instant instant) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        return dtf.format(instant.atZone(ZoneId.of("UTC")));
    }

    public static Instant timestampToInstant(@NonNull String timestamp) {
        if (timestamp.isEmpty()) return Instant.EPOCH;

        final DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
        return dtf.parse(timestamp, Instant::from);
    }
}
