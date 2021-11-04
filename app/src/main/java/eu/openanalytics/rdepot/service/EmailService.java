/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	Logger logger = LoggerFactory.getLogger(EmailService.class);
	
	public static final String PROPERTY_NAME_EMAIL_ENABLED = "email.enabled";
	public static final String PROPERTY_NAME_EMAIL_PASSWORD = "email.password";
	public static final String PROPERTY_NAME_EMAIL_USERNAME = "email.username";
	public static final String PROPERTY_NAME_EMAIL_FROM = "email.from";
	public static final String PROPERTY_NAME_EMAIL_SMTP_HOST = "email.smtp.host";
	public static final String PROPERTY_NAME_EMAIL_SMTP_PORT = "email.smtp.port";
	public static final String PROPERTY_NAME_EMAIL_SMTP_AUTH = "email.smtp.auth";
	public static final String PROPERTY_NAME_EMAIL_SMTP_STARTTLS = "email.smtp.starttls";
	
	private final boolean enabled;
	private final String username;
	private final String password;
	private final String from;
	private final String host;
	private final String port;
	private final String auth;
	private final String starttls;
	
	public EmailService(Environment env) {
	  enabled = Objects.equals(env.getProperty(PROPERTY_NAME_EMAIL_ENABLED, "false"), "true");
      username = env.getProperty(PROPERTY_NAME_EMAIL_USERNAME, "");
      password = env.getProperty(PROPERTY_NAME_EMAIL_PASSWORD, "");
      from = env.getProperty(PROPERTY_NAME_EMAIL_FROM, "root@localhost");
      host = env.getProperty(PROPERTY_NAME_EMAIL_SMTP_HOST, "localhost");
      port = env.getProperty(PROPERTY_NAME_EMAIL_SMTP_PORT, "22");
      auth = env.getProperty(PROPERTY_NAME_EMAIL_SMTP_AUTH, "false");
      starttls = env.getProperty(PROPERTY_NAME_EMAIL_SMTP_STARTTLS, "false");
	}
	
	public void sendActivateSubmissionEmail(Submission submission, String submissionUrl) 
			throws SendEmailException {
        if(!enabled) {
            logger.debug("Not sending an activate submission email, because it is disabled.");
            return;
        }
		User to = submission.getPackage().getUser();
		String subject = "RDepot: new submission";
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
		message += "The RDepot team";
		sendEmail(to, subject, message);
	}
	
	public void sendCanceledSubmissionEmail(Submission submission) throws SendEmailException {
  	    if(!enabled) {
            logger.debug("Not sending a canceled submission email, because it is disabled.");
            return;
        }
	    User to = submission.getPackage().getUser();
		String subject = "RDepot: canceled submission";
		String message = "Dear " + to.getName() + ",<br><br>";
		message += "The submission from " + submission.getUser().getName() + 
				": " + submission.getPackage().getName() + 
				" " + submission.getPackage().getVersion() + 
				" for " + submission.getPackage().getRepository().getName() + 
				" (" + submission.getPackage().getRepository().getPublicationUri() + 
				") has been canceled by the submitter.<br>";
		message += "The previous email can now be neglected and no further action is required.<br><br>";
		message += "Sincerely yours,<br>";
		message += "The RDepot team";
		sendEmail(to, subject, message);
	}

	public void sendEmail(User user, String subject, String message) throws SendEmailException {
  	    if(!enabled) {
            logger.debug("Not sending an email, because it is disabled.");
            return;
        }
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
		
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress(from));
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mimeMessage.setSubject(subject);
			mimeMessage.setContent(message, "text/html");
			Transport.send(mimeMessage);
		}
		catch (MessagingException mex) {
			throw new SendEmailException(mex.getMessage());
		}
		
	}	
	
}
