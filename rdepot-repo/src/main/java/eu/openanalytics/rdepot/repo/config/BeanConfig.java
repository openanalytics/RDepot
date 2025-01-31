/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import eu.openanalytics.rdepot.repo.python.transaction.backup.PythonRepositoryBackup;
import eu.openanalytics.rdepot.repo.r.storage.implementations.CranFileSystemStorageService;
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.repository.Repository;
import eu.openanalytics.rdepot.repo.storage.InitializableStorageService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"production"})
public class BeanConfig {

    @Bean
    @Primary
    InitializableStorageService initializableStorageService(CranFileSystemStorageService cranFileSystemStorageService) {
        return new InitializableStorageService() {

            @Override
            public void init() {
                cranFileSystemStorageService.init();
            }
        };
    }

    @Bean
    ConcurrentMap<String, Transaction> activeTransactionsById() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    ConcurrentMap<Repository, Transaction> activeTransactionsByRepo() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    ConcurrentMap<Transaction, PythonRepositoryBackup> pythonBackups() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    ConcurrentMap<Transaction, CranRepositoryBackup> cranBackups() {
        return new ConcurrentHashMap<>();
    }
}
