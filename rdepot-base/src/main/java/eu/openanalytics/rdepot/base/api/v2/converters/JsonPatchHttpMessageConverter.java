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
package eu.openanalytics.rdepot.base.api.v2.converters;

import jakarta.json.Json;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

/**
 * Provides support for JSON Patch objects.
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6902">JSON Patch specification</a>
 */
@Component
public class JsonPatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonPatch> {

    @Override
    protected boolean supports(@NonNull Class<?> clazz) {
        return JsonPatch.class.isAssignableFrom(clazz);
    }

    @Override
    protected @NonNull JsonPatch readInternal(
            @NonNull Class<? extends JsonPatch> clazz, @NonNull HttpInputMessage inputMessage)
            throws HttpMessageNotReadableException {
        JsonPatch patch;

        try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
            patch = Json.createPatch(reader.readArray());
        } catch (Exception e) {
            throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
        }

        return patch;
    }

    @Override
    protected void writeInternal(@NonNull JsonPatch t, @NonNull HttpOutputMessage outputMessage)
            throws HttpMessageNotWritableException {
        throw new NotImplementedException();
    }

    public JsonPatchHttpMessageConverter() {
        super(MediaType.valueOf("application/json-patch+json"));
    }
}
