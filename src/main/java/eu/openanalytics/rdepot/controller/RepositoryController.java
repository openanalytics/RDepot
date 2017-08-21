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

import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryValidator;

@Controller
@RequestMapping(value="/manager/repositories")
public class RepositoryController 
{
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private RepositoryEventService repositoryEventService;
	
	@Autowired
	private PackageService packageService;
	
	@Autowired
	private SubmissionService submissionService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RepositoryValidator repositoryValidator;
	
	@InitBinder(value="repository")
	private void initBinder(WebDataBinder binder)
	{
		binder.setValidator(repositoryValidator);
	}
	
	private int placeholder = 9;
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(method=RequestMethod.GET)
	public String repositoriesPage(Model model, Principal principal) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		model.addAttribute("role", requester.getRole().getValue());
		model.addAttribute("repositories", repositoryService.findMaintainedBy(requester));
		return "repositories";
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String newRepositoryPage(Model model) 
	{
		model.addAttribute("repository", new Repository());
		model.addAttribute("role", placeholder);
		return "repository-create";
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/create", method=RequestMethod.POST)
	public String createNewRepository(@ModelAttribute(value="repository") @Valid Repository repository,
			BindingResult result, RedirectAttributes redirectAttributes, Principal principal) 
	{
		User requester = userService.findByLogin(principal.getName());
		if(requester == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
		else if(!Objects.equals(requester.getRole().getName(), "admin"))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			repositoryValidator.validate(repository, result);
			if (result.hasErrors())
			{
				redirectAttributes.addFlashAttribute("repository", repository);
				redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.repository", result);
			}
			else
			{
				repositoryService.create(repository, requester);
				redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_REPOSITORY_CREATED);
				return "redirect:/manager/repositories";	
			}
		}
		return "redirect:/manager/repositories/create";
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.GET)
	public String editRepositoryPage(@PathVariable Integer id, Principal principal, 
			Model model, RedirectAttributes redirectAttributes)
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		String address = "redirect:/manager/repositories";
		if(repository == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		else if(!UserService.isAuthorizedToEdit(repository, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			model.addAttribute("repository", repository);
			model.addAttribute("role", requester.getRole().getValue());
			address = "repository-edit";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/packages", method=RequestMethod.GET)
	public String packagesOfRepositoryPage(@PathVariable Integer id, Principal principal, 
			Model model, RedirectAttributes redirectAttributes)
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		String address = "redirect:/manager/repositories";
		if(repository == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		else if(!UserService.isAuthorizedToEdit(repository, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			model.addAttribute("repository", repository);
			model.addAttribute("role", requester.getRole().getValue());
			model.addAttribute("packages", packageService.findByRepository(repository));
			address = "packages";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public String repositoryPage(@PathVariable Integer id, Principal principal, 
			Model model, RedirectAttributes redirectAttributes)
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		String address = "redirect:/manager/repositories";
		if(repository == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		else if(!UserService.isAuthorizedToEdit(repository, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			HashMap<Date, TreeSet<RepositoryEvent>> events = new HashMap<Date, TreeSet<RepositoryEvent>>();
			List<Date> dates = repositoryEventService.getUniqueDatesByRepository(repository);
			for(Date date : dates)
			{
				List<RepositoryEvent> rEvents = repositoryEventService.findByDateAndRepository(date, repository);
				for(RepositoryEvent rEvent : rEvents)
				{
					if(Objects.equals(rEvent.getChangedVariable(), "added"))
					{
						Package added = packageService.findByIdEvenDeleted(Integer.parseInt(rEvent.getValueAfter()));
						rEvent.setValueAfter(added.toString());
					}
					else if(Objects.equals(rEvent.getChangedVariable(), "submitted"))
					{
						Submission submitted = submissionService.findByIdEvenDeleted(Integer.parseInt(rEvent.getValueAfter()));
						rEvent.setValueAfter(submitted.toString());
					}
					else if(Objects.equals(rEvent.getChangedVariable(), "removed"))
					{
						Package removed = packageService.findByIdEvenDeleted(Integer.parseInt(rEvent.getValueAfter()));
						rEvent.setValueAfter(removed.toString());
					}
				}
				TreeSet<RepositoryEvent> sortedTime = new TreeSet<RepositoryEvent>(new Comparator<RepositoryEvent>()
				{

					@Override
				    public int compare(RepositoryEvent lhs, RepositoryEvent rhs) 
				    {
				        if (lhs.getTime().getTime() > rhs.getTime().getTime())
				            return -1;
				        else if (lhs.getTime() == rhs.getTime())
				            return 0;
				        else
				            return 1;
				    }
					
				});
				sortedTime.addAll(rEvents);
				events.put(date, sortedTime);
			}
			TreeMap<Date, TreeSet<RepositoryEvent>> sorted = new TreeMap<Date, TreeSet<RepositoryEvent>>(new Comparator<Date>() 
			{
			    @Override
			    public int compare(Date lhs, Date rhs) 
			    {
			        if (lhs.getTime() > rhs.getTime())
			            return -1;
			        else if (lhs.getTime() == rhs.getTime())
			            return 0;
			        else
			            return 1;
			    }
			});
			sorted.putAll(events);
			model.addAttribute("repository", repository);
			model.addAttribute("events", sorted);
			model.addAttribute("created", repositoryEventService.getCreatedOn(repository));
			model.addAttribute("role", requester.getRole().getValue());
			address = "repository";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	public String updateRepository(@ModelAttribute(value="repository") @Valid Repository repository,
			BindingResult result, @PathVariable Integer id,
			RedirectAttributes redirectAttributes, Principal principal) 
	{
		try
		{
			User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
			if(repository.getId() != id)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			else if(requester == null)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!UserService.isAuthorizedToEdit(repository, requester))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				repositoryValidator.validate(repository, result);
				if (result.hasErrors())
					throw new RepositoryEditException(result.getFieldError().getCode());					
				else
				{
					repositoryService.update(repository, requester);
					redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_REPOSITORY_UPDATED);
				}
			}
			return "redirect:/manager/repositories";
		}
		catch(RepositoryEditException e)	
		{
			redirectAttributes.addFlashAttribute("repository", repository);
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.repository", result);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/manager/repositories/{id}/edit";
		}
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Repository> repositories(Principal principal) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		return repositoryService.findMaintainedBy(requester);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Repository> deletedRepositories() 
	{
		return repositoryService.findByDeleted(true);
	}
	
	@RequestMapping(value="/{id}/published", method=RequestMethod.GET)
	public String publishedPage(@PathVariable Integer id, RedirectAttributes redirectAttributes, Model model, Principal principal) 
	{
		Repository repository = repositoryService.findById(id);
		String address = "redirect:/manager";
		if(principal != null && !(principal.getName().isEmpty() || principal.getName().equals("") || principal.getName().trim().isEmpty()))
		{
			User requester = userService.findByLogin(principal.getName());
			if(requester != null)
			{
				model.addAttribute("role", requester.getRole().getValue());
				if(requester.getRole().getValue() > 1)
					address = "redirect:/manager/repositories";
			}
			
		}
		if(repository == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		else
		{
			model.addAttribute("repository", repository);
			// We could move this to the .jsp -> repository.packages and if(package.active): add table row
			// = small load boost (less to download, less to query)
			model.addAttribute("packages", packageService.findByRepositoryAndActive(repository, true));
			address = "repository-published";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/publish", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody HashMap<String, String> publishRepository(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		try
		{
			if(requester == null)
				throw new RepositoryEditException(MessageCodes.ERROR_USER_NOT_FOUND);
			else if(repository == null)
				throw new RepositoryEditException(MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			else if(!UserService.isAuthorizedToEdit(repository, requester))
				throw new RepositoryEditException(MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				repositoryService.publishRepository(repository, requester);
				result.put("success", MessageCodes.SUCCESS_REPOSITORY_PUBLISHED);
			}
			return result;
		}
		catch(RepositoryEditException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/unpublish", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody HashMap<String, String> unpublishRepository(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		try
		{
			if(requester == null)
				throw new RepositoryEditException(MessageCodes.ERROR_USER_NOT_FOUND);
			else if(repository == null)
				throw new RepositoryEditException(MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			else if(!UserService.isAuthorizedToEdit(repository, requester))
				throw new RepositoryEditException(MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				repositoryService.unpublishRepository(repository, requester);
				result.put("success", MessageCodes.SUCCESS_REPOSITORY_UNPUBLISHED);
			}
			return result;
		}
		catch(RepositoryEditException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody HashMap<String, String> deleteRepository(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName());
		try
		{
			if(requester == null)
				throw new RepositoryDeleteException(MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				throw new RepositoryDeleteException(MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				repositoryService.delete(id, requester);
				result.put("success", MessageCodes.SUCCESS_REPOSITORY_DELETED);
			}
			return result;
		}
		catch(RepositoryDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody HashMap<String, String> shiftDeleteRepository(@PathVariable Integer id)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			repositoryService.shiftDelete(id);
			result.put("success", MessageCodes.SUCCESS_REPOSITORY_DELETED);
			return result;
		}
		catch(RepositoryDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
}
