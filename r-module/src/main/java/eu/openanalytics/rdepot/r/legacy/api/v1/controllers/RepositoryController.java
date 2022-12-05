/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
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

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.SynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.legacy.LegacyEventFormatter;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.RepositorySpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.DTOConverter;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.RepositoryV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryDeclarativeModeException;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException;
import eu.openanalytics.rdepot.r.legacy.mediator.LegacyEventSystemMediator;
import eu.openanalytics.rdepot.r.legacy.security.authorization.SecurityMediatorImplV1;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;

@SuppressWarnings("deprecation")
@Controller
@RequestMapping
public class RepositoryController {

	Logger logger = LoggerFactory.getLogger(RepositoryController.class);
	Locale locale = LocaleContextHolder.getLocale();

	@Autowired
	private RRepositoryService repositoryService;

	@Autowired
	private RPackageService packageService;

	@Autowired
	private UserService userService;

	@Autowired
	private RRepositoryValidator repositoryValidator;

	@Autowired
	private RepositoryMaintainerService repositoryMaintainerService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private CranMirrorSynchronizer mirrorService;

	@Autowired
	private SecurityMediatorImplV1 securityMediator;

	@Autowired
	private RStrategyFactory rStrategyFactory;

	@Autowired
	private RRepositoryDeleter rRepositoryDeleter;

	@Autowired
	private RLocalStorage rLocalStorage;

	@Autowired
	private LegacyEventSystemMediator legacyEventSystemMediator;

	@Value("${declarative}")
	private String declarative;

