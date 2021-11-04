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
package eu.openanalytics.rdepot.initializer;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.model.Mirror;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.properties.RepositoriesProps;
import eu.openanalytics.rdepot.runnable.SynchronizeMirrorTask;
import eu.openanalytics.rdepot.service.CranMirrorService;
import eu.openanalytics.rdepot.service.RepositoryService;

@Component
public class RepositoryDataInitializer {
	
	@Resource
	private Environment environment;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private RepositoriesProps repositoriesProps;
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private CranMirrorService mirrorService;
	
	Logger logger = LoggerFactory.getLogger(RepositoryDataInitializer.class);
	
	@EventListener(ApplicationReadyEvent.class)
	public void createRepositoriesFromConfig() {
		List<Repository> repositories = repositoriesProps.getRepositories();
    	logger.debug("Loading declared repositories...");
    	if(repositories == null) {
    		logger.info("There are no declared repositories!");
    		return;
    	}
    	
    	repositoryService.createRepositoriesFromConfig(repositories);
    	scheduleMirroring(repositories);
	}
	
	public void scheduleMirroring(List<Repository> repositories) {
		for(Repository declaredRepository : repositories) {
			
			Repository repository = repositoryService.findByName(declaredRepository.getName());
			if(repository != null) {
				Set<Mirror> mirrors = declaredRepository.getMirrors();
				
				if(mirrors != null) {
					for(Mirror mirror : mirrors) {
						
						if(!mirror.getSyncInterval().isEmpty()) {
							logger.info("Scheduling mirroring for " + repository.getName() 
							+ " with sync interval: " + mirror.getSyncInterval());
							
							CronTrigger cronTrigger = new CronTrigger(mirror.getSyncInterval());
							taskScheduler.schedule(new SynchronizeMirrorTask(mirrorService, mirror, repository), cronTrigger);
						}
					}
				}
			}
		}
	}
	
}
