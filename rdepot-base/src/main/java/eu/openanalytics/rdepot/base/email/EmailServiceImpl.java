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
package eu.openanalytics.rdepot.base.email;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Default implementation for the E-Mail service.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String emailFrom;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.enabled:false}") boolean enabled,
            @Value("${spring.mail.username:username}") String emailFrom,
            @Value("${spring.mail.host:example.org}") String hostname) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.emailFrom = emailFrom.contains("@") ? emailFrom : emailFrom + "@" + hostname;
    }

    protected void handleEmailDisabled() {
        log.debug("E-Mail service has been disabled by administrator.");
    }

    @Override
    public void sendAcceptSubmissionEmail(final Submission submission) {
        if (!enabled) {
            handleEmailDisabled();
            return;
        }
        final User to = submission.getPackage().getUser();
        final String submissionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/manager/submissions/{id}")
                .buildAndExpand(submission.getId())
                .toUri()
                .toString();

        final String subject = "RDepot: new submission";
        final String message = "Dear " + to.getName() + ",<br/><br/>"
                + "There's a new submission from " + submission.getSubmitter().getName()
                + ": " + submission.getPackage().getName() + " "
                + submission.getPackage().getVersion()
                + " for " + submission.getRepository().getName()
                + " (" + submission.getPackage().getRepository().getPublicationUri() + ").<br/>"
                + "To <strong>accept</strong> the submission directly, please use <a href='"
                + submissionUrl + "/accept'>this</a> link.<br/>"
                + "To get an <strong>overview</strong> of the submission in more detail, please use <a href='"
                + submissionUrl + "'>this</a> link.<br/>"
                + "To <strong>cancel</strong> the submission directly, please use <a href='" + submissionUrl
                + "/cancel'>this</a> link.<br><br>"
                + "Thank you for your cooperation.<br/><br/>"
                + "Sincerely yours,<br/>"
                + "The RDepot team";

        log.debug("Sending accept submission e-mail");
        sendEmail(emailFrom, to.getEmail(), subject, message);
    }

    /**
     * Sends e-mail to recipient.
     */
    protected void sendEmail(final String emailFrom, final String emailTo, final String subject, final String message) {
        final SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(emailFrom);
        mail.setTo(emailTo);
        mail.setSubject(subject);
        mail.setText(message);

        try {
            mailSender.send(mail);
        } catch (MailException e) {
            log.error("Could not send e-mail!", e);
        }
    }

    @Override
    public void sendCancelledSubmissionEmail(final Submission submission) {
        if (!enabled) {
            handleEmailDisabled();
            return;
        }
        final User to = submission.getPackage().getUser();
        final String subject = "RDepot: canceled submission";
        final String message = "Dear " + to.getName() + ",<br/><br/>"
                + "The submission from " + submission.getSubmitter().getName()
                + ": " + submission.getPackage().getName()
                + " " + submission.getPackage().getVersion()
                + " for " + submission.getPackage().getRepository().getName()
                + " (" + submission.getPackage().getRepository().getPublicationUri()
                + ") has been canceled by the submitter.<br/>"
                + "The previous email can now be neglected and no further action is required.<br/><br/>"
                + "Sincerely yours,<br/>"
                + "The RDepot team";
        sendEmail(emailFrom, to.getEmail(), subject, message);
    }
}
