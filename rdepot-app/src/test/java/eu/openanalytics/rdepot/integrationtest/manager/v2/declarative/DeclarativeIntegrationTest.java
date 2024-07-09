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
package eu.openanalytics.rdepot.integrationtest.manager.v2.declarative;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.http.ContentType;
import java.io.IOException;
import org.json.simple.parser.ParseException;

public abstract class DeclarativeIntegrationTest {

    private final String authorization;
    private final String bearer;
    private final String userToken;
    private final String apiPath;

    protected DeclarativeIntegrationTest(String authorization, String bearer, String userToken, String apiPath) {
        this.authorization = authorization;
        this.bearer = bearer;
        this.userToken = userToken;
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
        String data = given().header(authorization, bearer + userToken)
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

    protected void assertRepositories(JsonObject expectedJSON) throws ParseException, IOException {
        String data = given().header(authorization, bearer + userToken)
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

    protected abstract void updateMd5SumsAndVersion(JsonArray expectedContent) throws IOException;
}
