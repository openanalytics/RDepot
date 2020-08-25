/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.MovePackageSourceException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageSourceNotFoundException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.SendEmailException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionAcceptException;
import eu.openanalytics.rdepot.exception.SubmissionCreateException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.exception.UploadRequestValidationException;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageUploadRequest;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.SubmissionRepository;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.SubmissionAlreadyAcceptedWarning;
import eu.openanalytics.rdepot.warning.SubmissionCreateWarning;
import eu.openanalytics.rdepot.warning.SubmissionDeleteWarning;
import eu.openanalytics.rdepot.warning.SubmissionNeedsToBeAcceptedWarning;
import eu.openanalytics.rdepot.warning.UploadRequestValidationWarning;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class SubmissionService
{	
	Logger logger = LoggerFactory.getLogger(SubmissionService.class);
	
	Locale locale = LocaleContextHolder.getLocale();
	
	@Resource
	private MessageSource messageSource;
	
	@Resource
	private SubmissionRepository submissionRepository;
	
	@Resource
	private UserService userService;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private RepositoryService repositoryService;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private EmailService emailService;
	
	@Resource
	private SubmissionEventService submissionEventService;
	
	@Resource
	private RepositoryEventService repositoryEventService;
	
	@Resource
	private UploadRequestService uploadRequestService;
	
	@Resource
	private PackageStorage packageStorage;
	
	@Resource
	private RepositoryStorage repositoryStorage;
	
	@Resource
	private RepositoryMaintainerService repositoryMaintainerService;

	@Transactional(readOnly = false, rollbackFor=SubmissionCreateException.class)
	public Submission create(PackageUploadRequest uploadRequest, User creator) 
			throws SubmissionCreateException,
			SubmissionCreateWarning,
			SubmissionNeedsToBeAcceptedWarning  {
		Submission submission = new Submission();
		try {
			Event createEvent = eventService.getCreateEvent();
			submission.setUser(creator);
			submission.setPackage(uploadRequestService.createPackage(uploadRequest, creator));
			submission = submissionRepository.save(submission);
			submissionEventService.create(createEvent, creator, submission);
			
			if(uploadRequestService.canUpload(submission.getPackage().getName(),
					submission.getPackage().getRepository(), creator)) {
				acceptSubmission(submission, creator);
			} else {
				URI managerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(
						"/manager/submissions/{id}").buildAndExpand(submission.getId()).toUri();
				emailService.sendActivateSubmissionEmail(submission, managerUrl.toString());
				throw new SubmissionNeedsToBeAcceptedWarning(messageSource, locale, submission);
			}
			
		} catch (UploadRequestValidationException | EventNotFound | SubmissionAcceptException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionCreateException(messageSource, locale, submission);
		} catch(UploadRequestValidationWarning | SubmissionAlreadyAcceptedWarning | SendEmailException w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
			throw new SubmissionCreateWarning(messageSource, locale, submission);
		} catch(SubmissionNeedsToBeAcceptedWarning w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
			throw w;
		}
		
		return submission;
	}
	
	public Submission findById(int id) {
		return submissionRepository.findByIdAndDeleted(id, false);
	}
	
	public Submission findByIdEvenDeleted(int id) {
		return submissionRepository.getOne(id);
	}
	
	public Submission findByIdAndDeleted(int id, boolean deleted) {
		return submissionRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false)
	public void deleteSubmission(int id, User deleter) 
			throws SubmissionDeleteException, SubmissionNotFound, SubmissionDeleteWarning {
		delete(id, deleter, false);
	}
	
	@Transactional(readOnly=false)
	public void rejectSubmission(int id, User deleter) 
			throws SubmissionDeleteException, SubmissionNotFound, SubmissionDeleteWarning {
		delete(id, deleter, true);
	}
	
	@Transactional(readOnly = false, rollbackFor={SubmissionDeleteException.class})
	private void delete(int id, User deleter, boolean deletePackageSource) 
			throws SubmissionDeleteException, SubmissionNotFound, SubmissionDeleteWarning {
		
		Submission deletedSubmission = submissionRepository.findByIdAndDeleted(id, false);
				
		if (deletedSubmission == null)
			throw new SubmissionNotFound(messageSource, locale, id);

		try {
			Event deleteEvent = eventService.getDeleteEvent();
			
			if(deletePackageSource)
				packageService.deleteSource(deletedSubmission.getPackage());
			
			submissionEventService.create(deleteEvent, deleter, deletedSubmission);
			deletedSubmission.setDeleted(true);
			
			if(deleter.getId() == deletedSubmission.getUser().getId() && 
					!userService.isAuthorizedToAccept(deletedSubmission, deleter))
				emailService.sendCanceledSubmissionEmail(deletedSubmission); 
		}
		catch (EventNotFound | SourceFileDeleteException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionDeleteException(messageSource, locale, id);
		}
		catch (SendEmailException e) {
			logger.warn(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionDeleteWarning(messageSource, locale, id);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={SubmissionDeleteException.class})
	public Submission shiftDelete(int id) throws SubmissionDeleteException, SubmissionNotFound {
		Submission deletedSubmission = submissionRepository.findByIdAndDeleted(id, true);
		if (deletedSubmission == null)
			throw new SubmissionNotFound(messageSource, locale, id);
		
		for(SubmissionEvent event : deletedSubmission.getSubmissionEvents())
			submissionEventService.delete(event.getId());
		submissionRepository.delete(deletedSubmission);	
		return deletedSubmission;
	}

	public List<Submission> findAll() {
		return submissionRepository.findByDeleted(false, Sort.by(new Order(Direction.DESC, "id")));
	}
	
	public List<Submission> findByDeleted(boolean deleted) {
		return submissionRepository.findByDeleted(deleted, Sort.by(new Order(Direction.DESC, "id")));
	}
	
	@Transactional(readOnly=false, rollbackFor=SubmissionNotFound.class)
	public Submission updateUser(User user, Submission submission, User updater) 
			throws SubmissionEditException  {
		try {
			Event updateEvent = eventService.getUpdateEvent();
			SubmissionEvent event = new SubmissionEvent(0, DateProvider.now(), updater, submission,
					updateEvent, "user", "" + submission.getUser().getId(), "" + user.getId(), DateProvider.now());
			
			submission.setUser(user);
			submissionEventService.create(event);
			
			return submission;
		} catch (EventNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionEditException(messageSource, locale, submission);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor=SubmissionNotFound.class)
	public Submission updatePackage(Package packageBag, Submission submission, User updater) 
			throws SubmissionEditException {
		try {
			Event updateEvent = eventService.getUpdateEvent();
			SubmissionEvent event = new SubmissionEvent(0, DateProvider.now(), updater, submission, 
					updateEvent, "package", "" + submission.getPackage().getId(), 
					"" + packageBag.getId(), DateProvider.now());
			
			submission.setPackage(packageBag);
			submissionEventService.create(event);
			
			return submission;
		} catch (EventNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionEditException(messageSource, locale, submission);
		}
		
	}
	
	@Transactional(readOnly=false, rollbackFor=SubmissionAcceptException.class)
	public Submission acceptSubmission(Submission submission, User updater) 
			throws SubmissionAlreadyAcceptedWarning, SubmissionAcceptException {
		Package packageBag = submission.getPackage();
		Repository repository = packageBag.getRepository();
		String initialSource = packageBag.getSource();
		
		if(submission.isAccepted()) {
			SubmissionAlreadyAcceptedWarning warning = 
					new SubmissionAlreadyAcceptedWarning(messageSource, locale, submission);
			logger.warn(warning.getClass().getName() + ": ", warning.getMessage(), warning);		
			throw warning;
		}
		
		try {
			Event updateEvent = eventService.getUpdateEvent();
			SubmissionEvent event = new SubmissionEvent(0, DateProvider.now(), updater, submission,
					updateEvent, "accepted", "" + false, "" + true, DateProvider.now());
			
			submission.setAccepted(true);
			
			repositoryService.boostRepositoryVersion(repository, updater);
			submissionEventService.create(event);
			repositoryEventService.create(new RepositoryEvent(0, DateProvider.now(), updater, 
					submission.getPackage().getRepository(), updateEvent, 
					"added", "", "" + submission.getPackage().getId(), DateProvider.now()));			
			
			try {
				packageService.activatePackage(packageBag, updater);
			} catch (PackageAlreadyActivatedWarning e) {
				logger.warn(e.getClass().getName() + ": " + e.getMessage());
			}
			
			packageService.updateSource(packageBag, 
					packageStorage.moveToMainDirectory(packageBag).toString(), updater);
			
			if(repository.isPublished()) {
				repositoryService.publishRepository(repository, updater);
			}
			return submission;
		} catch(EventNotFound |  
				PackageActivateException |
				MovePackageSourceException |
				RepositoryEditException |
				PackageSourceNotFoundException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionAcceptException(messageSource, locale, submission);
		} catch(RepositoryPublishException | 
				PackageEditException e) {
			try {
				packageStorage.moveSource(packageBag, initialSource);
			} catch (PackageSourceNotFoundException | MovePackageSourceException e1) {
				logger.error("Failed to revert changes after submission accept error!\n"
						+ e1.getMessage(), e1);
			}
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SubmissionAcceptException(messageSource, locale, submission);
		}
	}
	
	public void evaluateAndUpdate(Submission submission, User updater) 
			throws SubmissionEditException, 
			SubmissionAlreadyAcceptedWarning, SubmissionAcceptException {
		Submission currentSubmission = submissionRepository.findByIdAndDeleted(submission.getId(), false);
		
		if(currentSubmission.getUser().getId() != submission.getUser().getId())
			updateUser(submission.getUser(), currentSubmission, updater);
		if(currentSubmission.getPackage().getId() != submission.getPackage().getId())
			updatePackage(submission.getPackage(), currentSubmission, updater);
		if(!currentSubmission.isAccepted() && submission.isAccepted())
			acceptSubmission(currentSubmission, updater);
	}
	
	public Submission chooseBestSubmitter(Submission submission, User requester) 
			throws AdminNotFound, SubmissionEditException {
		updateUser(userService.findFirstAdmin(), submission, requester);
		return submission;
	}
	
	public List<Submission> findAllFor(User user) {
		List<Submission> submissions = new ArrayList<Submission>();
		
		switch(user.getRole().getName()) {
			case "admin":
				submissions.addAll(findAll());
				break;
			case "repositorymaintainer":
				user = userService.findById(user.getId());
				for(RepositoryMaintainer repositoryMaintainer : user.getRepositoryMaintainers()) {
					List<Submission> ss = findByRepository(repositoryMaintainer.getRepository());
					for(Submission s : ss) {
						if(s.getUser().getId() != user.getId())
							submissions.add(s);
					}
 				}
				submissions.addAll(findBySubmitter(user));
				break;
			case "packagemaintainer":
				user = userService.findById(user.getId());
				List<Package> packages = new ArrayList<Package>();
				for(PackageMaintainer packageMaintainer : user.getPackageMaintainers()) {
					packages.addAll(packageService.findByNameAndRepository(
							packageMaintainer.getPackage(), packageMaintainer.getRepository()));
				}
				for(Package p : packages) {
					Submission submission = findByPackage(p);
					if(submission.getUser().getId() != user.getId())
						submissions.add(submission);
				}
				submissions.addAll(findBySubmitter(user));
				break;
			case "user":
				submissions.addAll(findBySubmitter(user));
				break;
		}
		return submissions;
	}
	
	public List<Submission> findBySubmitter(User submitter) {
		return submissionRepository.findByUserAndDeleted(submitter, false, 
				Sort.by(new Order(Direction.DESC, "id")));
	}

	public List<Submission> findByRepository(Repository repository) {
		return submissionRepository.findByDeletedAndPackage_Repository(false, repository);
	}

	public Submission findByPackage(Package packageBag) {
		return submissionRepository.findByPackageAndDeleted(packageBag, false);
	}
	
	@Transactional(readOnly = false)
	public void fixSubmissions(Set<Submission> submissions, User requester) 
			throws SubmissionEditException, AdminNotFound {
		for(Submission submission : submissions) {
			//updateUser(chooseBestSubmitter(submission), submission, requester);
			chooseBestSubmitter(submission, requester);
		}
//			evaluateAndUpdate(chooseBestSubmitter(submission), requester);
	}

}
