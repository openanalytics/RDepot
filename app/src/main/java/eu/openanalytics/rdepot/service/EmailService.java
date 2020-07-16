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
package eu.openanalytics.rdepot.service;

import java.util.Objects;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import eu.openanalytics.rdepot.exception.SendEmailException;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;

@Service
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class EmailService {
	@Resource
	private Environment env;
	
	private static final String PROPERTY_NAME_EMAIL_PASSWORD = "email.password";
	private static final String PROPERTY_NAME_EMAIL_USERNAME = "email.username";
	private static final String PROPERTY_NAME_EMAIL_FROM = "email.from";
	private static final String PROPERTY_NAME_EMAIL_SMTP_HOST = "email.smtp.host";
	private static final String PROPERTY_NAME_EMAIL_SMTP_PORT = "email.smtp.port";
	private static final String PROPERTY_NAME_EMAIL_SMTP_AUTH = "email.smtp.auth";
	private static final String PROPERTY_NAME_EMAIL_SMTP_STARTTLS = "email.smtp.starttls";
	
	public void sendActivateSubmissionEmail(Submission submission, String submissionUrl) 
			throws SendEmailException {
		User to = submission.getPackage().getUser();
		String subject = "R Repository Manager: new submission!";
		String message = "Dear " + to.getName() + ",<br><br>";
		message += "There's a new submission from " + submission.getUser().getName() + 
				": " + submission.getPackage().getName() + 
				" " + submission.getPackage().getVersion() + 
				" for " + submission.getPackage().getRepository().getName() + 
				" (" + submission.getPackage().getRepository().getPublicationUri() + ").<br>";
		message += "To <strong>accept</strong> the submission directly, please use <a href='" + submissionUrl + "/accept'>this</a> link.<br>";
		message += "To get an <strong>overview</strong> of the submission in more detail, please use <a href='" + submissionUrl + "'>this</a> link.<br>";
		message += "To <strong>cancel</strong> the submission directly, please use <a href='" + submissionUrl + "/cancel'>this</a> link.<br><br>";
		message += "Thank you for your cooperation.<br><br>";
		message += "Sincerely yours,<br>";
		message += "the R Repository Manager team";
		sendEmail(to, subject, message);
	}
	
	public void sendCanceledSubmissionEmail(Submission submission) throws SendEmailException {
		User to = submission.getPackage().getUser();
		String subject = "R Repository Manager: canceled submission!";
		String message = "Dear " + to.getName() + ",<br><br>";
		message += "The submission from " + submission.getUser().getName() + 
				": " + submission.getPackage().getName() + 
				" " + submission.getPackage().getVersion() + 
				" for " + submission.getPackage().getRepository().getName() + 
				" (" + submission.getPackage().getRepository().getPublicationUri() + 
				") has been canceled by the submitter.<br>";
		message += "The previous email can now be neglected and no further action is required.<br><br>";
		message += "Sincerely yours,<br>";
		message += "the R Repository Manager team";
		sendEmail(to, subject, message);
	}

	public void sendEmail(User user, String subject, String message_) throws SendEmailException {
		final String username = env.getRequiredProperty(PROPERTY_NAME_EMAIL_USERNAME);
		final String password = env.getRequiredProperty(PROPERTY_NAME_EMAIL_PASSWORD);
		String from = env.getRequiredProperty(PROPERTY_NAME_EMAIL_FROM);
		String host = env.getRequiredProperty(PROPERTY_NAME_EMAIL_SMTP_HOST);
		String port = env.getRequiredProperty(PROPERTY_NAME_EMAIL_SMTP_PORT);
		String auth = env.getRequiredProperty(PROPERTY_NAME_EMAIL_SMTP_AUTH);
		String starttls = env.getRequiredProperty(PROPERTY_NAME_EMAIL_SMTP_STARTTLS);
		
		String to = user.getEmail();

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.port", port);
		properties.setProperty("mail.smtp.host", host);
		properties.setProperty("mail.smtp.auth", auth);
		properties.setProperty("mail.smtp.starttls.enable", starttls);
		
		Session session = null;
		if(Objects.equals(auth, "true")) {
			session = Session.getInstance(properties,
					  new javax.mail.Authenticator() {
					      protected PasswordAuthentication getPasswordAuthentication() {
					    	  return new PasswordAuthentication(username, password);
					      }
					  });
		}
		else {
			session = Session.getDefaultInstance(properties);
		}
		
		try	{
		
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setContent(message_, "text/html");
			Transport.send(message);
		}
		catch (MessagingException mex) {
			throw new SendEmailException(mex.getMessage());
		}
		
	}	
	
}
