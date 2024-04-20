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
package eu.openanalytics.rdepot.test.unit.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.mediator.deletion.RSubmissionDeleter;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;

public class RSubmissionDeleterUnitTest extends UnitTest {
	@Mock
	NewsfeedEventService newsfeedEventService;
	
	@Mock
	SubmissionService submissionService;
	
	@Mock
	RPackageDeleter packageDeleter;
	
	@InjectMocks
	RSubmissionDeleter deleter;
	
	User user;
	RRepository repository;
	RPackage packageBag;
	Submission submission;
	
	@BeforeEach
	public void setUpResources() {
		user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		packageBag = RPackageTestFixture.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		submission = packageBag.getSubmission();
	}
	
	
	@Test
	public void delete() throws Exception {
		doNothing().when(packageDeleter).deleteForSubmission(submission);
		
		deleter.delete(submission);
		
		verify(packageDeleter).deleteForSubmission(submission);
	}
	
	@Test
	public void delete_throwsNPE_whenTryingToDeleteNullEvent() throws Exception {
		doThrow(new NullPointerException()).when(packageDeleter).deleteForSubmission(null);
		
		assertThrows(NullPointerException.class, () -> deleter.delete(null));
	}
	
	@Test
	public void delete_throwsException_whenPackageCannotBeDeleted() throws Exception {
		doThrow(new DeleteEntityException()).when(packageDeleter).deleteForSubmission(submission);
	
		assertThrows(DeleteEntityException.class, () -> deleter.delete(submission));
	}
}
