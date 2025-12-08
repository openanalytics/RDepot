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
package eu.openanalytics.rdepot.integrationtest.manager.v2.declarative;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.http.ContentType;
import java.io.IOException;

public abstract class DeclarativeIntegrationTest {

    public static final String ADMIN_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiIsIm5hbWUiOiJBbGJlcnQgRWluc3RlaW4iLCJlbWFpbCI6ImVpbnN0ZWluQGxkYXAuZm9ydW1zeXMuY29tIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIiwicmVwb3NpdG9yeW1haW50YWluZXIiLCJhZG1pbiJdLCJpc3MiOiJSRGVwb3QiLCJleHAiOjIwMDcwMjcyNDgsImlhdCI6MTY5MTY2NzI0OH0.SycsCWDmEFZfWV7cMpc05KareRXQ3iKfM9iprBa-j6M27D0hg0uKS1eGEPIuAHXEdqyUSD6yv7WMeXNY9BuYdw";
    public static final String REPOSITORYMAINTAINER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSIsIm5hbWUiOiJOaWtvbGEgVGVzbGEiLCJlbWFpbCI6InRlc2xhQGxkYXAuZm9ydW1zeXMuY29tIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIiwicmVwb3NpdG9yeW1haW50YWluZXIiXSwiaXNzIjoiUkRlcG90IiwiZXhwIjoyMDA3MDI3NDU3LCJpYXQiOjE2OTE2Njc0NTd9.6o7URshlNb91K9DKig79XIk9ozhomwaBmLg6im1JgbeWfJJUOP9k-gLTmWWHZkBC32MGKKFR-U11QzYY6G7zsw";
    public static final String PACKAGEMAINTAINER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYWxpZWxlbyIsImVtYWlsIjoiZ2FsaWVsZW9AbGRhcC5mb3J1bXN5cy5jb20iLCJuYW1lIjoiR2FsaWxlbyBHYWxpbGVpIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIl0sImlzcyI6IlJEZXBvdCIsImV4cCI6MjAwNzAyNzQ5MSwiaWF0IjoxNjkxNjY3NDkxfQ.24gRyDswxCmos1mUTkRJEKkrt3L2MFfyHEXa_H5EBhi3yirIN8AT7Bn_NYaTEtGcEfVd8NUQtgzm9uck76N2SQ";
    public static final String USER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24iLCJuYW1lIjoiSXNhYWMgTmV3dG9uIiwiZW1haWwiOiJuZXd0b25AbGRhcC5mb3J1bXN5cy5jb20iLCJhdWQiOiJSRGVwb3QiLCJyb2xlcyI6WyJ1c2VyIl0sImlzcyI6IlJEZXBvdCIsImV4cCI6MjAwNzAyNzUwOSwiaWF0IjoxNjkxNjY3NTA5fQ.waNTEOoLL0jkDpvihngEg_O6_W91wvIcSdtcXIBYiTeE5SbyLL60FFztYwuUwo-aEghzqnQlfVj4NATZMWgA-g";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";

    private final String apiPath;

    protected DeclarativeIntegrationTest(String apiPath) {
        this.apiPath = apiPath;
    }

