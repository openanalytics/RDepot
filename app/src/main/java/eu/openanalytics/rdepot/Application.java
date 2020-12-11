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
/**
 * 
 */
package eu.openanalytics.rdepot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;

import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.service.RepositoryService;

/**
 * @author jonas
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class Application extends SpringBootServletInitializer {	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	protected WebApplicationContext run(SpringApplication application) {
		// TODO Auto-generated method stub
		return super.run(application);
	}
//	
//	@Bean
//    InitializingBean createRepositoriesFromConfig() {
//        return () -> {        
//        	List<Repository> repositories = new ArrayList<>();
//        	for(int i=0;;i++) {
//        		String repositoryName = environment.getProperty(String.format("repositories[%d].name", i)); 
//        		if(repositoryName == null) break;
//        		else {
//        			Repository repository = new Repository(
//        					0,      
//        					environment.getProperty(String.format("repositories[%d].publication-uri", i)),
//        					environment.getProperty(String.format("repositories[%d].name", i)),
//        					environment.getProperty(String.format("repositories[%d].server-address", i)),
//        					false,
//        					false,
//        					new HashSet<>(),
//        					new HashSet<>(),
//        					new HashSet<>(),
//        					new HashSet<>()
//        					);   
//        			repositories.add(repository);
//        		}
//        	}
//        	repositoryService.createRepositoriesFromConfig(repositories);
//        };
//	}	
}
