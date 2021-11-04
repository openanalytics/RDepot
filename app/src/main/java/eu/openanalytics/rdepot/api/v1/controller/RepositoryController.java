/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.api.v1.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
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
import eu.openanalytics.rdepot.exception.RepositoryDeclarativeModeException;
import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.SynchronizeMirrorException;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
import eu.openanalytics.rdepot.formatter.RepositoryEventFormatter;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Mirror;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.SynchronizationStatus;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.CranMirrorService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryValidator;

@Controller
@RequestMapping
public class RepositoryController {
	
	Logger logger = LoggerFactory.getLogger(RepositoryController.class);
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
	
	@Autowired
	private CranMirrorService mirrorService;
	
	@Value("${declarative}")
	private String declarative;
	
	@InitBinder(value="repository")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(repositoryValidator);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/repositories"}, method=RequestMethod.GET)
	public String repositoriesPage(Model model, Principal principal) {
		User requester = userService.findByLogin(principal.getName());
		model.addAttribute("role", requester.getRole().getValue());
		List<Integer> maintained = new ArrayList<>();
		repositoryService.findMaintainedBy(requester, false).forEach(r -> maintained.add(r.getId()));
		List<Repository> repositories = repositoryService.findAll();
		
		model.addAttribute("maintained", maintained);
		model.addAttribute("repositories", repositories);
		model.addAttribute("disabled", Boolean.valueOf(declarative)); //TODO: Give "disabled" field a more informative name
        model.addAttribute("username", requester.getName());

		return "repositories";
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@GetMapping(value= {"/manager/repositories/synchronization/status", "/api/manager/repositories/synchronization/status"})
	public @ResponseBody ResponseEntity<List<Map<String, String>>> getSynchronizationStatus(
			Principal principal) {
		User requester = userService.findByLogin(principal.getName());
		
		List<Map<String, String>> response = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //TODO: add as a configuration property
		//TODO: It would be better to map it automatically by adding JDK 8 support for ObjectMapper
		List<SynchronizationStatus> status = mirrorService.getSynchronizationStatusList();
		status.forEach(s -> {
			Repository repository = repositoryService.findById(s.getRepositoryId());
			
			//authorization
			if(repository != null && userService.isAuthorizedToEdit(repository, requester)) {
				Map<String, String> statusMap = new HashMap<>();
				statusMap.put("repositoryId", String.valueOf(s.getRepositoryId()));
				statusMap.put("pending", String.valueOf(s.isPending()));
				statusMap.put("timestamp", dateFormat.format(s.getTimestamp()));
//				statusMap.put("error", s.getError().get());
				if(s.getError().isPresent()) {
					statusMap.put("error", s.getError().get().getMessage());
				} else {
					statusMap.put("error", String.valueOf((Object)null));
				}
				response.add(statusMap);
			}
		});
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/repositories/create","/api/manager/repositories/create"}, method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> createNewRepository(
			@RequestBody Repository repository, Principal principal, BindingResult bindingResult) 
					throws UserUnauthorizedException, RepositoryDeclarativeModeException, RepositoryCreateException {
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName());
		HttpStatus httpStatus = HttpStatus.OK;
		HashMap<String, Object> result = new HashMap<>();
		
		if(Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException(messageSource, locale);
		} else if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			repositoryValidator.validate(repository, bindingResult);
			
			if (bindingResult.hasErrors()) {
				result.put("repository", repository);
				logger.error(bindingResult.toString());
				
				String errorMessage = "";
				for(ObjectError error : bindingResult.getAllErrors()) {
					errorMessage += messageSource.getMessage(error.getCode(), null, error.getCode(), locale);
				}
				
				result.put("error", errorMessage);
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {	
				try {
					repositoryService.create(repository, requester);
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_CREATED, null, 
							MessageCodes.SUCCESS_REPOSITORY_CREATED, locale));
				} catch(RepositoryCreateException e) {
					throw e;
				}					
			}
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value= {"/api/manager/newsfeed/update", "/manager/newsfeed/update"},
					method=RequestMethod.GET,
					params= {"date", "lastPosition"},
					produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody LinkedHashMap<String, ArrayList<HashMap<String, String>>> updateNewsfeed(
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
	@RequestMapping(value= {"/manager/newsfeed/update","/api/manager/newsfeed/update"}, 
		method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
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
			Model model, RedirectAttributes redirectAttributes)	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		String address = "redirect:/manager/repositories";
		if(repository == null)
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, 
					MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
		else if(!userService.isAuthorizedToEdit(repository, requester))
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, 
					MessageCodes.ERROR_USER_NOT_AUTHORIZED, locale));
		else {
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
	@RequestMapping(value= {"/manager/repositories/{id}/edit","/api/manager/repositories/{id}/edit"}, 
		method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> updateRepository(
			@ModelAttribute(value="repository") @Valid Repository updatedRepository,
			BindingResult bindingResult, @PathVariable Integer id, Principal principal) 
					throws RepositoryDeclarativeModeException, UserUnauthorizedException, RepositoryNotFound {
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		HttpStatus httpStatus = HttpStatus.OK;
		HashMap<String, Object> result = new HashMap<>();
		Repository repository = repositoryService.findByIdAndDeleted(id, false);
		
		if(repository == null) {
			throw new RepositoryNotFound(messageSource, locale, id);
		} else if(Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException(messageSource, locale);
		} else if(updatedRepository.getId() != id) {
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_INVALID_ID, null, 
					MessageCodes.ERROR_REPOSITORY_INVALID_ID, locale));
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} else if(requester == null || !userService.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			repositoryValidator.validate(updatedRepository, bindingResult);
			
			if(bindingResult.hasErrors()) {
				logger.error(bindingResult.toString());
				
				String errorMessage = "";
				for(ObjectError error : bindingResult.getAllErrors()) {
					errorMessage += messageSource.getMessage(error.getCode(), null, error.getCode(), locale);
				}
				
				result.put("error", errorMessage);
				result.put("repository", updatedRepository);
//				result.put("org.springframework.validation.BindingResult.repository", bindingResult);

				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				try {
					updatedRepository.setPublished(repository.isPublished()); //Temporary fix before we entirely migrate to the new API
					updatedRepository.setDeleted(repository.isDeleted()); //So is this
					repositoryService.evaluateAndUpdate(repository, updatedRepository, requester);
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_UPDATED, 
							null, MessageCodes.SUCCESS_REPOSITORY_UPDATED, locale));
				} catch (RepositoryEditException | RepositoryPublishException | RepositoryDeleteException e) {
					result.put("repository", repository);
					result.put("error", e.getMessage());
					
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				} 
			}
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value= {"/manager/repositories/list","/api/manager/repositories/list"}, method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Repository> repositories(Principal principal) {
		return repositoryService.findAll();
	}
	
