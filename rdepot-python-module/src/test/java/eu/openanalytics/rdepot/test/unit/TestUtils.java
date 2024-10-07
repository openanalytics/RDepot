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
package eu.openanalytics.rdepot.test.unit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import org.springframework.test.web.servlet.ResultActions;

public class TestUtils {

    private static void matchInternalServerError(ResultActions result, String message, String messageCode)
            throws Exception {
        result.andExpect(jsonPath("$.data.traceId").exists())
                .andExpect(jsonPath("$.data.timestamp").exists())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.messageCode").value(messageCode));
    }

    public static void matchInternalServerErrorCreate(ResultActions result) throws Exception {
        matchInternalServerError(
                result, "Could not create resource due to internal server error.", "error.create.resource");
    }

    public static void matchInternalServerErrorPatch(ResultActions result) throws Exception {
        matchInternalServerError(
                result, "Could not apply patch to requested resource.", MessageCodes.ERROR_APPLY_PATCH);
    }

    public static void matchInternalServerErrorDelete(ResultActions result) throws Exception {
        matchInternalServerError(result, "Could not delete requested resource.", "error.delete.resource");
    }
}
