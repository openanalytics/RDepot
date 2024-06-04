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
package eu.openanalytics.rdepot.repo.test.transaction;

import eu.openanalytics.rdepot.repo.python.transaction.backup.PythonRepositoryBackup;
import eu.openanalytics.rdepot.repo.r.storage.implementations.CranFileSystemStorageService;
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.repository.Repository;
import eu.openanalytics.rdepot.repo.storage.InitializableStorageService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

@EnableAutoConfiguration
@ComponentScan(basePackages = "eu.openanalytics.rdepot.repo")
@TestConfiguration
@Profile({"test"})
public class TestConfig {

    @Autowired
    CranFileSystemStorageService cranFileSystemStorageService;

    public static final ConcurrentMap<String, Transaction> ACTIVE_BY_ID = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Repository, Transaction> ACTIVE_BY_REPO = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Transaction, CranRepositoryBackup> CRAN_BACKUPS = new ConcurrentHashMap<>();

    @Bean
    ConcurrentMap<String, Transaction> activeTransactionsById() {
        return ACTIVE_BY_ID;
    }

    @Bean
    ConcurrentMap<Repository, Transaction> activeTransactionsByRepo() {
        return ACTIVE_BY_REPO;
    }

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

    @Bean
    ConcurrentMap<Transaction, CranRepositoryBackup> cranBackups() {
        return CRAN_BACKUPS;
    }

    @Bean
    ConcurrentMap<Transaction, PythonRepositoryBackup> pythonBackups() {
        return new ConcurrentHashMap<>();
    }
}
