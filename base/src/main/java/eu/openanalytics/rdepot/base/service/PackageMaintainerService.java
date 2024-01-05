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
package eu.openanalytics.rdepot.base.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.base.daos.PackageMaintainerDao;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;

@org.springframework.stereotype.Service
public class PackageMaintainerService extends Service<PackageMaintainer> {
	  
	private final PackageMaintainerDao dao;
	
    public PackageMaintainerService(PackageMaintainerDao dao) {
        super(dao);
        this.dao = dao;
    }
  
    @Transactional
    public Optional<PackageMaintainer> findByPackageAndRepositoryAndNonDeleted(
    		String packageName, Repository<?,?> repository) {
    	Optional<PackageMaintainer> foundMaintainer = dao.findByPackageNameAndRepositoryIdAndDeleted(packageName, repository.getId(), false);

    	return foundMaintainer;
    }
    
    public Optional<PackageMaintainer> findByPackageAndRepositoryAndDeleted(
    		String packageName, Repository<?,?> repository) {
    	Optional<PackageMaintainer> foundMaintainer = dao.findByPackageNameAndRepositoryIdAndDeleted(packageName, repository.getId(), true);  
    	return foundMaintainer;
    }
  
    public List<PackageMaintainer> findByUser(User user) {
    	return dao.findByUser(user);
    }
  
    public List<PackageMaintainer> findByRepository(Repository<?,?> repository) {
    	return dao.findByRepository(repository);
    }
    
    public List<PackageMaintainer> findByRepositoryNonDeleted(Repository<?,?> repository) {
    	return dao.findByRepositoryAndDeleted(repository, false);
    }
    
    public List<PackageMaintainer> findByRequester(User requester) {
		List<PackageMaintainer> result = new ArrayList<>();
		switch(requester.getRole().getName()) {
			case "admin":
				result.addAll(findByDeleted(false));
				break;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers()) {
					if (!repositoryMaintainer.isDeleted())
						result.addAll(findByRepositoryNonDeleted(repositoryMaintainer.getRepository()));
				}
				break;
		}
		
		Comparator<PackageMaintainer> sortPackageMaintainersById
		 = Comparator.comparing(PackageMaintainer::getId);
	
		return result.stream().sorted(sortPackageMaintainersById).collect(Collectors.toList());
	}
    
    public List<PackageMaintainer> findByRequesterAndNotDeleted(User requester) {
    	List<PackageMaintainer> result = new ArrayList<>();
    	
		switch(requester.getRole().getName()) {
			case "admin":
				result.addAll(findByDeleted(false));
				break;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers()) {
					if (!repositoryMaintainer.isDeleted()) {
						result.addAll(findByRepositoryNonDeleted(repositoryMaintainer.getRepository()));
					}
				}
				break;
		}
		Comparator<PackageMaintainer> sortPackageMaintainersById
		 = Comparator.comparing(PackageMaintainer::getId);
	
		return result.stream().sorted(sortPackageMaintainersById).collect(Collectors.toList());
	}

	public List<PackageMaintainer> findNonDeletedByUser(User user) {
		return dao.findByUserAndDeleted(user, false);
	}
}