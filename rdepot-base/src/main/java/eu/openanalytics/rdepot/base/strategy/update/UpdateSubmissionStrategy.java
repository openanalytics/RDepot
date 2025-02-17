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
package eu.openanalytics.rdepot.base.strategy.update;

import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.WrongServiceException;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;

/**
 * Updates submission state.
 * @param <P> Technology-specific {@link Package} type.
 * @param <R> Technology-specific {@link Repository} type.
 */
public class UpdateSubmissionStrategy<P extends Package, R extends Repository> extends UpdateStrategy<Submission> {

    private final Storage<R, P> storage;
    private final PackageService<P> packageService;
    private final RepositoryService<R> repositoryService;
    private final EmailService emailService;
    private final SecurityMediator securityMediator;
    private final RepositorySynchronizer<R> repositorySynchronizer;
    private boolean requiresRepublishing = false;
    private final R repository;

    private static final String STATE = "state";
    private static final String APPROVER_ID = "approver_id";

    public UpdateSubmissionStrategy(
            Submission resource,
            NewsfeedEventService eventService,
            SubmissionService service,
            User requester,
            Submission updateSubmission,
            PackageService<P> packageService,
            Storage<R, P> storage,
            EmailService emailService,
            SecurityMediator securityMediator,
            RepositorySynchronizer<R> repositorySynchronizer,
            R repository,
            RepositoryService<R> repositoryService) {
        super(resource, service, eventService, requester, updateSubmission, new Submission(resource));
        this.packageService = packageService;
        this.storage = storage;
        this.emailService = emailService;
        this.securityMediator = securityMediator;
        this.repositorySynchronizer = repositorySynchronizer;
        this.repository = repository;
        this.repositoryService = repositoryService;
    }

    @Override
    protected Submission actualStrategy() throws StrategyFailure {
        if (updatedResource.getState() == SubmissionState.ACCEPTED) {
            acceptSubmission(resource);
        } else if (updatedResource.getState() == SubmissionState.CANCELLED) {
            cancelSubmission(resource);
        } else if (updatedResource.getState() == SubmissionState.REJECTED) {
            rejectSubmission(resource);
        }

        return resource;
    }

    /**
     * Used when waiting submission is rejected by maintainer.
     */
    private void rejectSubmission(Submission submission) throws StrategyFailure {
        recycleSubmission(submission);

        submission.setState(SubmissionState.REJECTED);
        submission.setApprover(requester);
        changedValues.add(
                new EventChangedVariable(STATE, submission.getState().getValue(), SubmissionState.REJECTED.getValue()));
        changedValues.add(new EventChangedVariable(APPROVER_ID, "", String.valueOf(requester.getId())));
    }

    /**
     * Used when waiting submission is cancelled by its author.
     */
    private void cancelSubmission(Submission submission) throws StrategyFailure {
        recycleSubmission(submission);

        if (requester.getId() == submission.getSubmitter().getId()
                && !securityMediator.isAuthorizedToAccept(submission, requester)) {
            emailService.sendCancelledSubmissionEmail(submission);
        }

        submission.setState(SubmissionState.CANCELLED);
        submission.setApprover(requester);
        changedValues.add(new EventChangedVariable(
                STATE, submission.getState().getValue(), SubmissionState.CANCELLED.getValue()));
        changedValues.add(new EventChangedVariable(APPROVER_ID, "", String.valueOf(requester.getId())));
    }

    /**
     * Moves package source to the trash directory and
     * set related package object as deleted
     */
    private void recycleSubmission(Submission submission) throws StrategyFailure {
        try {
            P packageBag =
                    packageService.findById(submission.getPackage().getId()).orElseThrow(WrongServiceException::new);
            packageBag.setSource(storage.moveToTrashDirectory(packageBag));
            packageBag.setActive(false);
            packageBag.setDeleted(true);
        } catch (WrongServiceException | MovePackageSourceException e) {
            throw new StrategyFailure(e);
        }
    }

    /**
     * Accepts submission and moves its source from waiting room to designated directory.
     */
    private void acceptSubmission(Submission submission) throws StrategyFailure {
        try {
            P packageBag = packageService
                    .findById(submission.getPackage().getId())
                    // TODO: #32886 We can cast it in the service method
                    // so that not to fetch it twice
                    .orElseThrow(WrongServiceException::new);
            packageBag.setSource(storage.moveToMainDirectory(packageBag));
            packageBag.setActive(true);
            requiresRepublishing = packageBag.getRepository().getPublished();
            repositoryService.incrementVersion(repository);
        } catch (InvalidSourceException | MovePackageSourceException | WrongServiceException e) {
            throw new StrategyFailure(e);
        }

        submission.setState(SubmissionState.ACCEPTED);
        submission.setApprover(requester);
        changedValues.add(new EventChangedVariable(
                STATE, SubmissionState.WAITING.getValue(), SubmissionState.ACCEPTED.getValue()));
        changedValues.add(new EventChangedVariable(APPROVER_ID, "", String.valueOf(requester.getId())));
    }

    @Override
    public void postStrategy() throws StrategyFailure {
        try {
            if (requiresRepublishing) repositorySynchronizer.storeRepositoryOnRemoteServer(repository);
        } catch (SynchronizeRepositoryException e) {
            throw new StrategyFailure(e, false);
        }
    }

    @Override
    protected NewsfeedEvent generateEvent(Submission resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
    }
}