//	@PreAuthorize("hasAuthority('user')")
//	@RequestMapping(value="/manager/repositories/{name}", method=RequestMethod.GET)
//	public String publishedPage(@PathVariable String name, RedirectAttributes redirectAttributes,
//			Model model, Principal principal) {
//		User requester = userService.findByLogin(principal.getName());
//		Repository repository = repositoryService.findByName(name);
//		String address = "error";
//		
//		if(requester == null) {
//			
//		}
//		return "";
//	}
	//TODO: refactor this method
	/**
	 * Not sure how this should work...
	 * Principal cannot be null if they have "user" authority so why do we even bother to check it?
	 * And all this logic seems odd to me, does it assume that we have some sort of a separate error page?
	 * Is it still a thing?
	 */
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/repositories/{name}", method=RequestMethod.GET)
	public String publishedPage(@PathVariable String name, RedirectAttributes redirectAttributes, 
			Model model, Principal principal) {
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
	@RequestMapping(value= {"/manager/repositories/{id}/publish","/api/manager/repositories/{id}/publish"}, 
		method=RequestMethod.PATCH, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, String>> publishRepository(
			@PathVariable Integer id, Principal principal) 
					throws UserUnauthorizedException, RepositoryNotFound, RepositoryPublishException {	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> response = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Repository repository = repositoryService.findById(id);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(repository == null) {
			throw new RepositoryNotFound(messageSource, locale, id);
		} else if(requester == null || !userService.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			repositoryService.publishRepository(repository, requester);
			response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, null,
					MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, locale));
		}
		
		return new ResponseEntity<>(response, httpStatus);
	}
	
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = {"/manager/repositories/{id}/synchronize-mirrors", 
			"/api/manager/repositories/{id}/synchronize-mirrors"},
			method=RequestMethod.PATCH, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, String>> synchronizeWithMirror(
			@PathVariable Integer id, Principal principal) 
					throws RepositoryNotFound, UserUnauthorizedException, SynchronizeMirrorException {
		HashMap<String, String> response = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName());
		Repository repository = repositoryService.findById(id);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(repository == null) {
			throw new RepositoryNotFound(messageSource, locale, id);
		} else if(requester == null || !userService.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			Set<Mirror> mirrors = mirrorService.findByRepository(repository);
			
			for(Mirror mirror : mirrors) {
				mirrorService.synchronize(repository, mirror);
			}
			
			response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_SYNCHRONIZATION_STARTED, null, locale));
		}
		
		return new ResponseEntity<>(response, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value= {"/manager/repositories/{id}/unpublish","/api/manager/repositories/{id}/unpublish"}, 
		method=RequestMethod.PATCH, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, String>> unpublishRepository(
			@PathVariable Integer id, Principal principal) 
					throws RepositoryNotFound, UserUnauthorizedException, RepositoryEditException {	
		HashMap<String, String> response = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		Repository repository = repositoryService.findById(id);
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			if(repository == null) {
				throw new RepositoryNotFound(messageSource, locale, id);
			} else if(requester == null || !userService.isAuthorizedToEdit(repository, requester)) {
				throw new UserUnauthorizedException(messageSource, locale);
			} else {
				repositoryService.unpublishRepository(repository, requester);
				response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_UNPUBLISHED, null, locale));
			}
		}
		catch(RepositoryEditException e) {
			throw e;
		}
		
		return new ResponseEntity<>(response, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value= {"/manager/repositories/{id}/delete","/api/manager/repositories/{id}/delete"}, 
		method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> deleteRepository(@PathVariable Integer id, Principal principal) 
			throws RepositoryDeclarativeModeException, UserUnauthorizedException, 
			RepositoryNotFound, RepositoryDeleteException {	
		HashMap<String, String> response = new HashMap<String, String>();
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName());
		HttpStatus httpStatus = HttpStatus.OK;
		Repository repository = repositoryService.findByIdAndDeleted(id, false);
		
		if(Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException(messageSource, locale);
		} else if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			//TODO: Move to a separate isAuthorized method? It would look more correct
			throw new UserUnauthorizedException(messageSource, locale);
		} else if(repository == null) {
			throw new RepositoryNotFound(messageSource, locale, id);
		} else {
			repositoryService.delete(repository, requester);
			response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_DELETED, null, 
					MessageCodes.SUCCESS_REPOSITORY_DELETED, locale));
		}
			
		return new ResponseEntity<>(response, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value= {"/manager/repositories/{id}/sdelete","/api/manager/repositories/{id}/sdelete"}, method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeleteRepository(@PathVariable Integer id) 
			throws RepositoryDeclarativeModeException, RepositoryNotFound, RepositoryDeleteException {	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		HttpStatus httpStatus = HttpStatus.OK;
		Repository repository = repositoryService.findByIdAndDeleted(id, true);
		
		if(Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException(messageSource, locale);
		} else if(repository == null) {
			throw new RepositoryNotFound(messageSource, locale, id);
		} else {
			repositoryService.shiftDelete(repository);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_DELETED, null, 
					MessageCodes.SUCCESS_REPOSITORY_DELETED, locale));
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@RequestMapping(value="/manager/repositories/{name}/packages/{packageName}/latest", method=RequestMethod.GET)
	public String publishedPackagePageLatest(
		@PathVariable String name, 
		@PathVariable String packageName, 
		RedirectAttributes redirectAttributes, 
		Model model, 
		Principal principal) {
		return publishedPackagePage(name, packageName, redirectAttributes, model, principal);
	}
	
	@RequestMapping(value="/manager/repositories/{name}/packages/{packageName}/{version}", method=RequestMethod.GET)
	public String publishedPackagePage(
		@PathVariable String name, 
		@PathVariable String packageName, 
		@PathVariable String version, 
		RedirectAttributes redirectAttributes, 
		Model model, 
		Principal principal) {
		Repository repository = repositoryService.findByName(name);
		if (repository == null) {
			model.addAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, 
					MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
			return "error";
		}
		model.addAttribute("repository", repository);
		Package packageBag = packageService.findByNameAndVersionAndRepository(packageName, version, repository);
		if(packageBag == null) {
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND, null, 
					MessageCodes.ERROR_PACKAGE_NOT_FOUND, locale));
			return "redirect:/manager/repositories/" + repository.getName();
		}
		model.addAttribute("packageBag", packageBag);
		model.addAttribute("vignettes", packageService.getAvailableVignettes(packageBag));
		model.addAttribute("isManualAvailable", 
				packageService.getReferenceManualFilename(packageBag).isPresent());
		return "package-published";
	}
	
	@RequestMapping(value="/manager/repositories/{name}/packages/{packageName}", method=RequestMethod.GET)
	public String publishedPackagePage(
		@PathVariable String name, 
		@PathVariable String packageName, 
		RedirectAttributes redirectAttributes, 
		Model model, 
		Principal principal) {
		Locale locale = LocaleContextHolder.getLocale();
		Repository repository = repositoryService.findByName(name);
		if (repository == null) {
			model.addAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, 
					MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
			return "error";
		}
		model.addAttribute("repository", repository);
		List<Package> packages = packageService.findByNameAndRepository(packageName, repository);
		if(packages == null || packages.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", 
					messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND, null, 
							MessageCodes.ERROR_PACKAGE_NOT_FOUND, locale));
			return "redirect:/manager/repositories/" + repository.getName();
		}
		Package latest = packages.get(0);
		int size = packages.size();
		for (int i = 1; i < size; i++) {
			Package maybeLatest = packages.get(i);
			if (latest.compareTo(maybeLatest) < 0 && maybeLatest.isActive())
				latest = packages.get(i);
		}
		model.addAttribute("packageBag", latest);
		model.addAttribute("vignettes", packageService.getAvailableVignettes(latest));
		model.addAttribute("isManualAvailable", 
				packageService.getReferenceManualFilename(latest).isPresent());
		
		return "package-published";
	}
		
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/manager/repositories/{repositoryName}/packages/{packageName}/archive", method=RequestMethod.GET)
	public String packageArchive(@PathVariable String repositoryName, @PathVariable String packageName,
			RedirectAttributes redirectAttributes, Model model, Principal principal) {
		
		String address = "error";
		Repository repository = repositoryService.findByName(repositoryName);
		
		if(principal != null && !(principal.getName().isEmpty() || principal.getName().equals("") || principal.getName().trim().isEmpty())) {
			User requester = userService.findByLogin(principal.getName());
			if(requester != null) {
				model.addAttribute("role", requester.getRole().getValue());
				if(requester.getRole().getValue() > Role.VALUE.PACKAGEMAINTAINER) {
					address = "redirect:/manager/repositories";
				}
			}
			
			if(repository == null) {
				if(address.equals("error")) {
					model.addAttribute("error", 
							messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, 
									MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
				}
				else {
					redirectAttributes.addFlashAttribute("error", 
							messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, 
							MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
				}
			}
			else {
				model.addAttribute("repository", repository);
				List<Package> packages = packageService.findByNameAndRepositoryAndActiveByOrderByVersionDesc(packageName, repository);
				model.addAttribute("packages", packages);
				address = "package-archive";
			}
		}
		return address;
	}	
}
