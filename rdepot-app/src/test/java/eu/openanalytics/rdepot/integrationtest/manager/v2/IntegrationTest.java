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
package eu.openanalytics.rdepot.integrationtest.manager.v2;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.openanalytics.rdepot.integrationtest.IntegrationTestContainers;
import eu.openanalytics.rdepot.integrationtest.environment.BashTestEnvironmentConfigurator;
import eu.openanalytics.rdepot.integrationtest.environment.TestEnvironmentConfigurator;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionMultipartBody;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.validator.GenericValidator;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class IntegrationTest {

    public static final String ADMIN_TOKEN = "ZWluc3RlaW46QUZGUXpqRkl4THpKamlJNlVFRTF1U2R1VnA1OXFpc08=";
    public static final String REPOSITORYMAINTAINER_TOKEN = "dGVzbGE6OFRpTE1XUTVLQzBnNXZoMktBOTcxQ0I2WXNaSTk1d1U=";
    public static final String PACKAGEMAINTAINER_TOKEN = "Z2FsaWVsZW86SHROQ1BkakJjNXRWdm40M2lxNm5VUjlKaERlSjNoQjI=";
    public static final String USER_TOKEN = "bmV3dG9uOlhXeVdzYlpjTDc5VnplU1VIRVczdlNYS1dFV1d1VEN2";

    public static final String NEW_USER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXdiaWUiLCJuYW1lIjoiTmV3IFVzZXIiLCJlbWFpbCI6Im5ld2JpZUBsb2NhbGhvc3QiLCJhdWQiOiJSRGVwb3QiLCJyb2xlcyI6WyJ1c2VyIl0sImlzcyI6IlJEZXBvdCIsImV4cCI6MjAwNzAyNzI0OCwiaWF0IjoxNjkxNjY3MjQ4fQ.E0mhFtUxpTvCGdySjizgcVskmMRtyKyQq1BZAC9T6HjE1jxvNsMhysObIhsvjm4bn5Ypf-DcX5rsliw9FRaQoA";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String BASIC = "Basic ";
    public static final String JSON_PATH = "src/test/resources/JSONs";
    public static final String PUBLICATION_URI_PATH = "/repo";

    private static final boolean RUN_CONTAINERS = true;
    protected static final TestEnvironmentConfigurator testEnv = BashTestEnvironmentConfigurator.getInstance();

    public final String apiPath;
    public String technology;

    public IntegrationTest(String apiPath) {
        this.apiPath = apiPath;
    }

    public void testEndpoint(TestRequestBody requestBody) throws Exception {
        int eventsNumberBeforeOperation = getTotalEventsAmount();
        chooseEndpoint(requestBody);
        int eventsNumberAfterOperation = getTotalEventsAmount();
        int result = eventsNumberAfterOperation - eventsNumberBeforeOperation;
        if (requestBody.getExpectedEventsJson().isPresent()) {
            testIfNewestEventsAreCorrect(
                    requestBody.getHowManyNewEventsShouldBeCreated(),
                    requestBody.getExpectedEventsJson().get());
        }
        Assertions.assertEquals(
                requestBody.getHowManyNewEventsShouldBeCreated(),
                result,
                "wrong number of events created after the operation");
    }

    @BeforeAll
    public static void setUpForAll() {
        if (RUN_CONTAINERS) {
            IntegrationTestContainers.startContainersIfNotRunningYet();
        }
        RestAssured.port = 8017;
        RestAssured.urlEncodingEnabled = false;
    }

    @AfterAll
    public static void turnDownForAll() {
        if (RUN_CONTAINERS) IntegrationTestContainers.stopContainersIfAllTestsCompleted();
    }

    @BeforeEach
    public final void setUp() throws Exception {
        testEnv.restoreEnvironment();
    }

    private void chooseEndpoint(TestRequestBody req) throws Exception {
        RequestType requestType = req.getRequestType();

        switch (requestType) {
            case GET:
                testGetEndpoint(
                        req.getExpectedJsonPath(), req.getUrlSuffix(), req.getStatusCode(), req.getToken(), false);
                break;
            case GET_OTHER_RESOURCE, GET_WITH_NEW_PATCH:
                testGetEndpoint(
                        req.getExpectedJsonPath(),
                        req.getPath().orElseThrow(),
                        req.getUrlSuffix(),
                        req.getStatusCode(),
                        req.getToken());
                break;
            case GET_ARRAY:
                testGetArrayEndpoint(
                        req.getExpectedJsonPath(), req.getUrlSuffix(), req.getStatusCode(), req.getToken());
                break;
            case GET_UNAUTHENTICATED:
                testGetEndpointUnauthenticated(req.getUrlSuffix());
                break;
            case GET_UNAUTHORIZED:
                testGetEndpointUnauthorized(req.getUrlSuffix(), req.getToken());
                break;
            case GET_AFTER_NEW_SUBMISSION:
                testGetEndpoint(
                        req.getExpectedJsonPath(), req.getUrlSuffix(), req.getStatusCode(), req.getToken(), true);
                break;
            case PATCH:
                testPatchEndpoint(
                        req.getBody().orElseThrow(),
                        req.getExpectedJsonPath(),
                        req.getUrlSuffix(),
                        req.getStatusCode(),
                        req.getToken());
                break;
            case PATCH_UNAUTHENTICATED:
                testPatchEndpointUnauthenticated(req.getBody().orElseThrow(), req.getUrlSuffix());
                break;
            case PATCH_UNAUTHORIZED:
                testPatchEndpointUnauthorized(req.getBody().orElseThrow(), req.getUrlSuffix(), req.getToken());
                break;
            case POST:
                testPostEndpoint(
                        req.getBody().orElseThrow(),
                        req.getExpectedJsonPath(),
                        req.getStatusCode(),
                        req.getToken(),
                        req.getUrlSuffix());
                break;
            case POST_UNAUTHENTICATED:
                testPostEndpoint_asUnauthenticated(req.getBody().orElseThrow());
                break;
            case POST_UNAUTHORIZED:
                testPostEndpoint_asUnauthorized(req.getBody().orElseThrow(), req.getToken());
                break;
            case POST_MULTIPART:
                testPostMultipartEndpoint(
                        req.getSubmissionMultipartBody().orElseThrow(),
                        req.getExpectedJsonPath(),
                        req.getStatusCode(),
                        req.getToken(),
                        req.getPath().orElse(apiPath));
                break;
            case DELETE:
                testDeleteEndpoint(req.getUrlSuffix(), req.getStatusCode(), req.getToken());
                break;
            case DELETE_UNAUTHENTICATED:
                testDeleteEndpointUnauthenticated(req.getUrlSuffix());
                break;
            case DELETE_UNAUTHORIZED:
                testDeleteEndpointUnauthorized(req.getUrlSuffix(), req.getToken());
                break;
            default:
                break;
        }
    }

    protected void testGetEndpoint(
            String expectedJsonPath, String urlSuffix, int statusCode, String token, boolean newSubmission)
            throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .when()
                .get(apiPath + urlSuffix)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        if (actualJSON.get("data") != null) removeFields(actualJSON, newSubmission);
        if (expectedJSON.get("data") != null) removeFields(expectedJSON, newSubmission);

        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output.");
    }

    protected void testGetArrayEndpoint(String expectedJsonPath, String urlSuffix, int statusCode, String token)
            throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .when()
                .get(apiPath + urlSuffix)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        if (actualJSON.get("data") != null) removeFields(actualJSON, false);
        if (expectedJSON.get("data") != null) removeFields(expectedJSON, false);

        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output.");
    }

    protected void testGetEndpoint(String expectedJsonPath, String path, String urlSuffix, int statusCode, String token)
            throws Exception {

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .when()
                .get(path + urlSuffix)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        if (actualJSON.get("data") != null) removeFields(actualJSON, false);
        if (expectedJSON.get("data") != null) removeFields(expectedJSON, false);

        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output.");
    }

    protected void testDeleteEndpoint(String urlSuffix, int statusCode, String token) {
        given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .when()
                .delete(apiPath + urlSuffix)
                .then()
                .statusCode(statusCode);
    }

    protected void testGetEndpointUnauthenticated(String urlSuffix) throws Exception {

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().accept(ContentType.JSON)
                .when()
                .get(apiPath + urlSuffix)
                .then()
                .statusCode(401)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
    }

    protected void testGetEndpointUnauthorized(String urlSuffix, String token) throws Exception {

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().accept(ContentType.JSON)
                .header(AUTHORIZATION, (token.equals(NEW_USER_TOKEN) ? BEARER : BASIC) + token)
                .when()
                .get(apiPath + urlSuffix)
                .then()
                .statusCode(403)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
    }

    protected void testPatchEndpointUnauthenticated(String patch, String urlSuffix) throws Exception {

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(patch)
                .when()
                .patch(apiPath + urlSuffix)
                .then()
                .statusCode(401)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output.");
    }

    protected void testPatchEndpointUnauthorized(String patch, String urlSuffix, String token) throws Exception {

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(patch)
                .when()
                .patch(apiPath + urlSuffix)
                .then()
                .statusCode(403)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output.");
    }

    protected void testPatchEndpoint(
            String patch, String expectedJsonPath, String urlSuffix, int statusCode, String token) throws Exception {

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(patch)
                .when()
                .patch(apiPath + urlSuffix)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        if (actualJSON.get("data") != null) removeFields(actualJSON, false);
        if (expectedJSON.get("data") != null) removeFields(expectedJSON, false);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
    }

    protected void testPostEndpoint(
            String body, String expectedJsonPath, int statusCode, String token, String urlSuffix) throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(apiPath + urlSuffix)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        removeFields(actualJSON, false);
        removeFields(expectedJSON, false);
        Assertions.assertEquals(expectedJSON, actualJSON, "There are some differences in packages that user sees.");
    }

    protected void testPostEndpoint(String body, String expectedJsonPath, int statusCode, String token)
            throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(apiPath)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        removeFields(actualJSON, false);
        removeFields(expectedJSON, false);
        Assertions.assertEquals(expectedJSON, actualJSON, "There are some differences in packages that user sees.");
    }

    protected void testPostEndpoint(String body, String path, String expectedJsonPath, int statusCode, String token)
            throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path)
                .then()
                .statusCode(statusCode)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        removeFields(actualJSON, false);
        removeFields(expectedJSON, false);
        Assertions.assertEquals(expectedJSON, actualJSON, "There are some differences in packages that user sees.");
    }

    protected void testPostMultipartEndpoint(
            SubmissionMultipartBody body, String expectedJsonPath, int statusCode, String token, String path)
            throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
        String data;
        if (body.getReplace() != null && body.getGenerateManual() != null) {
            data = given().header(AUTHORIZATION, BASIC + token)
                    .accept(ContentType.JSON)
                    .contentType("multipart/form-data")
                    .multiPart("repository", body.getRepository())
                    .multiPart("generateManual", body.getGenerateManual())
                    .multiPart("replace", body.getReplace())
                    .multiPart("changes", body.getChanges())
                    .multiPart(body.getMultipartFile())
                    .when()
                    .post(path)
                    .then()
                    .statusCode(statusCode)
                    .extract()
                    .asString();
        } else {
            data = given().header(AUTHORIZATION, BASIC + token)
                    .accept(ContentType.JSON)
                    .contentType("multipart/form-data")
                    .multiPart("repository", body.getRepository())
                    .multiPart("changes", body.getChanges())
                    .multiPart(body.getMultipartFile())
                    .when()
                    .post(apiPath)
                    .then()
                    .statusCode(statusCode)
                    .extract()
                    .asString();
        }

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        removeFields(actualJSON, true);
        removeFields(expectedJSON, true);
        Assertions.assertEquals(expectedJSON, actualJSON, "There are some differences in packages that user sees.");
    }

    protected void testPostEndpoint_asUnauthenticated(String body) throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(apiPath)
                .then()
                .statusCode(401)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "There are some differences in packages that user sees.");
    }

    protected void testPostEndpoint_asUnauthorized(String body, String token) throws Exception {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().header(AUTHORIZATION, BASIC + token)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(apiPath)
                .then()
                .statusCode(403)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "There are some differences in packages that user sees.");
    }

    protected void testDeleteEndpointUnauthenticated(String suffix) {
        given().accept(ContentType.JSON).when().delete(apiPath + suffix).then().statusCode(401);
    }

    protected void testDeleteEndpointUnauthorized(String suffix, String token) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().accept(ContentType.JSON)
                .header(AUTHORIZATION, BASIC + token)
                .when()
                .delete(apiPath + suffix)
                .then()
                .statusCode(403)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
    }

    protected String extractContent(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    protected byte[] readFileToByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
        return new byte[] {};
    }

    private int getTotalEventsAmount() throws JsonProcessingException {

        String data = given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .body("{resourceType: ${resourceType}}")
                .when()
                .get("/api/v2/manager/events")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonNode eventsNode = new ObjectMapper().readTree(data);

        JsonNode result = eventsNode.get("data").get("page").get("totalElements");

        return Integer.parseInt(result.toString());
    }

    private void testIfNewestEventsAreCorrect(int howMany, String expectedJsonPath) throws IOException {

        String data = given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .get("/api/v2/manager/events")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonNode expectedJSON = new ObjectMapper()
                .readTree(new File(JSON_PATH + expectedJsonPath))
                .get("data")
                .get("content");
        JsonNode eventsNode = new ObjectMapper().readTree(data).get("data").get("content");
        List<ObjectNode> eventsNodeConverted = convertEvents(eventsNode, howMany);

        Assertions.assertEquals(convertEvents(expectedJSON, howMany), eventsNodeConverted, "Events are not equal");
    }

    private List<ObjectNode> convertEvents(JsonNode events, int howMany) {
        List<ObjectNode> result = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            ObjectNode tmpEvent = (ObjectNode) events.get(i);
            tmpEvent.remove("time");
            ObjectNode relatedResourceObject = (ObjectNode) tmpEvent.get("relatedResource");
            if (relatedResourceObject == null) continue;

            removeDateIfExists(relatedResourceObject);
            if (relatedResourceObject.has("packageBag")) {
                ObjectNode packageBag = (ObjectNode) relatedResourceObject.get("packageBag");
                if (packageBag.has("source")) {
                    packageBag.remove("source");
                }
            }
            if (relatedResourceObject.has("lastUsed")) {
                relatedResourceObject.remove("lastUsed");
            }
            if (relatedResourceObject.has("lastLoggedInOn")) {
                relatedResourceObject.remove("lastLoggedInOn");
            }

            result.add(tmpEvent);
        }
        return result;
    }

    private void removeDateIfExists(ObjectNode relatedResourceObject) {
        if (relatedResourceObject == null) return;
        // throw new IllegalArgumentException("No related resource found in the event.");

        List<String> toRemove = new ArrayList<>();
        Iterator<Entry<String, JsonNode>> fieldIterator = relatedResourceObject.fields();
        while (fieldIterator.hasNext()) {
            Entry<String, JsonNode> entry = fieldIterator.next();
            String value = entry.getValue().asText();
            if (GenericValidator.isDate(value, "yyyy-MM-dd", false)) {
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(relatedResourceObject::remove);
    }

    private void removeFields(JSONObject json, boolean newSubmission) {
        try {
            JSONObject jsonData = (JSONObject) json.get("data");
            if (jsonData == null) return;
            if (jsonData.get("packageBag") != null) {
                JSONObject jsonPackage = (JSONObject) jsonData.get("packageBag");
                jsonPackage.remove("source");
            }
            if (jsonData.get("relatedResource") != null) {
                JSONObject jsonRelatedResource = (JSONObject) jsonData.get("relatedResource");
                if (jsonRelatedResource.get("lastLoggedInOn") != null) {
                    jsonRelatedResource.remove("lastLoggedInOn");
                }
                if (jsonRelatedResource.get("lastUsed") != null) {
                    jsonRelatedResource.remove("lastUsed");
                }
            }
            if (jsonData.get("lastLoggedInOn") != null) {
                jsonData.remove("lastLoggedInOn");
            }
            if (jsonData.get("createdOn") != null) {
                jsonData.remove("createdOn");
            }
            if (jsonData.get("creationDate") != null) {
                jsonData.remove("creationDate");
            }
            if (jsonData.get("expirationDate") != null) {
                jsonData.remove("expirationDate");
            }
            if (jsonData.get("lastUsed") != null) {
                jsonData.remove("lastUsed");
            }
            if (jsonData.get("value") != null) {
                jsonData.remove("value");
            }
            if (jsonData.get("lastPublicationTimestamp") != null) {
                jsonData.remove("lastPublicationTimestamp");
            }
            if (jsonData.get("lastModifiedTimestamp") != null) {
                jsonData.remove("lastModifiedTimestamp");
            }
            if (newSubmission) {
                jsonData.remove("created");
            }

            JSONArray expectedContent =
                    (JSONArray) Objects.requireNonNull(jsonData).get("content");
            if (expectedContent == null) return;
            for (int i = 0; i < expectedContent.size(); i++) {
                JSONObject el = (JSONObject) expectedContent.get(i);
                if (el.get("packageBag") != null) {
                    JSONObject jsonPackage = (JSONObject) el.get("packageBag");
                    jsonPackage.remove("source");
                }
                if (el.get("relatedResource") != null) {
                    JSONObject jsonRelatedResource = (JSONObject) el.get("relatedResource");
                    if (jsonRelatedResource.get("lastLoggedInOn") != null) {
                        jsonRelatedResource.remove("lastLoggedInOn");
                    }
                    if (jsonRelatedResource.get("lastUsed") != null) {
                        jsonRelatedResource.remove("lastUsed");
                    }
                }
                if (el.get("lastLoggedInOn") != null) {
                    el.remove("lastLoggedInOn");
                }
                if (el.get("createdOn") != null) {
                    el.remove("createdOn");
                }
                if (el.get("creationDate") != null) {
                    el.remove("creationDate");
                }
                if (el.get("expirationDate") != null) {
                    el.remove("expirationDate");
                }
                if (el.get("lastUsed") != null) {
                    el.remove("lastUsed");
                }
                if (el.get("value") != null) {
                    el.remove("value");
                }
                if (el.get("lastPublicationTimestamp") != null) {
                    el.remove("lastPublicationTimestamp");
                }
                if (el.get("lastModifiedTimestamp") != null) {
                    el.remove("lastModifiedTimestamp");
                }
                if (newSubmission && i == 0) {
                    el.remove("created");
                }
            }
        } catch (ClassCastException ignored) {
        }
    }
}
