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
package eu.openanalytics.rdepot.repo.storage;

import java.io.File;
import java.util.List;
import java.util.Map;

import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTransactionException;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;

public interface StorageService {

    void init();

    List<File> getRecentPackagesFromRepository(String repository);
    
    Map<String, List<File>> getArchiveFromRepository(String repository);
    
    public void processRequest(SynchronizeRepositoryRequestBody requestBody) throws ProcessRequestException;
    
    public Map<String, File> getPackagesFiles(String repository, boolean archive);
    
    public String getRepositoryVersion(String repository) throws GetRepositoryVersionException;
    
	public String initTransaction(String repository, String repositoryVersion) throws InitTransactionException;
	
	public void processLastRequest() throws ProcessRequestException;
}