    protected void removeFields(JsonObject json) {
        try {
            final JsonObject jsonData = json.getAsJsonObject("data");
            if (jsonData == null) return;
            if (jsonData.has("packageBag")) {
                JsonObject jsonPackage = (JsonObject) jsonData.get("packageBag");
                jsonPackage.remove("source");
            }
            if (jsonData.has("relatedResource")) {
                JsonObject jsonRelatedResource = jsonData.getAsJsonObject("relatedResource");
                jsonRelatedResource.remove("lastLoggedInOn");
            }
            jsonData.remove("lastLoggedInOn");
            jsonData.remove("createdOn");
            jsonData.remove("creationDate");
            jsonData.remove("expirationDate");
            jsonData.remove("value");
            jsonData.remove("lastPublicationTimestamp");
            jsonData.remove("lastModifiedTimestamp");
            jsonData.remove("created");

            JsonArray expectedContent = jsonData.getAsJsonArray("content");
            if (expectedContent != null) {
                for (int i = 0; i < expectedContent.size(); i++) {
                    JsonObject el = expectedContent.get(i).getAsJsonObject();
                    if (el.has("packageBag")) {
                        JsonObject jsonPackage = el.getAsJsonObject("packageBag");
                        jsonPackage.remove("source");
                    }
                    if (el.has("relatedResource")) {
                        JsonObject jsonRelatedResource = el.getAsJsonObject("relatedResource");
                        jsonRelatedResource.remove("lastLoggedInOn");
                    }
                    el.remove("lastLoggedInOn");
                    el.remove("createdOn");
                    el.remove("creationDate");
                    el.remove("expirationDate");
                    el.remove("value");
                    el.remove("lastPublicationTimestamp");
                    el.remove("lastModifiedTimestamp");
                    el.remove("created");
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    protected void assertPackages(JsonObject expectedJSON, boolean isSynchronized) throws IOException {
        String data = given().header(AUTHORIZATION, BEARER + USER_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .get(apiPath + "/packages?sort=id,asc")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonObject actualJSON = (JsonObject) JsonParser.parseString(data);

        JsonArray expectedContent = expectedJSON
                .getAsJsonObject("data")
                .getAsJsonObject()
                .get("content")
                .getAsJsonArray();

        JsonArray actualContent = actualJSON
                .getAsJsonObject("data")
                .getAsJsonObject()
                .get("content")
                .getAsJsonArray();

        if (isSynchronized) updateMd5SumsAndVersion(expectedContent);

        for (JsonElement el : expectedContent) {
            el.getAsJsonObject().remove("source");
            if (el.getAsJsonObject().get("name").getAsString().equals("genefilter")) {
                el.getAsJsonObject().remove("version");
            }
        }
        for (JsonElement el : actualContent) {
            el.getAsJsonObject().remove("source");
            if (el.getAsJsonObject().get("name").getAsString().equals("genefilter")) {
                el.getAsJsonObject().remove("version");
            }
        }

        assertEquals("Incorrect JSON output.", expectedContent, actualContent);
    }

    protected void assertRepositories(JsonObject expectedJSON) {
        String data = given().header(AUTHORIZATION, BEARER + USER_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .get(apiPath + "/repositories?sort=id,asc")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonObject actualJSON = (JsonObject) JsonParser.parseString(data);

        removeFields(actualJSON);
        removeFields(expectedJSON);

        assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);
    }

    protected Boolean assertSynchronizationFinished(String repositoryId) {
        String response = given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .get(apiPath + "/repositories/" + repositoryId + "/synchronization-status")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonObject actualJson = (JsonObject) JsonParser.parseString(response);

        return actualJson
                        .get("data")
                        .getAsJsonObject()
                        .get("repository")
                        .getAsJsonObject()
                        .get("id")
                        .getAsString()
                        .equals(repositoryId)
                && !actualJson
                        .get("data")
                        .getAsJsonObject()
                        .get("status")
                        .getAsString()
                        .equals("PENDING");
    }

    protected void assertSynchronizationStatus(JsonObject expectedJSON, String repoId, String size, String page) {

        String data;

        if (size != null && page != null) {
            data = given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .when()
                    .get(apiPath + "/repositories/" + repoId
                            + "/synchronization-status"
                            + "?size=" + size
                            + "&page=" + page)
                    .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        } else if (size != null) {
            data = given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .when()
                    .get(apiPath + "/repositories/" + repoId + "/synchronization-status" + "?size=" + size)
                    .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        } else {
            data = given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .when()
                    .get(apiPath + "/repositories/" + repoId + "/synchronization-status")
                    .then()
                    .statusCode(200)
                    .extract()
                    .asString();
        }

        JsonObject actualJSON = (JsonObject) JsonParser.parseString(data);

        removeTimestampField(actualJSON);
        removeTimestampField(expectedJSON);

        assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);
    }

    private void removeTimestampField(JsonObject json) {
        try {
            final JsonObject jsonData = json.getAsJsonObject("data");
            if (jsonData == null) return;
            jsonData.remove("timestamp");
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    protected abstract void updateMd5SumsAndVersion(JsonArray expectedContent) throws IOException;
}
