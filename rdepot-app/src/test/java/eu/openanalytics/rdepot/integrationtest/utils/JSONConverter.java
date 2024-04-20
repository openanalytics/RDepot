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
package eu.openanalytics.rdepot.integrationtest.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.parser.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JSONConverter {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Set<JsonObject>> convertNewPackagesFromRepo(JsonArray rootJSON) throws ParseException {
		 List<Set<JsonObject>> JSON = new ArrayList<>();
				
		 for(int i = 0; i < rootJSON.size(); i++) {
			 JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
			 JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
			 Set JSONSet = new HashSet<>();
			 for(int k = 0; k < packagesJSON.size(); k++) {
				 JsonObject packageJSON = (JsonObject) packagesJSON.get(k);
				 JSONSet.add(fixMd5SumAndSource(packageJSON));
			}
			JSON.add(JSONSet);
		}
		return JSON;
	}
	
	public static JsonObject fixSubmissionEvent(JsonObject eventObj) {
		JsonObject packageJSON = (JsonObject)((JsonObject)((JsonObject)eventObj).get("relatedResource"))
				.get("packageBag");
		return fixMd5SumAndSource(packageJSON);
	}
	
	private static JsonObject fixChecksumAndSource(JsonObject packageJSON, String checksumType) {
		packageJSON = fixSource(packageJSON);
		packageJSON.remove(checksumType);
		return packageJSON;
	}
	
	public static JsonObject fixSource(JsonObject packageJSON) {
		if(!packageJSON.has("source"))
			return packageJSON;
		
		final String source = packageJSON.get("source").getAsString();
		packageJSON.remove("source");
		final String newSource = source.replaceFirst("/[0-9]{2}[0-9]+", "");
		packageJSON.addProperty("source", newSource);
		
		return packageJSON;
	}
	
	private static JsonObject fixMd5SumAndSource(JsonObject packageJSON) {
		return fixChecksumAndSource(packageJSON, "md5sum");
	}
}
