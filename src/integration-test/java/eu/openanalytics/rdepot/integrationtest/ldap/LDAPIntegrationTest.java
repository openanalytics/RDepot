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
package eu.openanalytics.rdepot.integrationtest.ldap;

import static org.junit.Assert.assertEquals;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LDAPIntegrationTest {
	
	private RemoteWebDriver driver;
	
	private final String url = "http://192.168.49.21:8080";
								
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
	public void testUpdateFamilyName() throws InterruptedException {
      	driver.get(url);
      	driver.findElementById("username").sendKeys("tarski");
      	driver.findElementById("password").sendKeys("testpassword");
      	driver.findElementById("button").click();
		String title = driver.getTitle();
		String username = driver.findElementByName("username").getText();
		
		WebElement navBar = driver.findElementById("navbar");
		
		int sizeOfNavBar = navBar.findElements(By.xpath("./a")).size();
		
		assertEquals("Simple user can only see 4 sections in menu", 4, sizeOfNavBar);
		assertEquals("RDepot", title);
		assertEquals("Alfred Tajtelbaum", username);
	}
}
