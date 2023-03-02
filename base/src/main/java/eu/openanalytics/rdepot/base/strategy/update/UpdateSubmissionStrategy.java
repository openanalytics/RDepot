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
package eu.openanalytics.rdepot.base.strategy.update;

import javax.naming.OperationNotSupportedException;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.EmailService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.WrongServiceException;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.time.DateProvider;

/**
 * Changes submission state.
 * @param <P> Package extension-specific type
 * @param <R> Repository extension-specific type
 */
public class UpdateSubmissionStrategy
	<P extends Package<P, ?>, R extends Repository<R, ?>> 
	extends UpdateStrategy<Submission> {
	
	private final Storage<R, P> storage;
	private final PackageService<P> packageService;
	private final RepositoryService<R> repositoryService;
	private final EmailService emailService;
	private final SecurityMediator securityMediator;
	private final RepositorySynchronizer<R> repositorySynchronizer;
	private boolean requiresRepublishing = false;
	private final R repository;
	
	public UpdateSubmissionStrategy(Submission resource, NewsfeedEventService eventService, 
			SubmissionService service, User requester, Submission updateSubmission,
			PackageService<P> packageService,
			Storage<R, P> storage, EmailService emailService, SecurityMediator securityMediator,
			RepositorySynchronizer<R> repositorySynchronizer, R repository, RepositoryService<R> repositoryService) {
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
		if(resource.getState() == SubmissionState.WAITING) {
			if(updatedResource.getState() == SubmissionState.ACCEPTED) {
				acceptSubmission(resource);
			} else if(updatedResource.getState() == SubmissionState.CANCELLED) {
				cancelSubmission(resource);
			} else if(updatedResource.getState() == SubmissionState.REJECTED) {
				rejectSubmission(resource);
			}
		} else {
			throw new StrategyFailure(new OperationNotSupportedException());
		}
		
		return resource;
	}

	/**
	 * Used when waiting submission is rejected by maintainer.
	 * @param submission
	 * @throws StrategyFailure
	 */
	private void rejectSubmission(Submission submission) throws StrategyFailure {
		recycleSubmission(submission);
		
		changedValues.add(new EventChangedVariable("state", 
				submission.getState().getValue(), SubmissionState.REJECTED.getValue()));
		submission.setState(SubmissionState.REJECTED);
	}

	/**
	 * Used when waiting submission is cancelled by its author.
	 * @param submission
	 * @throws StrategyFailure
	 */
	private void cancelSubmission(Submission submission) throws StrategyFailure {
		recycleSubmission(submission);
		
		if(requester.getId() == submission.getUser().getId() && //TODO: Are we sure it is correct?
				!securityMediator.isAuthorizedToAccept(submission, requester)) {
			emailService.sendCancelledSubmissionEmail(submission);
		}
		
		changedValues.add(new EventChangedVariable("state", 
				submission.getState().getValue(), SubmissionState.CANCELLED.getValue()));
		submission.setState(SubmissionState.REJECTED);
	}
	
	/**
	 * Moves package source to the trash directory and 
	 * set related package object as deleted
	 * @param submission
	 * @throws StrategyFailure
	 */
	private void recycleSubmission(Submission submission) throws StrategyFailure {
		try {
			P packageBag = packageService.findById(submission.getPackage().getId()) //TODO: We can cast it in the service method so that not to fetch it twice
					.orElseThrow(() -> new WrongServiceException());
			packageBag.setSource(storage.moveToTrashDirectory(packageBag));
			packageBag.setActive(false);
			packageBag.setDeleted(true);
		} catch (WrongServiceException | MovePackageSourceException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e);
		}
	}

	/**
	 * Accepts submission and moves its source from waiting room to designated directory.
	 * @param submission
	 * @throws StrategyFailure
	 */
	private void acceptSubmission(Submission submission) throws StrategyFailure {
		try {
			P packageBag = packageService.findById(submission.getPackage().getId()) //TODO: We can cast it in the service method so that not to fetch it twice
					.orElseThrow(() -> new WrongServiceException());
			packageBag.setSource(storage.moveToMainDirectory(packageBag));
			packageBag.setActive(true);
			requiresRepublishing = packageBag.getRepository().isPublished();
			repositoryService.incrementVersion(repository);
		} catch(InvalidSourceException | MovePackageSourceException | WrongServiceException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e);
		}
				
		submission.setState(SubmissionState.ACCEPTED);
		changedValues.add(new EventChangedVariable("state", 
				SubmissionState.WAITING.getValue(), SubmissionState.ACCEPTED.getValue()));
	}
	
	@Override
	protected void postStrategy() throws StrategyFailure {
		try {
			if(requiresRepublishing)
				repositorySynchronizer.storeRepositoryOnRemoteServer(repository, 
						DateProvider.getCurrentDateStamp());
		} catch(SynchronizeRepositoryException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e, false);
		}
		
	}

	@Override
	public void revertChanges() throws StrategyReversionFailure {
		//TODO
	}

	@Override
	protected NewsfeedEvent generateEvent(Submission resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
	}
}
