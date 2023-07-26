/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.r.daos.RPackageDao;
import eu.openanalytics.rdepot.r.services.RPackageService;

@ExtendWith(MockitoExtension.class)
public class PackageServiceTest {
	
	@Mock
	RPackageDao dao;
	
	RPackageService testedService;
	
	@BeforeEach
	public void setUp() {
		testedService = new RPackageService(dao);
	}
	
	private void assertVersions(Collection<String> versions, String... versionsToCheck) {
		for(String version : versionsToCheck) {
			assertTrue(versions.contains(version), version + " was not included");
		}
	}
	
	private void testGenerateVersions(String version) throws Exception {
		doAnswer(new Answer<>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Collection<String> versions = invocation.getArgument(3);
				assertVersions(versions, "1.2.3", "1.2-3", "1-2.3", "1-2-3");
				return null;
			}
		}).when(dao).findByNameAndRepositoryAndDeletedAndVersionIn(
				anyString(), any(), anyBoolean(), anyCollection());
		
		
		testedService.findByNameAndVersionAndRepositoryAndDeleted("test", version, null, false);
	}
	
	@Test
	public void generateAllPackageVersions_dashes() throws Exception {
		testGenerateVersions("1-2-3");
	}
	
	@Test
	public void generateAllPackageVersions_dots() throws Exception {
		testGenerateVersions("1.2.3");
	}
	
	@Test
	public void generateAllPackageVersions_mixed() throws Exception {
		testGenerateVersions("1.2-3");
	}
}
