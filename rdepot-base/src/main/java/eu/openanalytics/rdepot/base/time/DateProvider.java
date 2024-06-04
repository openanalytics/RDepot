/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Allows mocking Date for testing.
 */
public class DateProvider {

    private static Date testDate = null;

    /**
     *  This method should only be used in Unit Tests.
     *  It makes the test date "current".
     */
    public static void setTestDate(Date date) {
        testDate = date;
    }

    /**
     *  This method should only be used in Unit Tests.
     *  It makes the test date "current".
     */
    public static void setTestDate(LocalDateTime localDateTime) {
        testDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Current date or mocked test date if specified earlier
     * using {@link #setTestDate(Date)} method.
     */
    public static Date now() {
        if (testDate != null) {
            return testDate;
        }
        return new Date();
    }

    /**
     * Current date or mocked test date if specified earlier
     * using {@link #setTestDate(Date)} method, in "yyyyMMdd" format.
     */
    public static String getCurrentDateStamp() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        final LocalDateTime currentDate = getCurrentDateTime();
        return dtf.format(currentDate);
    }

    public static LocalDate getCurrentDate() {
        return getCurrentDateTime().toLocalDate();
    }

    private static LocalDateTime getCurrentDateTime() {
        return testDate == null
                ? LocalDateTime.now()
                : testDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