	@InitBinder(value = "repository")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(repositoryValidator);
	}

	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value = { "/manager/repositories" }, method = RequestMethod.GET)
	public String repositoriesPage(Model model, Principal principal) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		model.addAttribute("role", requester.getRole().getValue());

		List<Integer> maintained = new ArrayList<>();

		if (requester.getRole().getValue() == Role.VALUE.ADMIN) {
			maintained = repositoryService.findByDeleted(false).stream().map(r -> r.getId())
					.collect(Collectors.toList());
		} else {
			maintained = repositoryMaintainerService.findByUserWithoutDeleted(requester).stream()
					.map(m -> repositoryService.findById(m.getRepository().getId())).filter(r -> r.isPresent())
					.map(r -> r.get()).map(r -> r.getId()).collect(Collectors.toList());
		}

		List<RRepository> repositoriesEntities = repositoryService.findByDeleted(false);

		model.addAttribute("maintained", maintained);
		model.addAttribute("repositories", DTOConverter.convertRepositories(repositoriesEntities));
		model.addAttribute("disabled", Boolean.valueOf(declarative));
		model.addAttribute("username", requester.getName());

		return "repositories";
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@GetMapping(value = { "/manager/repositories/synchronization/status",
			"/api/manager/repositories/synchronization/status" })
	public @ResponseBody ResponseEntity<List<Map<String, String>>> getSynchronizationStatus(Principal principal) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);

		List<Map<String, String>> response = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); // TODO: add as a configuration property
		// TODO: It would be better to map it automatically by adding JDK 8 support for
		// ObjectMapper
		List<SynchronizationStatus> status = mirrorService.getSynchronizationStatusList();
		status.forEach(s -> {
			RRepository repository = repositoryService.findById(s.getRepositoryId()).orElse(null);

			// authorization
			if (repository != null && securityMediator.isAuthorizedToEdit(repository, requester)) {
				Map<String, String> statusMap = new HashMap<>();
				statusMap.put("repositoryId", String.valueOf(s.getRepositoryId()));
				statusMap.put("pending", String.valueOf(s.isPending()));
				statusMap.put("timestamp", dateFormat.format(s.getTimestamp()));
				if (s.getError().isPresent()) {
					statusMap.put("error", s.getError().get().getMessage());
				} else {
					statusMap.put("error", String.valueOf((Object) null));
				}
				response.add(statusMap);
			}
		});

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = { "/manager/repositories/create",
			"/api/manager/repositories/create" }, method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> createNewRepository(
			@RequestBody RRepository repository, Principal principal, BindingResult bindingResult)
			throws RepositoryDeclarativeModeException, UserUnauthorizedException {
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		HashMap<String, Object> result = new HashMap<>();

		if (Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException();
		} else if (requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException();
		} else {
			repositoryValidator.validate(repository, bindingResult);

			if (bindingResult.hasErrors()) {
				result.put("repository", DTOConverter.convertRepository(repository));
				logger.error(bindingResult.toString());

				String errorMessage = "";
				for (ObjectError error : bindingResult.getAllErrors()) {
					errorMessage += messageSource.getMessage(error.getCode(), null, error.getCode(), locale);
				}

				result.put("error", errorMessage);
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				try {
					rStrategyFactory.createRepositoryStrategy(repository, requester).perform();
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_CREATED, null,
							MessageCodes.SUCCESS_REPOSITORY_CREATED, locale));
				} catch (StrategyFailure e) {
					result.put("error", e.getMessage());
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				}
			}
		}

		return new ResponseEntity<>(result, httpStatus);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value = { "/api/manager/newsfeed/update",
			"/manager/newsfeed/update" }, method = RequestMethod.GET, params = { "date",
					"lastPosition" }, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, List<Map<String, Object>>> updateNewsfeed(Principal principal,
			RedirectAttributes redirectAttributes, @RequestParam("date") String lastRefreshed,
			@RequestParam("lastPosition") int lastPosition) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate lastRefreshedDate;

		try {
			lastRefreshedDate = LocalDate.parse(lastRefreshed, dayFormatter);
		} catch (DateTimeParseException e) {
			return new LinkedHashMap<>();
		}

		Map<LocalDate, List<NewsfeedEvent>> latestEvents = legacyEventSystemMediator
				.findLatestRepositoryEventsByUser(requester, lastRefreshedDate, lastPosition);

		return LegacyEventFormatter.formatEvents(latestEvents);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value = { "/manager/newsfeed/update",
			"/api/manager/newsfeed/update" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, List<Map<String, Object>>> updateNewsfeed(Principal principal,
			RedirectAttributes redirectAttributes) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Map<LocalDate, List<NewsfeedEvent>> events = legacyEventSystemMediator.findRepositoryEventsByUser(requester);

		return LegacyEventFormatter.formatEvents(events);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value = "/manager/newsfeed", method = RequestMethod.GET)
	public String newsfeedPage(Principal principal, Model model, RedirectAttributes redirectAttributes) {
		String address = "newsfeed";
		User user = userService.findByLogin(principal.getName()).orElse(null);
		model.addAttribute("role", user.getRole().getValue());
		return address;
	}

	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value = "/manager/repositories/{id}/packages", method = RequestMethod.GET)
	public String packagesOfRepositoryPage(@PathVariable Integer id, Principal principal, Model model,
			RedirectAttributes redirectAttributes) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		RRepository repository = repositoryService.findById(id).orElse(null);
		String address = "redirect:/manager/repositories";
		if (repository == null)
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(
					MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null, MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
		else if (!securityMediator.isAuthorizedToEdit(repository, requester))
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(
					MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, MessageCodes.ERROR_USER_NOT_AUTHORIZED, locale));
		else {
			List<Integer> maintained = new ArrayList<>();
//			repositoryService.findMaintainedBy(requester, false).forEach(r -> maintained.add(r.getId()));
			repositoryMaintainerService.findByUserWithoutDeleted(requester).stream().map(m -> m.getRepository().getId())
					.collect(Collectors.toList());
			model.addAttribute("maintained", maintained);
			model.addAttribute("repository", DTOConverter.convertRepository(repository));
			model.addAttribute("role", requester.getRole().getValue());
			model.addAttribute("packages",
					DTOConverter.convertPackages(packageService.findAllByRepository(repository)));
			address = "packages";
		}
		return address;
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value = { "/manager/repositories/{id}/edit",
			"/api/manager/repositories/{id}/edit" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> updateRepository(
			@ModelAttribute(value = "repository") RRepository updatedRepository, BindingResult bindingResult,
			@PathVariable Integer id, Principal principal)
			throws RepositoryDeclarativeModeException, UserUnauthorizedException, RepositoryNotFound {
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		HashMap<String, Object> result = new HashMap<>();
		RRepository repository = repositoryService.findById(id).orElse(null);

		if (repository == null || repository.isDeleted()) {
			throw new RepositoryNotFound();
		} else if (Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException();
		} else if (updatedRepository.getId() != id) {
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_INVALID_ID, null,
					MessageCodes.ERROR_REPOSITORY_INVALID_ID, locale));
			httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
		} else if (requester == null || !securityMediator.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException();
		} else {
			repositoryValidator.validate(updatedRepository, bindingResult);

			if (bindingResult.hasErrors()) {
				logger.error(bindingResult.toString());

				String errorMessage = "";
				for (ObjectError error : bindingResult.getAllErrors()) {
					errorMessage += messageSource.getMessage(error.getCode(), null, error.getCode(), locale);
				}

				result.put("error", errorMessage);
				result.put("repository", DTOConverter.convertRepository(updatedRepository));
//				result.put("org.springframework.validation.BindingResult.repository", bindingResult);

				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				try {
					updatedRepository.setPublished(repository.isPublished()); // Temporary fix before we entirely
																				// migrate to the new API
					updatedRepository.setDeleted(repository.isDeleted()); // So is this

					rStrategyFactory.updateRepositoryStrategy(repository, requester, updatedRepository).perform();
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_UPDATED, null,
							MessageCodes.SUCCESS_REPOSITORY_UPDATED, locale));
				} catch (StrategyFailure e) {
					result.put("repository", DTOConverter.convertRepository(repository));
					result.put("error", e.getMessage());

					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				}
			}
		}

		return new ResponseEntity<>(result, httpStatus);
	}

	@PreAuthorize("hasAuthority('user')")
	@GetMapping(value = { "/manager/repositories/list",
			"/api/manager/repositories/list" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryV1Dto> repositories(Principal principal) {
		Specification<RRepository> deletedComponent = RepositorySpecs.isDeleted(false);
		Specification<RRepository> specification = SpecificationUtils.andComponent(null, deletedComponent);
		List<RRepository> entities = repositoryService.findSortedBySpecification(specification, Sort.by(Direction.ASC, "name").and(Sort.by(Direction.ASC, "id")));
		return DTOConverter.convertRepositories(entities);
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
	// TODO: refactor this method
	/**
	 * Not sure how this should work... Principal cannot be null if they have "user"
	 * authority so why do we even bother to check it? And all this logic seems odd
	 * to me, does it assume that we have some sort of a separate error page? Is it
	 * still a thing?
	 */
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value = "/manager/repositories/{name}", method = RequestMethod.GET)
	public String publishedPage(@PathVariable String name, RedirectAttributes redirectAttributes, Model model,
			Principal principal) {
		String address = "error";
		RRepository repository = repositoryService.findByName(name).orElse(null);

		if (principal != null && !(principal.getName().isEmpty() || principal.getName().equals("")
				|| principal.getName().trim().isEmpty())) {
			User requester = userService.findByLogin(principal.getName()).orElse(null);
			if (requester != null) {
				model.addAttribute("role", requester.getRole().getValue());
				if (requester.getRole().getValue() > Role.VALUE.PACKAGEMAINTAINER) {
					address = "redirect:/manager/repositories";
				}
			}
		}
		if (repository == null) {
			if (address.equals("error")) {
				model.addAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			} else {
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			}
		} else {
			model.addAttribute("repository", DTOConverter.convertRepository(repository));
			Set<RPackage> packages = packageService.filterLatest(packageService.findAllByRepository(repository).stream()
					.filter(p -> p.isActive()).collect(Collectors.toSet()));
			List<RPackage> sorted = new LinkedList<>(packages);
			sorted.sort(new Comparator<RPackage>() {

				@Override
				public int compare(RPackage packageA, RPackage packageB) {
					 if(packageA.getRepository().getId() != packageB.getRepository().getId())
				            return packageA.getRepository().getName().compareTo(packageB.getRepository().getName());
				        
					 if(!packageA.getName().equals(packageB.getName()))
			            return packageA.getName().compareTo(packageB.getName());
			        
					 return -1 * packageA.compareTo(packageB);        
				}
			});
			model.addAttribute("packages", DTOConverter.convertPackages(sorted));
			address = "repository-published";
		}

		return address;
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/manager/repositories/{id}/publish",
			"/api/manager/repositories/{id}/publish" }, method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Object>> publishRepository(@PathVariable Integer id,
			Principal principal) throws UserUnauthorizedException, RepositoryNotFound {
		Locale locale = LocaleContextHolder.getLocale();
		Map<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		RRepository repository = repositoryService.findById(id).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;

		if (repository == null) {
			throw new RepositoryNotFound();
		} else if (requester == null || !securityMediator.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException();
		} else {
			RRepository updatedRepository = new RRepository(repository);
			updatedRepository.setPublished(true);
			try {
				rStrategyFactory.updateRepositoryStrategy(repository, requester, updatedRepository).perform();
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, null,
						MessageCodes.SUCCESS_REPOSITORY_PUBLISHED, locale));
			} catch (StrategyFailure e) {
				logger.error(e.getMessage(), e);
				result.put("error", e.getMessage());
			}
		}

		return new ResponseEntity<>(result, httpStatus);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/manager/repositories/{id}/synchronize-mirrors",
			"/api/manager/repositories/{id}/synchronize-mirrors" }, method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, String>> synchronizeWithMirror(@PathVariable Integer id,
			Principal principal) throws RepositoryNotFound, UserUnauthorizedException {
		HashMap<String, String> response = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		RRepository repository = repositoryService.findById(id).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;

		if (repository == null) {
			throw new RepositoryNotFound();
		} else if (requester == null || !securityMediator.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException();
		} else {
			Set<CranMirror> mirrors = mirrorService.findByRepository(repository);

			for (CranMirror mirror : mirrors) {
				mirrorService.synchronize(repository, mirror);
			}

			response.put("success",
					messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_SYNCHRONIZATION_STARTED, null, locale));
		}

		return new ResponseEntity<>(response, httpStatus);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/manager/repositories/{id}/unpublish",
			"/api/manager/repositories/{id}/unpublish" }, method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Object>> unpublishRepository(@PathVariable Integer id,
			Principal principal) throws RepositoryNotFound, UserUnauthorizedException {
		Map<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Locale locale = LocaleContextHolder.getLocale();
		RRepository repository = repositoryService.findById(id).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;

		if (repository == null) {
			throw new RepositoryNotFound();
		} else if (requester == null || !securityMediator.isAuthorizedToEdit(repository, requester)) {
			throw new UserUnauthorizedException();
		} else {
			RRepository updatedRepository = new RRepository(repository);
			updatedRepository.setPublished(false);
			try {
				rStrategyFactory.updateRepositoryStrategy(repository, requester, updatedRepository).perform();
				result.put("success",
						messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_UNPUBLISHED, null, locale));
			} catch (StrategyFailure e) {
				logger.error(e.getMessage(), e);
				result.put("error", e.getMessage());
			}

		}

		return new ResponseEntity<>(result, httpStatus);
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value = { "/manager/repositories/{id}/delete",
			"/api/manager/repositories/{id}/delete" }, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> deleteRepository(@PathVariable Integer id, Principal principal)
			throws RepositoryDeclarativeModeException, UserUnauthorizedException, RepositoryNotFound {
		HashMap<String, String> response = new HashMap<String, String>();
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		RRepository repository = repositoryService.findById(id).orElse(null);

		if (Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException();
		} else if (requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			// TODO: Move to a separate isAuthorized method? It would look more correct
			throw new UserUnauthorizedException();
		} else if (repository == null || repository.isDeleted()) {
			throw new RepositoryNotFound();
		} else {
			RRepository updatedRepository = new RRepository(repository);
			updatedRepository.setDeleted(true);
			try {
				rStrategyFactory.updateRepositoryStrategy(repository, requester, updatedRepository).perform();
				response.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_DELETED, null,
						MessageCodes.SUCCESS_REPOSITORY_DELETED, locale));
			} catch (StrategyFailure e) {
				logger.error(e.getMessage(), e);
				response.put("error", e.getMessage());
			}
		}

		return new ResponseEntity<>(response, httpStatus);
	}

	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/manager/repositories/{id}/sdelete",
			"/api/manager/repositories/{id}/sdelete" }, method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeleteRepository(@PathVariable Integer id)
			throws RepositoryDeclarativeModeException, RepositoryNotFound {
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		HttpStatus httpStatus = HttpStatus.OK;
		RRepository repository = repositoryService.findById(id).orElse(null);

		if (Boolean.valueOf(declarative)) {
			throw new RepositoryDeclarativeModeException();
		} else if (repository == null || !repository.isDeleted()) {
			throw new RepositoryNotFound();
		} else {
			try {
				rRepositoryDeleter.delete(repository);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORY_DELETED, null,
						MessageCodes.SUCCESS_REPOSITORY_DELETED, locale));
			} catch (DeleteEntityException e) {
				logger.error(e.getMessage(), e);
				result.put("error", e.getMessage());
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}
		return new ResponseEntity<>(result, httpStatus);
	}

	@RequestMapping(value = "/manager/repositories/{name}/packages/{packageName}/latest", method = RequestMethod.GET)
	public String publishedPackagePageLatest(@PathVariable String name, @PathVariable String packageName,
			RedirectAttributes redirectAttributes, Model model, Principal principal) {
		return publishedPackagePage(name, packageName, redirectAttributes, model, principal);
	}

	@RequestMapping(value = "/manager/repositories/{name}/packages/{packageName}/{version}", method = RequestMethod.GET)
	public String publishedPackagePage(@PathVariable String name, @PathVariable String packageName,
			@PathVariable String version, RedirectAttributes redirectAttributes, Model model, Principal principal) {
		RRepository repository = repositoryService.findByName(name).orElse(null);
		if (repository == null) {
			model.addAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null,
					MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
			return "error";
		}
		model.addAttribute("repository", DTOConverter.convertRepository(repository));

		Optional<RPackage> packageBag = packageService.findByNameAndVersionAndRepositoryAndDeleted(packageName, version, repository, false);
	
		if (packageBag.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND,
					null, MessageCodes.ERROR_PACKAGE_NOT_FOUND, locale));
			return "redirect:/manager/repositories/" + repository.getName();
		}
		model.addAttribute("packageBag", DTOConverter.convertPackage(packageBag.get()));
		model.addAttribute("vignettes", rLocalStorage.getAvailableVignettes(packageBag.get()));
		model.addAttribute("isManualAvailable", rLocalStorage.isReferenceManualAvailable(packageBag.get()));
		return "package-published";
	}

	@RequestMapping(value = "/manager/repositories/{name}/packages/{packageName}", method = RequestMethod.GET)
	public String publishedPackagePage(@PathVariable String name, @PathVariable String packageName,
			RedirectAttributes redirectAttributes, Model model, Principal principal) {
		Locale locale = LocaleContextHolder.getLocale();
		RRepository repository = repositoryService.findByName(name).orElse(null);
		if (repository == null) {
			model.addAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null,
					MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
			return "error";
		}
		model.addAttribute("repository", DTOConverter.convertRepository(repository));
		List<RPackage> packages = packageService.findAllByNameAndRepository(packageName, repository);
		if (packages == null || packages.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND,
					null, MessageCodes.ERROR_PACKAGE_NOT_FOUND, locale));
			return "redirect:/manager/repositories/" + repository.getName();
		}
		RPackage latest = packages.get(0);
		int size = packages.size();
		for (int i = 1; i < size; i++) {
			RPackage maybeLatest = packages.get(i);
			if (latest.compareTo(maybeLatest) < 0 && maybeLatest.isActive())
				latest = packages.get(i);
		}
		model.addAttribute("packageBag", DTOConverter.convertPackage(latest));
		model.addAttribute("vignettes", rLocalStorage.getAvailableVignettes(latest));
		model.addAttribute("isManualAvailable", rLocalStorage.isReferenceManualAvailable(latest));

		return "package-published";
	}

	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value = "/manager/repositories/{repositoryName}/packages/{packageName}/archive", method = RequestMethod.GET)
	public String packageArchive(@PathVariable String repositoryName, @PathVariable String packageName,
			RedirectAttributes redirectAttributes, Model model, Principal principal) {

		String address = "error";
		RRepository repository = repositoryService.findByName(repositoryName).orElse(null);

		if (principal != null && !(principal.getName().isEmpty() || principal.getName().equals("")
				|| principal.getName().trim().isEmpty())) {
			User requester = userService.findByLogin(principal.getName()).orElse(null);
			if (requester != null) {
				model.addAttribute("role", requester.getRole().getValue());
				if (requester.getRole().getValue() > Role.VALUE.PACKAGEMAINTAINER) {
					address = "redirect:/manager/repositories";
				}
			}

			if (repository == null) {
				if (address.equals("error")) {
					model.addAttribute("error", messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null,
							MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
				} else {
					redirectAttributes.addFlashAttribute("error",
							messageSource.getMessage(MessageCodes.ERROR_REPOSITORY_NOT_FOUND, null,
									MessageCodes.ERROR_REPOSITORY_NOT_FOUND, locale));
				}
			} else {
				model.addAttribute("repository", DTOConverter.convertRepository(repository));
				List<RPackage> packages = packageService.findAllByNameAndRepository(packageName, repository).stream()
						.filter(p -> p.isActive()).collect(Collectors.toList());
				packages.sort(new Comparator<RPackage>() {
					@Override
					public int compare(RPackage o1, RPackage o2) {
						return o1.compareTo(o2) * -1;
					}
				});
				model.addAttribute("packages", DTOConverter.convertPackages(packages));
				address = "package-archive";
			}
		}
		return address;
	}
}
