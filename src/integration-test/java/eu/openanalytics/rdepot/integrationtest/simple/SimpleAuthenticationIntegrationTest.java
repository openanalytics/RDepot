/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest.simple;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SimpleAuthenticationIntegrationTest {
	private RemoteWebDriver driver;
	
	private final String url = "http://192.168.49.24:8080";
								
	@Before
    public void setUp() throws InterruptedException, IOException{
        driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), new ChromeOptions());
        String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
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
	public void logInAsAdmin() throws InterruptedException {
      	driver.get(url);
      	driver.findElementById("username").sendKeys("einstein");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
		String title = driver.getTitle();
		
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Admin should see all 6 sections in menu", 6, sizeOfNavBar);			
		assertEquals("RDepot", title);
	}
	
	@Test
	public void testLogInAsUser() throws InterruptedException {
		driver.get(url);
      	driver.findElementById("username").sendKeys("newton");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
		String title = driver.getTitle();
		
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Simple user can only see 4 sections in menu", 4, sizeOfNavBar);		
		assertEquals("RDepot", title);
	}
	
	@Test
	public void testCreateUserAccount() throws InterruptedException {
      	driver.get(url);
      	driver.findElementById("username").sendKeys("newbie");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
		String title = driver.getTitle();
				
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Simple user can only see 4 sections in menu", 4, sizeOfNavBar);		
		assertEquals("RDepot", title);
	}
		
	@Test
	public void testLogOut() throws InterruptedException {
      	driver.get(url);
      	driver.findElementById("username").sendKeys("einstein");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
      	driver.findElementByName("logout").click();
      	
		String title = driver.getTitle();
		
		assertEquals("It hasn't logged out", "RDepot - Login Page", title);
	}
	
	@Test
	public void tryToLogInAsInactiveUser() throws InterruptedException {
      	driver.get(url);
      	driver.findElementById("username").sendKeys("doe");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
		String title = driver.getTitle();
      	
      	String errorMsg = driver.findElementById("error_message").getText();
		
		assertEquals("Wrong error message displayed", "User John Doe: This account is inactive.", errorMsg);
		assertEquals("It hasn't stayed on login page", "RDepot - Login Page", title);
	}
}
