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
package eu.openanalytics.rdepot.base.initializer;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CommonRepositoryDataInitializer {

	private final List<IRepositoryDataInitializer> repositoryDataInitializers;
	
	@Value("${declarative}")
	private String declarative;
	
	public CommonRepositoryDataInitializer(List<IRepositoryDataInitializer> repositoryDataInitializers) {
		this.repositoryDataInitializers = repositoryDataInitializers;
	}
	
	@Transactional
	@EventListener(ApplicationReadyEvent.class)
	public void createRepositoriesFromConfig() {
		createRepositoriesFromConfig(Boolean.valueOf(declarative));
	}
	
//	@Override
	public void createRepositoriesFromConfig(boolean declarative) {
		repositoryDataInitializers.forEach(i -> i.createRepositoriesFromConfig(declarative));
	}

}
