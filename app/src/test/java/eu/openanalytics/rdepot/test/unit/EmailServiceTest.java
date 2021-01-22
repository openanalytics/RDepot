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
package eu.openanalytics.rdepot.test.unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import eu.openanalytics.rdepot.exception.SendEmailException;
import eu.openanalytics.rdepot.service.EmailService;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

public class EmailServiceTest {

	@Test
	public void sendNoEmails() {
	    MockEnvironment env = new MockEnvironment();
	    EmailService emailService = new EmailService(env);
	    
	    try {
	      emailService.sendEmail(null, null, null);
	      emailService.sendActivateSubmissionEmail(null, null);
	      emailService.sendCanceledSubmissionEmail(null);
	    } catch (Exception e) {
	      fail("EmailService tried to send a mail when it shouldn't have");
	    }
	    
	    env.setProperty(EmailService.PROPERTY_NAME_EMAIL_ENABLED, "");
	    emailService = new EmailService(env);
	    
	    try {
          emailService.sendEmail(null, null, null);
          emailService.sendActivateSubmissionEmail(null, null);
          emailService.sendCanceledSubmissionEmail(null);
        } catch (Exception e) {
          fail("EmailService tried to send a mail when it shouldn't have");
        }
	    
	    env.setProperty(EmailService.PROPERTY_NAME_EMAIL_ENABLED, "false");
        emailService = new EmailService(env);
        
        try {
          emailService.sendEmail(null, null, null);
          emailService.sendActivateSubmissionEmail(null, null);
          emailService.sendCanceledSubmissionEmail(null);
        } catch (Exception e) {
          fail("EmailService tried to send a mail when it shouldn't have");
        }
        
        env.setProperty(EmailService.PROPERTY_NAME_EMAIL_ENABLED, "aklsfh");
        emailService = new EmailService(env);
        
        try {
          emailService.sendEmail(null, null, null);
          emailService.sendActivateSubmissionEmail(null, null);
          emailService.sendCanceledSubmissionEmail(null);
        } catch (Exception e) {
          fail("EmailService tried to send a mail when it shouldn't have");
        }
	}
	
	@Test
    public void sendEmailsButThrowException() {
      MockEnvironment env = new MockEnvironment();
      env.setProperty(EmailService.PROPERTY_NAME_EMAIL_ENABLED, "true");
      EmailService emailService = new EmailService(env);
      
      assertThrows(SendEmailException.class, () -> {
        emailService.sendEmail(UserTestFixture.GET_FIXTURE_USER(), "test", "test");
      });
      
      assertThrows(SendEmailException.class, () -> {
        eu.openanalytics.rdepot.model.Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(
            RepositoryTestFixture.GET_FIXTURE_REPOSITORY(), UserTestFixture.GET_FIXTURE_USER());
        emailService.sendActivateSubmissionEmail(testPackage.getSubmission(), "http://test.com");
      });
      
      assertThrows(SendEmailException.class, () -> {
        eu.openanalytics.rdepot.model.Package testPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(
            RepositoryTestFixture.GET_FIXTURE_REPOSITORY(), UserTestFixture.GET_FIXTURE_USER());
        emailService.sendCanceledSubmissionEmail(testPackage.getSubmission());
      });
    }
}
