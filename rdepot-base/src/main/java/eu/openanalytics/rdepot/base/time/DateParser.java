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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateParser {

    public static Optional<Instant> parseTimestampStart(@NonNull String timestamp) {
        return parseTimestamp(timestamp, true);
    }

    public static Optional<Instant> parseTimestampEnd(@NonNull String timestamp) {
        return parseTimestamp(timestamp, false);
    }

    private static Optional<Instant> parseTimestamp(@NonNull String timestamp, boolean start) {
        try {
            return Optional.of(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(timestamp)));
        } catch (DateTimeException ignored) {
        }

        try {
            final LocalDate date = LocalDate.parse(timestamp, DateTimeFormatter.ISO_DATE);
            final LocalDateTime dateTime = start ? date.atStartOfDay() : date.atTime(23, 59, 59);
            final Instant dateInstant = dateTime.toInstant(ZoneOffset.UTC);
            return Optional.of(dateInstant);
        } catch (DateTimeException ignored) {
        }

        return Optional.empty();
    }
}
