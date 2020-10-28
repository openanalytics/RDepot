/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest.ldap;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LDAPIntegrationTest {
	
	private RemoteWebDriver driver;
	
	private final String url = "oa-rdepot-app:8080";
								
	@Before
    public void setUp() throws MalformedURLException{
        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), new ChromeOptions());
    }
	
	@After
    public void tearDown() {
        driver.quit();
    }
	
	@Test
	public void testLoginPage() {
      	driver.get(url);
		String title = driver.getTitle();
		
		assertEquals("RDepot - Login Page", title);
	}
	
	@Test
	public void logIn() throws InterruptedException {
      	driver.get(url);
      	driver.findElementById("username").sendKeys("einstein");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
		String title = driver.getTitle();
		
		assertEquals("RDepot", title);
	} 
	
//	@Test
//	public void shouldUpdateUserNameAndEmail() throws IOException, ParseException {
//		JSONParser jsonParser = new JSONParser();
//		
//		FileReader reader = new FileReader(JSON_PATH + "/user/updated_users.json");
//		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
//		Set<JSONObject> expectedJSON = convert(rootJSON);
//		
//		given()
//			.header(AUTHORIZATION, BEARER + USER_TO_UPDATE_TOKEN)
//			.accept(ContentType.JSON)
//		.when()
//			.get("/api/manager/repositories/list")
//		.then()
//			.statusCode(200);			
//		
//		String data = given()
//			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
//			.accept(ContentType.JSON)
//		.when()
//			.get(API_PATH + "/list")
//		.then()
//			.statusCode(200)
//			.extract()
//			.asString();
//		
//		rootJSON = (JSONArray) jsonParser.parse(data);
//
//		Set<JSONObject> actualJSON = convert(rootJSON);
//
//		assertEquals("User hasn't been updated", expectedJSON, actualJSON);
//	}
}
