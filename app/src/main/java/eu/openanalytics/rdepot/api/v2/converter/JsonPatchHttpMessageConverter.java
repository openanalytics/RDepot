/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.api.v2.converter;

import java.io.IOException;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import jakarta.json.Json;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;

@Component
public class JsonPatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonPatch>{

	@Override
	protected boolean supports(Class<?> clazz) {
		return JsonPatch.class.isAssignableFrom(clazz);
	}

	@Override
	protected JsonPatch readInternal(Class<? extends JsonPatch> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		JsonPatch patch = null;
		
		try(JsonReader reader = Json.createReader(inputMessage.getBody())) {
			patch = Json.createPatch(reader.readArray());
		} catch(Exception e) {
			throw new HttpMessageNotReadableException(e.getMessage(), inputMessage);
		}
		
		return patch;
	}

	@Override
	protected void writeInternal(JsonPatch t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		throw new NotImplementedException();
	}
	
	public JsonPatchHttpMessageConverter() {
		super(MediaType.valueOf("application/json-patch+json"));
	}

}
