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
package eu.openanalytics.rdepot.test.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.openanalytics.rdepot.repository.RepositoryMaintainerEventRepository;
import eu.openanalytics.rdepot.repository.EventRepository;
import eu.openanalytics.rdepot.repository.PackageEventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerEventRepository;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.repository.RepositoryEventRepository;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.repository.SubmissionEventRepository;
import eu.openanalytics.rdepot.repository.SubmissionRepository;
import eu.openanalytics.rdepot.repository.UserEventRepository;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.EmailService;
import eu.openanalytics.rdepot.storage.BaseStorage;
import eu.openanalytics.rdepot.storage.PackageStorageLocalImpl;
import eu.openanalytics.rdepot.storage.RepositoryStorageLocalImpl;

@Configuration
public class MockRepositoryBeansConfig {
	
	@Bean
	public EventRepository eventRepository() {
	    return mock(EventRepository.class);
	}

	@Bean
	public PackageEventRepository packageEventRepository() {
	    return mock(PackageEventRepository.class);
	}

	@Bean
	public PackageMaintainerEventRepository packageMaintainerEventRepository() {
	    return mock(PackageMaintainerEventRepository.class);
	}

	@Bean
	public PackageMaintainerRepository packageMaintainerRepository() {
	    return mock(PackageMaintainerRepository.class);
	}

	@Bean
	public PackageRepository packageRepository() {
	    return mock(PackageRepository.class);
	}

	@Bean
	public RepositoryEventRepository repositoryEventRepository() {
	    return mock(RepositoryEventRepository.class);
	}

	@Bean
	public RepositoryMaintainerEventRepository repositoryMaintainerEventRepository() {
	    return mock(RepositoryMaintainerEventRepository.class);
	}

	@Bean
	public RepositoryMaintainerRepository repositoryMaintainerRepository() {
	    return mock(RepositoryMaintainerRepository.class);
	}

	@Bean
	public RepositoryRepository repositoryRepository() {
	    return mock(RepositoryRepository.class);
	}

	@Bean
	public RoleRepository roleRepository() {
	    return mock(RoleRepository.class);
	}

	@Bean
	public SubmissionEventRepository submissionEventRepository() {
	    return mock(SubmissionEventRepository.class);
	}

	@Bean
	public SubmissionRepository submissionRepository() {
	    return mock(SubmissionRepository.class);
	}

	@Bean
	public UserEventRepository userEventRepository() {
	    return mock(UserEventRepository.class);
	}

	@Bean
	public UserRepository userRepository() {
	    return mock(UserRepository.class);
	}
	
	@Bean
	public MessageSource messageSource() {
		return mock(MessageSource.class);
	}
	
	@Bean
	public RepositoryStorageLocalImpl repositoryStorage() {
		return mock(RepositoryStorageLocalImpl.class);
	}

	@Bean
	public PackageStorageLocalImpl packageStorage() {
		return mock(PackageStorageLocalImpl.class);
	}
	
	@Bean
	public BaseStorage baseStorage() {
		return mock(BaseStorage.class);
	}
	
	@Bean
	public EmailService emailService() {
		return mock(EmailService.class);
	}
}
