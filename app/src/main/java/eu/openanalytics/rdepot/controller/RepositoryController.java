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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.exception.RepositoryCreateException;
import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.formatter.RepositoryEventFormatter;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryValidator;
import eu.openanalytics.rdepot.warning.RepositoryAlreadyUnpublishedWarning;

@Controller
@RequestMapping
public class RepositoryController {
	
	Logger logger = LoggerFactory.getLogger(RepositoryService.class);
	Locale locale = LocaleContextHolder.getLocale();
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private RepositoryEventService repositoryEventService;
	
	@Autowired
	private PackageService packageService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RepositoryValidator repositoryValidator;

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private RepositoryEventFormatter repositoryEventFormatter;
	
	@InitBinder(value="repository")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(repositoryValidator);
	}
		
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/repositories"}, method=RequestMethod.GET)
	public String repositoriesPage(Model model, Principal principal)  {
		User requester = userService.findByLogin(principal.getName());
		model.addAttribute("role", requester.getRole().getValue());
		List<Integer> maintained = new ArrayList<>();
		repositoryService.findMaintainedBy(requester, false).forEach(r -> maintained.add(r.getId()));
		model.addAttribute("maintained", maintained);
		model.addAttribute("repositories", repositoryService.findAll());
		
		return "repositories";
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/repositories/create","/api/manager/repositories/create"}, method=RequestMethod.POST)
	public @ResponseBody HashMap<String, Object> createNewRepository(@RequestBody Repository repository,
			Principal principal, BindingResult bindingResult) {
		
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName());
		if(requester == null)
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
		else if(!Objects.equals(requester.getRole().getName(), "admin"))
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
		else {
			repositoryValidator.validate(repository, bindingResult);
			
			if (bindingResult.hasErrors()) {
				result.put("repository", repository);
				logger.error(bindingResult.toString());
				
				String errorMessage = "";
				for(ObjectError error : bindingResult.getAllErrors()) {
					errorMessage += messageSource.getMessage(error.getCode(), null, locale);
				}
				
				result.put("error", errorMessage);
			}
			else {	
				try {
					repositoryService.create(repository, requester);
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_CREATED, null, locale));
				} 
				catch(RepositoryCreateException e) {
					result.put("error", e.getMessage());
				}
					
				return result;	
			}
		}
		
		return result;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value= {"/api/manager/newsfeed/update", "/manager/newsfeed/update"},
					method=RequestMethod.GET,
					params= {"date", "lastPosition"},
					produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LinkedHashMap<String, ArrayList<HashMap<String, String>>> updateNewsfeed(
			Principal principal, RedirectAttributes redirectAttributes,
			@RequestParam("date") String lastRefreshed, @RequestParam("lastPosition") int lastPosition) {
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
		Date lastRefreshedDate;
		try {
			lastRefreshedDate = dayFormatter.parse(lastRefreshed);
		} catch(ParseException e) {
			return new LinkedHashMap<>();
		}
		TreeMap<Date, ArrayList<RepositoryEvent>> latestEvents = repositoryEventService.findLatestByUser(requester, lastRefreshedDate, lastPosition);
		
		return repositoryEventFormatter.formatEvents(latestEvents);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value= {"/manager/newsfeed/update","/api/manager/newsfeed/update"}, method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LinkedHashMap<String, ArrayList<HashMap<String, String>>> updateNewsfeed(Principal principal, RedirectAttributes redirectAttributes) {
		User requester = userService.findByLoginWithMaintainers(principal.getName());
		TreeMap<Date, ArrayList<RepositoryEvent>> events = repositoryEventService.findAllByUser(requester);
		
		return repositoryEventFormatter.formatEvents(events);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/manager/newsfeed", method=RequestMethod.GET)
	public String newsfeedPage(Principal principal, Model model, RedirectAttributes redirectAttributes) {
		String address = "newsfeed";
		User user = userService.findByLogin(principal.getName());
		model.addAttribute("role", user.getRole().getValue());
	    return address;
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/repositories/{id}/packages", method=RequestMethod.GET)
	public String packagesOfRepositoryPage(@PathVariable Integer id, Principal principal, 
			Model model, RedirectAttributes redirectAttributes)
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		String address = "redirect:/manager/repositories";
		if(repository == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
		else if(!userService.isAuthorizedToEdit(repository, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			List<Integer> maintained = new ArrayList<>();
			repositoryService.findMaintainedBy(requester, false).forEach(r -> maintained.add(r.getId()));
			model.addAttribute("maintained", maintained);
			model.addAttribute("repository", repository);
			model.addAttribute("role", requester.getRole().getValue());
			model.addAttribute("packages", packageService.findByRepository(repository));
			address = "packages";
		}
		return address;
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value= {"/manager/repositories/{id}/edit","/api/manager/repositories/{id}/edit"}, method=RequestMethod.POST)
	@ResponseBody
	public HashMap<String, Object> updateRepository(@ModelAttribute(value="repository") @Valid Repository repository,
			BindingResult result, @PathVariable Integer id, Principal principal) 
	{
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> response = new HashMap<>();
		try
		{
			User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
			if(repository.getId() != id)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, locale));
			else if(requester == null)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(!userService.isAuthorizedToEdit(repository, requester))
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else
			{
				repositoryValidator.validate(repository, result);
				if (result.hasErrors())
					response.put("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_EDIT, null, locale));
				else
				{
					repositoryService.evaluateAndUpdate(repository, requester);
					response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_UPDATED, null, locale));
				}
			}
			return response;
		}
		catch(RepositoryEditException | RepositoryNotFound e)	
		{
			response.put("repository", repository);
			response.put("org.springframework.validation.BindingResult.repository", result);
			response.put("error", e.getMessage());
			return response;
		}
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/repositories/list","/api/manager/repositories/list"}, method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Repository> repositories(Principal principal) 
	{
//		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
//		return repositoryService.findMaintainedBy(requester, false);
		return repositoryService.findAll();
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/repositories/{name}", method=RequestMethod.GET)
	public String publishedPage(@PathVariable String name, RedirectAttributes redirectAttributes, Model model, Principal principal) 
	{
		String address = "error";
		Repository repository = repositoryService.findByName(name);
		
		if(principal != null && !(principal.getName().isEmpty() || principal.getName().equals("") || principal.getName().trim().isEmpty()))
		{
			User requester = userService.findByLogin(principal.getName());
			if(requester != null)
			{
				model.addAttribute("role", requester.getRole().getValue());
				if(requester.getRole().getValue() > Role.VALUE.PACKAGEMAINTAINER)
				{
					address = "redirect:/manager/repositories";
				}
			}
		}
		if(repository == null)
		{
			if(address.equals("error"))
			{
				model.addAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			}
			else
			{
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			}
		}
		else
		{
			model.addAttribute("repository", repository);
			List<Package> packages = packageService.findByRepositoryAndActiveAndNewest(repository, true);
			model.addAttribute("packages", packages);
			address = "repository-published";
		}
		
		return address;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value= {"/manager/repositories/{id}/publish","/api/manager/repositories/{id}/publish"}, method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, String> publishRepository(@PathVariable Integer id, Principal principal)
	{	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> response = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		try
		{
			if(requester == null)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(repository == null)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, locale));
			else if(!userService.isAuthorizedToEdit(repository, requester))
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else
			{
				repositoryService.publishRepository(repository, requester);
				response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, null, locale));
			}
			return response;
		}
		catch(RepositoryPublishException e)
		{
			response.put("error", e.getMessage());			
			return response;
		}
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value= {"/manager/repositories/{id}/unpublish","/api/manager/repositories/{id}/unpublish"}, method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, String> unpublishRepository(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> response = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		Repository repository = repositoryService.findById(id);
		try
		{
			if(requester == null)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(repository == null)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, locale));
			else if(!userService.isAuthorizedToEdit(repository, requester))
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else
			{
				repositoryService.unpublishRepository(repository, requester);
				response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_UNPUBLISHED, null, locale));
			}
			return response;
		}
		catch(RepositoryEditException e)
		{
			response.put("error", e.getMessage());
		} catch (RepositoryAlreadyUnpublishedWarning w) {
			response.put("warning", w.getMessage());
		}
		return response;
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value= {"/manager/repositories/{id}/delete","/api/manager/repositories/{id}/delete"}, method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, String> deleteRepository(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> response = new HashMap<String, String>();
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName());
		try
		{
			if(requester == null)
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				response.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else
			{
				repositoryService.delete(id, requester);
				response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_DELETED, null, locale));
			}
			return response;
		}
		catch(RepositoryDeleteException | RepositoryNotFound e)
		{
			response.put("error", e.getMessage());
			return response;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value= {"/manager/repositories/{id}/sdelete","/api/manager/repositories/{id}/sdelete"}, method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, String> shiftDeleteRepository(@PathVariable Integer id)
	{	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			repositoryService.shiftDelete(id);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_DELETED, null, locale));
			return result;
		}
		catch(RepositoryDeleteException | RepositoryNotFound e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@RequestMapping(value="/manager/repositories/{name}/packages/{packageName}/latest", method=RequestMethod.GET)
	public String publishedPackagePageLatest(
		@PathVariable String name, 
		@PathVariable String packageName, 
		RedirectAttributes redirectAttributes, 
		Model model, 
		Principal principal) 
	{
		return publishedPackagePage(name, packageName, redirectAttributes, model, principal);
	}
	
	@RequestMapping(value="/manager/repositories/{name}/packages/{packageName}/{version}", method=RequestMethod.GET)
	public String publishedPackagePage(
		@PathVariable String name, 
		@PathVariable String packageName, 
		@PathVariable String version, 
		RedirectAttributes redirectAttributes, 
		Model model, 
		Principal principal) 
	{
		Repository repository = repositoryService.findByName(name);
		if (repository == null)
		{
			model.addAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			return "error";
		}
		model.addAttribute("repository", repository);
		Package packageBag = packageService.findByNameAndVersionAndRepository(packageName, version, repository);
		if(packageBag == null)
		{
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			return "redirect:/manager/repositories/" + repository.getName();
		}
		model.addAttribute("packageBag", packageBag);
		return "package-published";
	}
	
	@RequestMapping(value="/manager/repositories/{name}/packages/{packageName}", method=RequestMethod.GET)
	public String publishedPackagePage(
		@PathVariable String name, 
		@PathVariable String packageName, 
		RedirectAttributes redirectAttributes, 
		Model model, 
		Principal principal) 
	{
		Locale locale = LocaleContextHolder.getLocale();
		Repository repository = repositoryService.findByName(name);
		if (repository == null)
		{
			model.addAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, locale));
			return "error";
		}
		model.addAttribute("repository", repository);
		List<Package> packages = packageService.findByNameAndRepository(packageName, repository);
		if(packages == null || packages.isEmpty())
		{
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			return "redirect:/manager/repositories/" + repository.getName();
		}
		Package latest = packages.get(0);
		int size = packages.size();
		for (int i = 1; i < size; i++)
		{
			Package maybeLatest = packages.get(i);
			if (latest.compareTo(maybeLatest) < 0 && maybeLatest.isActive())
				latest = packages.get(i);
		}
		model.addAttribute("packageBag", latest);
		return "package-published";
	}
	
