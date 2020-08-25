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
package eu.openanalytics.rdepot.integrationtest.openid;

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

public class OIDCIntegrationTest {
		
	private RemoteWebDriver driver;
	
	private final String url = "oa-rdepot-app-openid:8080";
								
	@Before
    public void setUp() throws IOException, InterruptedException{
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
		
		assertEquals("Please sign in", title);
	}
	
	@Test
	public void testProviderLoginPage() throws InterruptedException {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
		String title = driver.getTitle();
		
		assertEquals("Sign-in", title);
	}
	
	@Test
	public void testLogInAsAdmin() throws InterruptedException {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
      	driver.findElementByName("email").sendKeys("einstein@localhost");
      	driver.findElementByName("password").sendKeys("testpassword");
      	driver.findElementByName("submit").click();
		String title = driver.getTitle();
		
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Admin can see all 6 sections in menu", 6, sizeOfNavBar);
		
		assertEquals("RDepot", title);
	}
	
	@Test
	public void testLogInAsUser() throws InterruptedException {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
      	driver.findElementByName("email").sendKeys("newton@localhost");
      	driver.findElementByName("password").sendKeys("testpassword");
      	driver.findElementByName("submit").click();
		String title = driver.getTitle();
		
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Simple user can only see 4 sections in menu", 4, sizeOfNavBar);		
		assertEquals("RDepot", title);
	}
	
	@Test
	public void testCreateUserAccount() throws InterruptedException {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
      	driver.findElementByName("email").sendKeys("newbie@localhost");
      	driver.findElementByName("password").sendKeys("testpassword");
      	driver.findElementByName("submit").click();
		String title = driver.getTitle();
		
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Simple user can only see 4 sections in menu", 4, sizeOfNavBar);		
		assertEquals("RDepot", title);
	}
	
	@Test
	public void testProviderLogoutPage() {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
      	driver.findElementByName("email").sendKeys("einstein@localhost");
      	driver.findElementByName("password").sendKeys("testpassword");
      	driver.findElementByName("submit").click();
      	driver.findElementByName("logout").click();
      	
      	String title = driver.getTitle();
		
		assertEquals("Logout", title);
	}
	
	@Test
	public void testLogOut() throws InterruptedException {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
      	driver.findElementByName("email").sendKeys("einstein@localhost");
      	driver.findElementByName("password").sendKeys("testpassword");
      	driver.findElementByName("submit").click();
      	driver.findElementByName("logout").click();
      	driver.findElement(By.xpath("//button[text()='Yes']")).click();
      	
		String title = driver.getTitle();
		
		assertEquals("Please sign in", title);
	}
	
	@Test
	public void tryToLogInAsInactiveUser() throws InterruptedException {
      	driver.get(url);
      	driver.findElement(By.xpath("//a[@href='/oauth2/authorization/rdepot']")).click();
      	driver.findElementByName("email").sendKeys("doe@localhost");
      	driver.findElementByName("password").sendKeys("testpassword");
      	driver.findElementByName("submit").click();
      	
      	String errorMsg = driver.findElementById("error_message").getText();
		String title = driver.getTitle();
		
		assertEquals("This account is inactive", errorMsg);
		assertEquals("RDepot - Authentication failed", title);
	}
}
