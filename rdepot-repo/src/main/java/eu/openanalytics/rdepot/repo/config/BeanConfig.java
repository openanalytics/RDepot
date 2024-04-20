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
package eu.openanalytics.rdepot.repo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.openanalytics.rdepot.repo.r.storage.CranFileSystemStorageService;
import eu.openanalytics.rdepot.repo.storage.InitializableStorageService;

@Configuration
public class BeanConfig {
	
	@Autowired
	CranFileSystemStorageService cranFileSystemStorageService;
	
	@Bean
	@Primary
	InitializableStorageService initializableStorageService() {
		return new InitializableStorageService() {
			
			@Override
			public void init() {
				cranFileSystemStorageService.init();
			}
		};
	}
}