//	TODO: why archive is not visible from outside?
	
	@RequestMapping(value="/manager/repositories/{repositoryName}/packages/{packageName}/archive", method=RequestMethod.GET)
	public String packageArchive(@PathVariable String repositoryName, @PathVariable String packageName,
			RedirectAttributes redirectAttributes, Model model, Principal principal)
	{
		
		String address = "error";
		Repository repository = repositoryService.findByName(repositoryName);
		
		if(principal != null && !(principal.getName().isEmpty() || principal.getName().equals("") || principal.getName().trim().isEmpty())) {
			User requester = userService.findByLogin(principal.getName());
			if(requester != null)
			{
				model.addAttribute("role", requester.getRole().getValue());
				if(requester.getRole().getValue() > Role.VALUE.PACKAGEMAINTAINER)
				{
					address = "redirect:/manager/repositories";
				}
			}
			
			if(repository == null)
			{
				if(address.equals("error"))
				{
					model.addAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
				}
				else
				{
					redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
				}
			}
			else
			{
				model.addAttribute("repository", repository);
				List<Package> packages = packageService.findByNameAndRepositoryAndActiveByOrderByVersionDesc(packageName, repository);
				model.addAttribute("packages", packages);
				address = "package-archive";
			}
		}
		return address;
	}	
}
