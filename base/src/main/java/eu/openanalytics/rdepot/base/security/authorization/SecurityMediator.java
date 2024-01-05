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
package eu.openanalytics.rdepot.base.security.authorization;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;

/**
 * Determines what actions user is allowed to perform.
 */
public interface SecurityMediator extends LdapAuthoritiesPopulator {
	
	/**
	 * Used to determine if user's submission can automatically be accepted
	 * based on associated maintainers and roles.
	 * @param packageName
	 * @param repository
	 * @param user
	 * @return
	 */
	default boolean canUpload(String packageName, Repository<?, ?> repository, User user) { 
		return false; 
	}
	
	boolean isAuthorizedToAccept(Submission submission, User user);
	boolean isAuthorizedToCancel(Submission submission, User user);
	boolean isAuthorizedToReject(Submission submission, User user);
	boolean canSeeEvent(NewsfeedEvent event, User user);
	boolean isAuthorizedToSee(PackageMaintainer packageMaintainer, User user);
	boolean isAuthorizedToEdit(PackageMaintainer packageMaintainer, User requester);
	boolean isAuthorizedToEdit(Submission submission, SubmissionDto submissionDto, User requester);
	boolean isAuthorizedToEdit(Package<?,?> packageBag, User requester);
	boolean isAuthorizedToEdit(Repository<?,?> repository, User requester);
	Collection<? extends GrantedAuthority> getGrantedAuthorities(String userLogin);
}
