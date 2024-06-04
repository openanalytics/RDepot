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
package eu.openanalytics.rdepot.base.security.authorization;

import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.*;
import eu.openanalytics.rdepot.base.entities.Package;
import jakarta.json.JsonPatch;
import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

/**
 * Determines what actions user is allowed to perform.
 */
public interface SecurityMediator {

    /**
     * Used to determine if user's submission can automatically be accepted
     * based on associated maintainers and roles.
     */
    default boolean canUpload(String packageName, Repository repository, User user) {
        return false;
    }

    /**
     * Determines if user is allowed to accept a submission.
     */
    default boolean isAuthorizedToAccept(Submission submission, User user) {
        return false;
    }

    /**
     * Determines if user is allowed to cancel a submission.
     */
    default boolean isAuthorizedToCancel(Submission submission, User user) {
        return false;
    }

    /**
     * Determines if user is allowed to reject a submission.
     */
    default boolean isAuthorizedToReject(Submission submission, User user) {
        return false;
    }

    /**
     * Determines if user is able to see an event.
     */
    default boolean canSeeEvent(NewsfeedEvent event, User user) {
        return false;
    }

    /**
     * Determines if user is allowed to see a package maintainer.
     */
    default boolean isAuthorizedToSee(PackageMaintainer packageMaintainer, User user) {
        return false;
    }

    /**
     * Determines if user is allowed to edit a package maintainer.
     */
    default boolean isAuthorizedToEdit(PackageMaintainer packageMaintainer, User requester) {
        return false;
    }

    /**
     * Determines if user is allowed to make changes in a submission
     * based on the differences between current state and a given {@link SubmissionDto dto}.
     */
    default boolean isAuthorizedToEdit(Submission submission, SubmissionDto submissionDto, User requester) {
        return false;
    }

    /**
     * Determines if user is allowed to edit a package.
     */
    default boolean isAuthorizedToEdit(Package packageBag, User requester) {
        return false;
    }

    /**
     * Determines if user is allowed to perform this specific patch on a user.
     */
    default boolean isAuthorizedToEditWithPatch(JsonPatch patch, User user, User requester) {
        return false;
    }

    /**
     * Determines if user is allowed to edit a repository.
     */
    default boolean isAuthorizedToEdit(Repository repository, User requester) {
        return false;
    }

    /**
     * Returns Granted Authorities.
     * They are used in
     * {@link org.springframework.security.access.prepost.PreAuthorize @PreAuthorize} annotations.<br/>
     * E.g. <i>@PreAuthorize("hasAuthority('packagemaintainer'))")</i>
     * will allow only {@link PackageMaintainer Package Maintainers}
     * to execute an annotated method.
     */
    default Collection<? extends GrantedAuthority> getGrantedAuthorities(String userLogin) {
        return Set.of();
    }
}
