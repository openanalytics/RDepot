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
package eu.openanalytics.rdepot.base.security.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;

@Component
public class SecurityMediatorImpl implements SecurityMediator {
	
	private final Environment env;
	private final PackageMaintainerService packageMaintainerService;
	private final RepositoryMaintainerService repositoryMaintainerService;		
	private final RoleService roleService;
	protected final UserService userService;
	
	public SecurityMediatorImpl(RepositoryMaintainerService repositoryMaintainerService,
			PackageMaintainerService packageMaintainerService, UserService userService,
			Environment environment, RoleService roleService) {
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.packageMaintainerService = packageMaintainerService;
		this.userService = userService;
		this.env = environment;
		this.roleService = roleService;
	}

	@Override
	public boolean isAuthorizedToAccept(Submission submission, User user) {
		return isAuthorizedToEdit(submission.getPackage(), user);
	}
	
	@Override
	public boolean isAuthorizedToCancel(Submission submission, User user) {
		return submission.getUser().getId() == user.getId();
	}

	@Override
	public boolean canSeeEvent(NewsfeedEvent event, User user) {
		if(event.getRelatedResource().getResourceType().equals(ResourceType.USER)) {
			if(user.getRole().getValue() == Role.VALUE.USER && user.getId() != event.getRelatedResource().getId()) {
				return false;
			}
		}
		return true; //TODO
	}

	@Override
	public boolean isAuthorizedToSee(PackageMaintainer packageMaintainer, User user) {
		if(isAuthorizedToEdit(packageMaintainer, user)) {
			return true;
		}
		return packageMaintainer.getUser().getId() == user.getId();
	}

	@Override
	public boolean isAuthorizedToEdit(PackageMaintainer packageMaintainer, User requester) {
		return isAuthorizedToEdit(packageMaintainer.getRepository(), requester);
	}

	@Override
	public boolean isAuthorizedToEdit(Submission submission, SubmissionDto submissionDto, User requester) {
		if(!submission.getState().equals(submissionDto.getState())) {
			if(submissionDto.getState().equals(SubmissionState.ACCEPTED) 
					&& isAuthorizedToAccept(submission, requester)) {
				return true;
			} else if(submissionDto.getState().equals(SubmissionState.CANCELLED) && 
					isAuthorizedToCancel(submission, requester)) {
				return true;
			} else if(submissionDto.getState().equals(SubmissionState.REJECTED) && 
					isAuthorizedToReject(submission, requester)) {
				return true;
			}
			
			return false;
		} else {
			return isAuthorizedToEdit(submission.getPackage(), requester);
		}
	}

	@Override
	public boolean isAuthorizedToEdit(Package<?, ?> packageBag, User requester) {
		if(packageBag.getUser().getId() == requester.getId()) {
			return true; //TODO: Are we sure about it?
		}
		switch(requester.getRole().getValue()) {
		case Role.VALUE.ADMIN:
			return true;
		case Role.VALUE.REPOSITORYMAINTAINER:
			for(RepositoryMaintainer maintainer : repositoryMaintainerService.findByUserWithoutDeleted(requester)) {
				if(maintainer.getRepository().getId() == packageBag.getRepository().getId()
						&& maintainer.getRepository().getTechnology() 
						== packageBag.getRepository().getTechnology()) {
					return true;
				}
			}
			break;
		case Role.VALUE.PACKAGEMAINTAINER:
			for(PackageMaintainer maintainer : packageMaintainerService.findNonDeletedByUser(requester)) {
				if(maintainer.getRepository().getId() == packageBag.getRepository().getId()
						&& maintainer.getRepository().getTechnology() == packageBag.getRepository().getTechnology()
						&& maintainer.getPackageName().equals(packageBag.getName())) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public boolean isAuthorizedToEdit(Repository<?, ?> repository, User requester) {
		switch(requester.getRole().getValue()) {
		case Role.VALUE.ADMIN:
			return true;
		case Role.VALUE.REPOSITORYMAINTAINER:
			for(RepositoryMaintainer maintainer : repositoryMaintainerService
					.findByUserAndRepository(requester, repository)) {
				if(!maintainer.isDeleted())
					return true;
			}
		}
		return false;
	}

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(String userLogin) {
		User user = userService.findByLogin(userLogin)
				.orElseThrow(() -> new IllegalStateException()); //TODO: Check scenarios when it's thrown
		Collection<SimpleGrantedAuthority> authorities;
		if(env.getProperty("app.authentication").equals("simple")) {
			authorities = new ArrayList<>(0);
		} else {
			authorities = new HashSet<>(0);
		}
		for(int i = 0, v = user.getRole().getValue(); i <= v; i++) {
			authorities.add(new SimpleGrantedAuthority(
					roleService.findByValue(i).orElseThrow(() -> new IllegalStateException())
					.getName()));
		}
		return authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(
			DirContextOperations userData,
			String username) {
		String login = userData.getStringAttribute(env.getProperty("app.ldap.loginfield"));
		User user = userService.findByLogin(login).orElseThrow(() -> new IllegalStateException());
		Collection<SimpleGrantedAuthority> authorities = new HashSet<>(0);
		
		for(int i = 0, v = user.getRole().getValue(); i <= v; i++) {
			authorities.add(
					new SimpleGrantedAuthority(
							roleService.findByValue(i).orElseThrow(() -> new IllegalStateException())
							.getName()));
		}
		return authorities;
	}

	@Override
	public boolean canUpload(String packageName, Repository<?, ?> repository, User user) {
		if(user.getRole().getValue() == Role.VALUE.ADMIN) {
			return true;
		}
		else if(user.getRole().getValue() == Role.VALUE.REPOSITORYMAINTAINER) {
			List<RepositoryMaintainer> maintainers = 
					repositoryMaintainerService.findByUserAndRepository(user, repository);
			for(RepositoryMaintainer maintainer : maintainers) {
				if(!maintainer.isDeleted())
					return true;
			}
		} else if(user.getRole().getValue() == Role.VALUE.PACKAGEMAINTAINER) {
			List<PackageMaintainer> maintainers = packageMaintainerService.findByUser(user);
			
			for(PackageMaintainer maintainer : maintainers) {
				if(maintainer.getPackageName().equals(packageName) 
						&& maintainer.getRepository().getId() == repository.getId() 
						&& maintainer.getRepository().getTechnology() == repository.getTechnology()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isAuthorizedToReject(Submission submission, User user) {
		if(submission.getUser().getId() == user.getId())
			return false;
		return isAuthorizedToEdit(submission.getPackage(), user);
	}
}
