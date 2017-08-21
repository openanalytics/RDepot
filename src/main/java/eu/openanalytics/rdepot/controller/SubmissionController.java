/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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

import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.SendEmailException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.UploadRequestValidationException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.MultiUploadRequest;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.EmailService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UploadRequestService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.warning.UploadRequestValidationWarning;

@Controller
public class SubmissionController
{	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private PackageService packageService;
	
	@Autowired
	private SubmissionService submissionService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UploadRequestService uploadRequestService;
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/packages/submit", method=RequestMethod.POST)
	public String createSubmissions(@ModelAttribute(value="multiUploads") MultiUploadRequest multiUploads,
			BindingResult result, Principal principal, RedirectAttributes redirectAttributes) 
	{		
		HashMap<String, String> error = new HashMap<String, String>();
		HashMap<String, String> warning = new HashMap<String, String>();
		HashMap<String, String> success = new HashMap<String, String>();
		
		// Can't fail, because the user is already logged in
		// except when someone changes his login name during his session?
		User uploader = userService.findByLoginWithMaintainers(principal.getName());
		
		// Check anyways
		if(uploader == null)
			error.put(principal.getName(), MessageCodes.ERROR_USER_NOT_FOUND);
		else
		{
			for(int i = 0, l = multiUploads.getUploadRequests().length; i < l; i++)
			{
				Repository repository = null;
				for(int j = 0, k = multiUploads.getUploadRequests()[i].getFileData().length; j < k; j++)
				{
					CommonsMultipartFile file = multiUploads.getUploadRequests()[i].getFileData()[j];
					try
					{
						Submission submission = new Submission();
						submission.setUser(uploader);
						if(multiUploads.getUploadRequests()[i].getChanges().length > j)
							submission.setChanges(multiUploads.getUploadRequests()[i].getChanges()[j]);
						submission.setPackage(uploadRequestService.createPackage(
								multiUploads.getUploadRequests()[i], j, uploader));
						submission.setAccepted(uploadRequestService.canUpload(
								submission.getPackage().getName(), submission.getPackage().getRepository(),
								uploader));
						submission = submissionService.create(submission, uploader);
						if(!submission.isAccepted())
						{	try 
							{
								URI managerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(
										"/manager/submissions/{id}").buildAndExpand(submission.getId()).toUri();
								emailService.sendActivateSubmissionEmail(submission, managerUrl.toString());
							} 
							catch (SendEmailException e) 
							{
								throw new UploadRequestValidationWarning("email.send.exception");
							}
						}
						else 
						{
							repository = submission.getPackage().getRepository();
							try 
							{
								repositoryService.boostRepositoryVersion(submission.getPackage().getRepository(), uploader);
							} 
							catch (RepositoryEditException e) 
							{
								throw new UploadRequestValidationWarning(e.getMessage());
							}
						}
						success.put(file.getOriginalFilename(), "submission.create.success");	
					}
					catch(UploadRequestValidationException e)
					{
						error.put(file.getOriginalFilename(), e.getMessage());
					}
					catch(UploadRequestValidationWarning w)
					{
						warning.put(file.getOriginalFilename(), w.getMessage());
					}
				}
				if(repository != null && !repository.isDeleted() && repository.isPublished())
				{	
					try 
					{
						repositoryService.publishRepository(repository, uploader);
					} 
					catch (RepositoryEditException e) 
					{
						error.put(repository.getName(), e.getMessage());
					}
				}
			}
		}
		if(!error.isEmpty())
			redirectAttributes.addFlashAttribute("error", error);
		if(!warning.isEmpty())
			redirectAttributes.addFlashAttribute("warning", warning);
		if(!success.isEmpty())
			redirectAttributes.addFlashAttribute("success", success);	
		return "redirect:/manager";		
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Submission> submissions(Principal principal) 
	{
		User submitter = userService.findByLoginWithMaintainers(principal.getName());
		List<Submission> submissionList = submissionService.findBySubmitter(submitter);
		return submissionList;
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/manager/submissions/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Submission> deletedSubmissions() 
	{
		return submissionService.findByDeleted(true);
	}
	
//	@PreAuthorize("hasAuthority('admin')")
//	@RequestMapping(value="/manager/submissions/all", method=RequestMethod.GET, produces="application/json")
//	@ResponseStatus(HttpStatus.OK)
//	public @ResponseBody List<Submission> allSubmissionsPage() 
//	{
//		List<Submission> submissionList = submissionService.findAll();
//		return submissionList;
//	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions", method=RequestMethod.GET)
	public String submissionsPage(Principal principal, Model model) 
	{
		User submitter = userService.findByLoginWithMaintainers(principal.getName());
		model.addAttribute("submissions", submissionService.findBySubmitter(submitter));
		model.addAttribute("user", submitter);
		model.addAttribute("role", submitter.getRole().getValue());
		return "submissions";
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/all", method=RequestMethod.GET)
	public String allSubmissionsPage(Principal principal, Model model) 
	{
		// This could be faster if we would just give the principal.getName() to submissionService.findAllFor()
		// Because now, we're sometimes querying for the user twice... (see findAllFor -> in case of maintainer)
		User user = userService.findByLoginWithMaintainers(principal.getName());
		model.addAttribute("submissions", submissionService.findAllFor(user));
		model.addAttribute("user", user);
		model.addAttribute("role", user.getRole().getValue());
		return "submissions";
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/manager/submissions/{id}/cancel", method=RequestMethod.GET)
	public String cancelSubmissionByMaintainer(@PathVariable Integer id, Principal principal, 
			RedirectAttributes  redirectAttributes) 
	{
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		try
		{
			if(submission == null)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);	
			else if(submission.isAccepted())
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_ALREADY_ACCEPTED);	
			else if(!UserService.isAuthorizedToAccept(submission, requester))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);	
			else
			{
				submissionService.delete(id, requester);
				redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_SUBMISSION_CANCELED);	
			}
			return "redirect:/manager/submissions";
		}
		catch(SubmissionDeleteException e)
		{
			redirectAttributes.addFlashAttribute("error", e.getMessage());	
			return "redirect:/manager/submissions";
		}
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/{id}/cancel", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> cancelSubmissionBySubmitterOrMaintainer(
			@PathVariable Integer id, 
			Principal principal) 
	{
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			if(submission == null)
				result.put("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
			else if(submission.isAccepted())
				result.put("error", MessageCodes.ERROR_SUBMISSION_ALREADY_ACCEPTED);
			else if(!UserService.isAuthorizedToCancel(submission, requester))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);		
			else
			{
				submissionService.delete(id, requester);
				if(requester.getId() == submission.getUser().getId() && !UserService.isAuthorizedToAccept(submission, requester))
					emailService.sendCanceledSubmissionEmail(submission);
				result.put("success", MessageCodes.SUCCESS_SUBMISSION_CANCELED);					
			}
			return result;
		}
		catch(SubmissionDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		} 
		catch (SendEmailException e) 
		{
			result.put("warning", e.getMessage());
			return result;
		}	
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/{id}", method=RequestMethod.GET)
	public String submissionPage(@PathVariable Integer id, Principal principal, 
			RedirectAttributes redirectAttributes, Model model) 
	{
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		String address = "redirect:/manager/submissions/all";
		if(submission == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
		else if(!UserService.isAuthorizedToCancel(submission, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);		
		else
		{
			model.addAttribute("submission", submission);
			model.addAttribute("role", requester.getRole().getValue());
			address = "submission";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/manager/submissions/{id}/accept", method=RequestMethod.GET)
	public String acceptSubmissionByMaintainer(@PathVariable Integer id, Principal principal, 
			RedirectAttributes redirectAttributes) 
	{
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		try 
		{
			if(submission == null)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
			else if(submission.isAccepted())
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_SUBMISSION_ALREADY_ACCEPTED);
			else if(!UserService.isAuthorizedToAccept(submission, requester))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				
				if(packageService.isHighestVersion(submission.getPackage()))
				{
					submission.getPackage().setActive(true);
					packageService.update(submission.getPackage(), requester);
				}
				else
				{
					submission.setAccepted(true);
					submissionService.update(submission, requester);
					repositoryService.boostRepositoryVersion(submission.getPackage().getRepository(), requester);
					repositoryService.publishRepository(submission.getPackage().getRepository(), requester);
				}	
			}
			return "redirect:/manager/submissions";
		}
		catch(SubmissionEditException | RepositoryEditException | PackageEditException e)
		{
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/manager/submissions";
		}
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/submissions/{id}/accept", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> acceptSubmissionByMaintainerREST(@PathVariable Integer id, 
			Principal principal) 
	{
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		Submission submission = submissionService.findById(id);
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			if(submission == null)
				result.put("error", MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
			else if(submission.isAccepted())
				result.put("warning", MessageCodes.ERROR_SUBMISSION_ALREADY_ACCEPTED);
			else if(!UserService.isAuthorizedToAccept(submission, requester))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);	
			else
			{
				if(packageService.isHighestVersion(submission.getPackage()))
				{
					submission.getPackage().setActive(true);
					packageService.update(submission.getPackage(), requester);
				}
				else
				{
					repositoryService.boostRepositoryVersion(submission.getPackage().getRepository(), requester);
					submission.setAccepted(true);
					repositoryService.publishRepository(submission.getPackage().getRepository(), requester);
					submissionService.update(submission, requester);
				}
				result.put("success", MessageCodes.SUCCESS_SUBMISSION_ACCEPTED);
			}
			return result;
		}
		catch(SubmissionEditException | PackageEditException | RepositoryEditException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/manager/submissions/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> shiftDeleteSubmission(@PathVariable Integer id) 
	{
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			submissionService.shiftDelete(id);
			result.put("success", MessageCodes.SUCCESS_SUBMISSION_CANCELED);					
			return result;
		}
		catch(SubmissionDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		} 
	}
}
