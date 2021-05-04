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
package eu.openanalytics.rdepot.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import eu.openanalytics.rdepot.exception.SubmissionAcceptException;
import eu.openanalytics.rdepot.exception.SubmissionCreateException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.PackageUploadRequest;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.warning.SubmissionAlreadyAcceptedWarning;
import eu.openanalytics.rdepot.warning.SubmissionCreateWarning;
import eu.openanalytics.rdepot.warning.SubmissionDeleteWarning;
import eu.openanalytics.rdepot.warning.SubmissionNeedsToBeAcceptedWarning;

@Controller
public class SubmissionController {	
	
	@Autowired
	private SubmissionService submissionService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private MessageSource messageSource;
	
	/**
	 * This method is used to upload a package to the Manager.<br/>
	 * It uses multipart form to carry a file.
	 * @param multipartFile package file
	 * @param repository where package is to be uploaded
	 * @param replace if package should replace a similar one
	 * @param principal represents the uploader
	 * @return response entity with JSON object containing success, error or warning message under the respective fields
	 * @throws UserUnauthorizedException 
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
			Principal principal) throws UserUnauthorizedException {
		
		HashMap<String, Pair<String, String>> result = new HashMap<>();
		PackageUploadRequest uploadRequest = new PackageUploadRequest(multipartFile, repository, generateManual, replace);
		User uploader = userService.findByLoginWithMaintainers(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(uploader == null) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			try {
				submissionService.create(uploadRequest, uploader);

				// TODO: check if submission really just is submission and doesn't alter anything in the db
				result.put("success", Pair.of(multipartFile.getOriginalFilename(), 
						messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_CREATED, null, 
								MessageCodes.SUCCESS_SUBMISSION_CREATED, locale)));
			} catch(SubmissionCreateException  e) {
				result.put("error", Pair.of(multipartFile.getOriginalFilename(), e.getMessage()));
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			} catch(SubmissionCreateWarning | SubmissionNeedsToBeAcceptedWarning w) {
				result.put("warning", Pair.of(multipartFile.getOriginalFilename(), w.getMessage()));
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
	public @ResponseBody List<Submission> submissions(Principal principal) {
		User submitter = userService.findByLoginWithMaintainers(principal.getName());
		List<Submission> submissionList = submissionService.findBySubmitter(submitter);
		return submissionList;
	}
	
	/**
	 * This method provides a list of all deleted submissions.
	 * @return list of deleted submissions.
	 */
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/submissions/deleted","/api/manager/submissions/deleted"}, method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Submission> deletedSubmissions() {
		return submissionService.findByDeleted(true);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions", method=RequestMethod.GET)
	public String submissionsPage(Principal principal, Model model) {
		User submitter = userService.findByLoginWithMaintainers(principal.getName());
		model.addAttribute("submissions", submissionService.findBySubmitter(submitter));
		model.addAttribute("user", submitter);
		model.addAttribute("role", submitter.getRole().getValue());
		model.addAttribute("filter", "allForSubmitter");
		return "submissions";
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/all", method=RequestMethod.GET)
	public String allSubmissionsPage(Principal principal, Model model) {
		User user = userService.findByLoginWithMaintainers(principal.getName());
		model.addAttribute("submissions", submissionService.findAllFor(user));
		model.addAttribute("user", user);
		model.addAttribute("role", user.getRole().getValue());
		model.addAttribute("filter", "allForUser");
		return "submissions";
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
					throws SubmissionNotFound, UserUnauthorizedException, SubmissionDeleteException {
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			if(submission == null) {
				throw new SubmissionNotFound(messageSource, locale, id);
			} else if(!userService.isAuthorizedToCancel(submission, requester)) {
				throw new UserUnauthorizedException(messageSource, locale);
			} else if(submission.isAccepted()) {
				result.put("warning", messageSource.getMessage(MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED, 
						null, MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED, locale));
			} else {
				submissionService.rejectSubmission(submission, requester);
//				if(requester.getId() == submission.getUser().getId() && !userService.isAuthorizedToAccept(submission, requester))
//					emailService.sendCanceledSubmissionEmail(submission);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_CANCELED, null, 
						MessageCodes.SUCCESS_SUBMISSION_CANCELED, locale));					
			}
		} catch(SubmissionDeleteWarning w) {
			result.put("warning", w.getMessage());
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/{id}", method=RequestMethod.GET)
	public String submissionPage(@PathVariable Integer id, Principal principal, 
			RedirectAttributes redirectAttributes, Model model) {
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		String address = "redirect:/manager/submissions/all";
		if(submission == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
		else if(!userService.isAuthorizedToCancel(submission, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);		
		else {
			model.addAttribute("submission", submission);
			model.addAttribute("role", requester.getRole().getValue());

			address = "submission";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/submissions/{id}/accept","/api/manager/submissions/{id}/accept"}, method=RequestMethod.PATCH, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> acceptSubmissionByMaintainerREST(@PathVariable Integer id, 
			Principal principal) throws UserUnauthorizedException, SubmissionNotFound, SubmissionAcceptException {
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		HttpStatus httpStatus = HttpStatus.OK;
		HashMap<String, String> result = new HashMap<String, String>();

		if(submission == null) {
			throw new SubmissionNotFound(messageSource, locale, id);
		} else if(requester == null || !userService.isAuthorizedToAccept(submission, requester)) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			try {
				submissionService.acceptSubmission(submission, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_ACCEPTED, null, 
						MessageCodes.SUCCESS_SUBMISSION_ACCEPTED, locale));
			} catch(SubmissionAlreadyAcceptedWarning w) {
				result.put("warning", w.getMessage());
			}
		}
		return new ResponseEntity<>(result, httpStatus);

	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/submissions/{id}/sdelete","/api/manager/submissions/{id}/sdelete"}, method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeleteSubmission(@PathVariable Integer id) throws SubmissionDeleteException, SubmissionNotFound {
		Locale locale = LocaleContextHolder.getLocale();
		HttpStatus httpStatus = HttpStatus.OK;
		Submission submission = submissionService.findByIdAndDeleted(id, true);
		HashMap<String, String> result = new HashMap<String, String>();
		
		if(submission == null) {
			throw new SubmissionNotFound(messageSource, locale, id);
		} else {
			submissionService.shiftDelete(submission);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_SUBMISSION_DELETED, null, 
					MessageCodes.SUCCESS_SUBMISSION_DELETED, locale));		
		}
			
		return new ResponseEntity<>(result, httpStatus);
	}
}
