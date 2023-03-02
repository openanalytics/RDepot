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
package eu.openanalytics.rdepot.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuration class for e-mail notifications.
 * It can be disabled with in application.yaml file.
 */
@Configuration
public class EmailConfiguration {
	
	@Value("${spring.mail.host:localhost}")
    private String mailServerHost;

    @Value("${spring.mail.port:587}")
    private int mailServerPort;

    @Value("${spring.mail.username:admin}")
    private String mailServerUsername;

    @Value("${spring.mail.password:secretpassword}")
    private String mailServerPassword;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean mailServerAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean mailServerStartTls;
    
    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    	PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
    	pspc.setIgnoreResourceNotFound(true);
    	
    	return pspc;
    }
    
    @Bean
    JavaMailSender getJavaMailSender() {    
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(mailServerHost);
        mailSender.setPort(mailServerPort);
        mailSender.setUsername(mailServerUsername);
        mailSender.setPassword(mailServerPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailServerAuth);
        props.put("mail.smtp.starttls.enable", mailServerStartTls);
//        props.put("mail.debug", "true");
        
        return mailSender;
    }
}
