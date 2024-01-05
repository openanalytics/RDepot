/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.synchronization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.exceptions.SendSynchronizeRequestException;
import eu.openanalytics.rdepot.r.synchronization.pojos.Upload;
import eu.openanalytics.rdepot.r.synchronization.pojos.VersionedRepository;

@Component
public class RRepositorySynchronizer implements RepositorySynchronizer<RRepository> {
	
	private static final Logger logger = LoggerFactory.getLogger(RRepositorySynchronizer.class);
	private final RStorage storage;
	private final RPackageService packageService;
	
	@Value("${localStorage.maxRequestSize}")
	private Integer maxRequestSize;
	
	@Value("${declarative}")
	private String declarative;
	
	public RRepositorySynchronizer(RStorage storage, RPackageService packageService) {
		this.storage = storage;
		this.packageService = packageService;
	}
	
	@Override
	public void storeRepositoryOnRemoteServer(
			RRepository repository, String dateStamp) throws SynchronizeRepositoryException {
		LinkedHashSet<RPackage> packages = new LinkedHashSet<>(
				packageService.findActiveByRepository(repository));
		List<RPackage> allPackages = new LinkedList<>(packages);
		
		Set<RPackage> latestPackageSet = packageService.filterLatest(packages);
		packages.removeAll(latestPackageSet);
		
		List<RPackage> archivePackages = new LinkedList<>(packages);
		List<RPackage> latestPackages = new LinkedList<>(latestPackageSet);
		
		storeRepositoryOnRemoteServer(repository, dateStamp, allPackages, 
				archivePackages, latestPackages);
	}
	
	private synchronized void storeRepositoryOnRemoteServer(RRepository repository, 
			String dateStamp, List<RPackage> packages, 
			List<RPackage> archivePackages, List<RPackage> latestPackages) 
					throws SynchronizeRepositoryException {
		try {
			synchronizeRepository(
					storage.organizePackagesInStorage(dateStamp, packages, 
							latestPackages, archivePackages, repository),
					repository);
		} catch(OrganizePackagesException e) {
			logger.error(e.getMessage(), e);
			throw new SynchronizeRepositoryException();
		}
		
	}
	
	private void synchronizeRepository(
			PopulatedRepositoryContent populatedRepositoryContent,
			RRepository repository) throws SynchronizeRepositoryException {
		
		String[] serverAddressComponents = repository.getServerAddress().split("/");
		if(serverAddressComponents.length < 4) {
			throw new IllegalStateException("Incorrect server address: " + repository.getServerAddress());
		}
		
		String serverAndPort = serverAddressComponents[0] + "//" + serverAddressComponents[2];
		
		String repositoryDirectory = "";
		for(int i = 3; i < serverAddressComponents.length; i++) {
			repositoryDirectory += serverAddressComponents[i];
			repositoryDirectory += "/";
		}		
		
		Gson gson = new Gson();
		RestTemplate rest = new RestTemplate();
		
		ResponseEntity<String> response = rest.getForEntity(serverAndPort + "/" + repositoryDirectory, String.class);

		final VersionedRepository remoteLatestPackages = gson.fromJson(response.getBody(), VersionedRepository.class);
		response = rest.getForEntity(serverAndPort + "/" + repositoryDirectory + "/archive/", String.class);
		final VersionedRepository remoteArchivePackages = gson.fromJson(response.getBody(), VersionedRepository.class);
		
		String versionBefore = remoteLatestPackages.getRepositoryVersion();

		try {
			SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
					populatedRepositoryContent, remoteLatestPackages, remoteArchivePackages, repository, versionBefore);			
			
			sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory);
			storage.cleanUpAfterSynchronization(populatedRepositoryContent);
		} catch(SendSynchronizeRequestException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SynchronizeRepositoryException();
		} catch (CleanUpAfterSynchronizationException e) {
			// TODO Auto-generated catch block
			// TODO We should somehow inform the administrator that it failed and/or revert it
			e.printStackTrace();
		} 		
	}
	
	private void sendSynchronizeRequest(SynchronizeRepositoryRequestBody request,
			String serverAddress, String repositoryDirectory) throws SendSynchronizeRequestException {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		List<MultiValueMap<String, Object>> chunks = request.toChunks(maxRequestSize);
		
		logger.debug("Sending chunk to repo...");
		
		try {
			String id = "";
			for(MultiValueMap<String, Object> chunk : chunks) {
				chunk.add("id", id);
				
				HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(chunk);
				ResponseEntity<RepoResponse> httpResponse = 
						rest.postForEntity(serverAddress + "/" + repositoryDirectory, entity, RepoResponse.class);
				
				if(!httpResponse.getStatusCode().is2xxSuccessful() || 
						!Objects.equals(httpResponse.getBody().getMessage(), "OK")) {
					throw new SendSynchronizeRequestException();
				}
				
				id = httpResponse.getBody().getId();
			}
		} catch(RestClientException e) {
			logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			throw new SendSynchronizeRequestException();
		}
	}
}
