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
package eu.openanalytics.rdepot.r.legacy.api.v1.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicatedToSilentlyIgnore;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.DTOConverter;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.SubmissionV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.SubmissionNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException;
import eu.openanalytics.rdepot.r.legacy.security.authorization.SecurityMediatorImplV1;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;

@Controller
public class SubmissionController {	
	
	@Autowired
	private SubmissionService submissionService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private RRepositoryService repositoryService;
	
	@Autowired
	private RStrategyFactory factory;
	
	@Autowired
	private SecurityMediatorImplV1 securityMediator;
	
	@Autowired
	private SubmissionDeleter submissionDeleter;
	
	@Autowired
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Autowired
	private PackageMaintainerService packageMaintainerService;
	
	@Autowired
	private RPackageService packageService;
	
	private final Locale locale = LocaleContextHolder.getLocale();
	
	/**
	 * This method is used to upload a package to the Manager.<br/>
	 * It uses multipart form to carry a file.
	 * @param multipartFile package file
	 * @param repository where package is to be uploaded
	 * @param replace if package should replace a similar one
	 * @param principal represents the uploader
	 * @return response entity with JSON object containing success, error or warning message under the respective fields
	 * @throws UserUnauthorizedException 
	 * @throws RepositoryNotFound 
	 */
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(
			value={"/api/manager/packages/submit", "/manager/packages/submit"}, 
			method=RequestMethod.POST, 
			produces=MediaType.APPLICATION_JSON_VALUE,
			consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody ResponseEntity<?> createSubmission(
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam("repository") String repository,
			@RequestParam(name="generateManual", defaultValue="${generate-manuals}") boolean generateManual,
			@RequestParam(name="replace", defaultValue="false") boolean replace,
			Principal principal) throws UserUnauthorizedException, RepositoryNotFound {
		
		HashMap<String, Pair<String, String>> result = new HashMap<>();
		
		RRepository repositoryEntity = repositoryService.findByNameAndDeleted(repository, false)
				.orElseThrow(() -> new RepositoryNotFound());
		
		PackageUploadRequest<RRepository> uploadRequest = 
				new PackageUploadRequest<>(multipartFile, repositoryEntity, generateManual, replace);
		User uploader = userService.findByLogin(principal.getName()).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(uploader == null) {
			throw new UserUnauthorizedException();
		} else {
			try {
				Strategy<Submission> strategy = factory.uploadPackageStrategy(uploadRequest, uploader);
//				submissionService.create(uploadRequest, uploader);
				strategy.perform();
				// TODO: check if submission really just is submission and doesn't alter anything in the db
				result.put("success", Pair.of(multipartFile.getOriginalFilename(), 
						messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_CREATED, null, 
								MessageCodes.SUCCESS_SUBMISSION_CREATED, locale)));
			} catch(StrategyFailure  e) {
				if(e.getReason() instanceof PackageDuplicatedToSilentlyIgnore) {
					result.put("warning", Pair.of(multipartFile.getOriginalFilename(), e.getMessage()));
					httpStatus = HttpStatus.OK;
				}
				else if(e.getReason() instanceof PackageValidationException) {
					result.put("error", Pair.of(multipartFile.getOriginalFilename(), e.getMessage()));
					httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
				} else {
					result.put("error", Pair.of(multipartFile.getOriginalFilename(), e.getMessage()));
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				}
			}
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}

	/**
	 * This method provides a list of submissions created by the user.
	 * @param principal represents the user
	 * @return list of created submissions
	 */
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/submissions/list", "/api/manager/submissions/list"}, 
		method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<SubmissionV1Dto> submissions(Principal principal) {
		User submitter = userService.findByLogin(principal.getName()).orElse(null);
		List<Submission> submissionEntities = submissionService.findBySubmitter(submitter);
		return DTOConverter.convertSubmissions(submissionEntities);
	}
	
	/**
	 * This method provides a list of all deleted submissions.
	 * @return list of deleted submissions (with state CANCELLED or REJECTED).
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/submissions/deleted","/api/manager/submissions/deleted"}, method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<SubmissionV1Dto> deletedSubmissions() {
		List<Submission> cancelledSubmissionEntities = submissionService.findAllByState(SubmissionState.CANCELLED);
		List<Submission> rejectedSubmissionEntities = submissionService.findAllByState(SubmissionState.REJECTED);
		List<Submission> submissionEntites = Stream.concat(cancelledSubmissionEntities.stream(), 
				rejectedSubmissionEntities.stream()).collect(Collectors.toList());
		return DTOConverter.convertSubmissions(submissionEntites);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions", method=RequestMethod.GET)
	public String submissionsPage(Principal principal, Model model) {
		User submitter = userService.findByLogin(principal.getName()).orElse(null);
		List<Submission> submissionEntities = submissionService.findBySubmitter(submitter);
		model.addAttribute("submissions", DTOConverter.convertSubmissions(submissionEntities));
		model.addAttribute("user", DTOConverter.convertUser(submitter));
		model.addAttribute("role", submitter.getRole().getValue());
		model.addAttribute("filter", "allForSubmitter");
		return "submissions";
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/all", method=RequestMethod.GET)
	public String allSubmissionsPage(Principal principal, Model model) {
		User user = userService.findByLogin(principal.getName()).orElse(null);
		model.addAttribute("submissions", findAllSubmissionsForUser(user));
		model.addAttribute("user", DTOConverter.convertUser(user));
		model.addAttribute("role", user.getRole().getValue());
		model.addAttribute("filter", "allForUser");
		return "submissions";
	}

	private List<SubmissionV1Dto> findAllSubmissionsForUser(User user) {
		List<Submission> submissions = new ArrayList<>();
		
		switch(user.getRole().getValue()) {
		case Role.VALUE.ADMIN:
			submissions.addAll(submissionService.findAll());
			break;
		case Role.VALUE.REPOSITORYMAINTAINER:
			for(RepositoryMaintainer maintainer : repositoryMaintainerService.findByUserWithoutDeleted(user)) {
				List<Submission> repoSubmissions = 
						submissionService.findAllByRepository(maintainer.getRepository());
				submissions.addAll(repoSubmissions);
			}
			break;
		case Role.VALUE.PACKAGEMAINTAINER:
			List<RPackage> packages = new ArrayList<>();
			for(PackageMaintainer maintainer : packageMaintainerService.findByUser(user)) {
				packages.addAll(packageService
						.findAllByNameAndRepository(
								maintainer.getPackageName(), maintainer.getRepository()));
			}
			for(RPackage p : packages) {
				submissionService.findByPackage(p).ifPresent(s -> submissions.add(s));
			}
			break;
		case Role.VALUE.USER:
			submissions.addAll(submissionService.findBySubmitter(user));
			break;
		}
		return DTOConverter.convertSubmissions(submissions);
	}

	/**
	 * This method cancels submission. <br/>
	 * It means submission is deleted and uploaded package source is removed from the file system.
	 * @param id submission ID
	 * @param principal represents the user
	 * @return response entity with JSON object containing success, error or warning message under the respective fields
	 * @throws SubmissionNotFound 
	 * @throws UserUnauthorizedException 
	 * @throws SubmissionDeleteException 
	 */
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/submissions/{id}/cancel","/api/manager/submissions/{id}/cancel"}, 
		method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> cancelSubmissionBySubmitterOrMaintainer(
			@PathVariable Integer id, Principal principal) 
					throws SubmissionNotFound, UserUnauthorizedException {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Submission submission = submissionService.findById(id)
				.orElseThrow(() -> new SubmissionNotFound());
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		HttpStatus httpStatus = HttpStatus.OK;
		final SubmissionState desiredState = 
				submission.getUser().getId() == requester.getId() ? 
						SubmissionState.CANCELLED : SubmissionState.REJECTED;	
		
		try {
			if(desiredState.equals(SubmissionState.CANCELLED) 
					&& !securityMediator.isAuthorizedToCancel(submission, requester)
				|| 
				desiredState.equals(SubmissionState.REJECTED) 
					&& !securityMediator.isAuthorizedToReject(submission, requester)) {
				throw new UserUnauthorizedException();
			} else if(submission.getState() == SubmissionState.ACCEPTED) {
				result.put("warning", messageSource.getMessage(MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED, 
						null, MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED, locale));
			} else {
//				submissionService.rejectSubmission(submission, requester);
//				if(requester.getId() == submission.getUser().getId() && !userService.isAuthorizedToAccept(submission, requester))
//					emailService.sendCanceledSubmissionEmail(submission);
				RRepository repository = repositoryService.findById(
						submission.getPackage().getRepository().getId())
						.orElseThrow(
						() -> new IllegalStateException("Constraints violated: "
								+ "Repository for given submission does not exist."));
				
				Submission cancelled = new Submission(submission);
				cancelled.setState(desiredState);
				factory.updateSubmissionStrategy(submission, cancelled, repository, requester).perform();
				
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_CANCELED, null, 
						MessageCodes.SUCCESS_SUBMISSION_CANCELED, locale));					
			}
		} catch(StrategyFailure e) {
			result.put("warning", e.getMessage());
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/{id}", method=RequestMethod.GET)
	public String submissionPage(@PathVariable Integer id, Principal principal, 
			RedirectAttributes redirectAttributes, Model model) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Submission submission = submissionService.findById(id).orElse(null);
		String address = "redirect:/manager/submissions/all";
		if(submission == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
		else if(!securityMediator.isAuthorizedToCancel(submission, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);		
		else {
			model.addAttribute("submission", DTOConverter.convertSubmission(submission));
			model.addAttribute("role", requester.getRole().getValue());

			address = "submission";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/submissions/{id}/accept","/api/manager/submissions/{id}/accept"}, method=RequestMethod.PATCH, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> acceptSubmissionByMaintainerREST(@PathVariable Integer id, 
			Principal principal) throws UserUnauthorizedException, SubmissionNotFound {
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Submission submission = submissionService.findById(id).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		HashMap<String, String> result = new HashMap<String, String>();

		if(submission == null) {
			throw new SubmissionNotFound();
		} else if(requester == null || !securityMediator.isAuthorizedToAccept(submission, requester)) {
			throw new UserUnauthorizedException();
		} else {
			try {
				RRepository repository = repositoryService.findById(submission.getPackage().getRepository().getId()).orElseThrow(
						() -> new IllegalStateException("Constraints violated: "
								+ "Repository for given submission does not exist."));
				
				Submission accepted = new Submission(submission);
				accepted.setState(SubmissionState.ACCEPTED);
				
				factory.updateSubmissionStrategy(submission, accepted, repository, requester).perform();
				
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_ACCEPTED, null, 
						MessageCodes.SUCCESS_SUBMISSION_ACCEPTED, locale));
			} catch(StrategyFailure e) {
				result.put("warning", e.getMessage());
			}
		}
		return new ResponseEntity<>(result, httpStatus);

	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/submissions/{id}/sdelete","/api/manager/submissions/{id}/sdelete"}, method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeleteSubmission(@PathVariable Integer id) throws SubmissionNotFound, DeleteEntityException {
		Locale locale = LocaleContextHolder.getLocale();
		HttpStatus httpStatus = HttpStatus.OK;
		
		Optional<Submission> submission = submissionService.findByIdAndState(id, SubmissionState.CANCELLED);
		if(submission.isEmpty()) {
			submission = submissionService.findByIdAndState(id, SubmissionState.REJECTED);
		}
		if(submission.isEmpty()) {
			throw new SubmissionNotFound();
		}
		HashMap<String, String> result = new HashMap<String, String>();
		
		submissionDeleter.delete(submission.get());
		result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_DELETED, null, 
					MessageCodes.SUCCESS_SUBMISSION_DELETED, locale));		
			
		return new ResponseEntity<>(result, httpStatus);
	}
}
