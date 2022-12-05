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
 
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceNotFoundException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.PackageSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.DTOConverter;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.PackageV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException;
import eu.openanalytics.rdepot.r.legacy.mediator.LegacyEventSystemMediator;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;

/**
 * Controller providing endpoints for package management
 */
@Controller
@RequestMapping(value= {"/manager/packages", "/api/manager/packages"})
public class PackageController {
	
    @Autowired
    private RPackageService packageService;
     
    @Autowired
    private UserService userService;
     
    @Autowired
    private RRepositoryService repositoryService;
     
    @Autowired
    private PackageMaintainerService packageMaintainerService;
    
    @Autowired
    private RepositoryMaintainerService repositoryMaintainerService;
 
    @Autowired
    private RLocalStorage rLocalStorage;
    
    @Autowired
    private RStrategyFactory strategyFactory;
    
    @Autowired
    private RPackageDeleter rPackageDeleter;
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private SecurityMediator securityMediator;
    
	@Autowired
    private LegacyEventSystemMediator legacyEventSystemMediator;
    
    Logger logger = LoggerFactory.getLogger(PackageController.class);
    Locale locale = LocaleContextHolder.getLocale();
     
    /**
     * This method provides content for "Packages" site.<br/>
     * It provides the following elements:<br/>
     * maintained - IDs of packages maintained by the user<br/>
     * packages - list of all packages<br/>
     * role - numeric value representing user's privileges
     * 
	 * @param model contains values sent to the user
	 * @param principal represents the user
	 * @return string which represents packages template
     */
    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(method=RequestMethod.GET)
    public String packagesPage(Model model, Principal principal) {
        User requester = userService.findByLogin(principal.getName()).orElse(null);
        Set<Integer> maintained = new HashSet<>();
        
        if(requester.getRole().getValue() == Role.VALUE.ADMIN) {
        	maintained.addAll(packageService.findAll().stream()
        			.map(p -> p.getId()).collect(Collectors.toSet()));
        } else {
        	List<RepositoryMaintainer> repositoryMaintainers = repositoryMaintainerService.findByUserWithoutDeleted(requester);
            repositoryMaintainers.forEach(m -> {
            	maintained.addAll(
            			packageService.findAllByRepository(m.getRepository())
            			.stream().map(p -> p.getId())
            			.collect(Collectors.toSet()));
            });
            
            
            List<PackageMaintainer> packageMaintainers = packageMaintainerService.findByUser(requester);
            packageMaintainers.forEach(m -> {
            	maintained.addAll(packageService.findAllByNameAndRepository(m.getPackageName(), m.getRepository())
            	.stream().map(p -> p.getId()).collect(Collectors.toSet()));
            });
        }
        
        model.addAttribute("maintained", maintained);
        model.addAttribute("packages", DTOConverter.convertPackages(packageService.findAllNonDeletedAndAccepted()
        		.stream()
//        		.filter(p -> p.getSubmission().getState().equals(SubmissionState.ACCEPTED))
        		.collect(Collectors.toList())));
        model.addAttribute("role", requester.getRole().getValue());
        return "packages";
    }
     
    /**
     * This method provides a list of all packages.
     * @param principal represents the user
     * @param repository name (optional)
     * @return list of maintained packages
     */
    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<PackageV1Dto> packages(Principal principal, @RequestParam(required = false) Optional<String> repositoryName) {
              
        Specification<RPackage> deletedComponent = PackageSpecs.isDeleted(false);
        Specification<RPackage> stateComponent = PackageSpecs.ofSubmissionState(SubmissionState.ACCEPTED);
        Specification<RPackage> specification = SpecificationUtils.andComponent(null, deletedComponent);
        specification = SpecificationUtils.andComponent(specification, stateComponent);
        
        if(repositoryName.isPresent()) {
        	Optional<RRepository> repository = repositoryService.findByName(repositoryName.get());
            if(repository.isPresent()) {
            	Specification<RPackage> repositoryComponent = PackageSpecs.ofRepository(repository.get());
            	specification = SpecificationUtils.andComponent(specification, repositoryComponent);
            }
        }
    
    	List<RPackage> packageEntities = packageService.findSortedBySpecification(specification, Sort.by(Direction.ASC, "name").and(Sort.by(Direction.ASC, "id")));
    	return DTOConverter.convertPackages(packageEntities);
    }
     
    /**
     * This method provides a list of all deleted packages.
     * @return list of all deleted packages
     */
    @PreAuthorize("hasAuthority('admin')")
    @RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<PackageV1Dto> deletedPackages() {
    	List<RPackage> packageEntities = packageService.findByDeleted(Pageable.unpaged(), true).toList();
    	return DTOConverter.convertPackages(packageEntities);    
    }
     
    /**
     * This method provides content for package's published page.<br/>
     * It provides the following elements:<br/>
     * role - numeric value representing user's privileges<br/>
     * packageBag - package object</br>
     * repository - repository object<br/>
     * @param id package ID
     * @param model contains values sent to the user
     * @param redirectAttributes useful when need to redirect
     * @param principal represents the user
     * @return string representing package-published template or redirect url
     */
    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(value="/{id}/published", method=RequestMethod.GET)
    public String publishedPage(
        @PathVariable Integer id, 
        Model model, RedirectAttributes redirectAttributes, Principal principal) {
        RPackage packageBag = packageService.findById(id).orElse(null);
        String address = "redirect:/manager";
        
        //In case the user is logged in, we need to provide role information to display
        //proper elements in the side panel
        if(principal != null && 
           !(principal.getName().isEmpty() || principal.getName().trim().isEmpty())) {
            User requester = userService.findByLogin(principal.getName()).orElse(null);
            if(requester != null) {
                model.addAttribute("role", requester.getRole().getValue());
                if(requester.getRole().getValue() > 0)
                    address = "redirect:/manager/packages";
            }
             
        }
        if(packageBag == null) {
            redirectAttributes.addFlashAttribute("error",  
            		new PackageNotFound(messageSource, locale).getMessage());
            return address;
        }
        //TODO: set vignettes for packageBag
        
        boolean isManualAvailable = true;
        
        try {
        	rLocalStorage.getReferenceManual(packageBag);
        } catch(Exception e) {
        	isManualAvailable = false;
        }
        model.addAttribute("packageBag", DTOConverter.convertPackage(packageBag));
        model.addAttribute("vignettes", rLocalStorage.getAvailableVignettes(packageBag));
        model.addAttribute("isManualAvailable", isManualAvailable);
         
        RRepository repository = repositoryService.findByName(packageBag.getRepository().getName()).orElse(null);
        model.addAttribute("repository", DTOConverter.convertRepository(repository));
         
        return "package-published";
    }
     
    /**
     * This method returns events related to a particular package.<br/>
     * Events are stored under "result" field.<br/>
     * In case of error, full message is stored under "error" field.
     * @param id package ID
     * @param principal represent the user
     * @return sorted list of events or error in case of failure
     * @throws PackageNotFound when package of a given ID does not exist in the database
     * @throws UserUnauthorizedException when user did not provide necessary credentials
     */
	@PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/events", method=RequestMethod.GET)
    public @ResponseBody ResponseEntity<Map<String, Object>> events(@PathVariable Integer id, Principal principal) throws PackageNotFound, UserNotAuthorized {
        RPackage packageBag = packageService.findById(id).orElse(null);
        User requester = userService.findByLogin(principal.getName()).orElse(null);
        Map<String, Object> result = new HashMap<>();
        HttpStatus httpStatus = HttpStatus.OK;
        
        if(packageBag == null) {
            throw new PackageNotFound(messageSource, locale);
        }
        else if(requester == null || !securityMediator.isAuthorizedToEdit(packageBag, requester)) {
            throw new UserNotAuthorized(messageSource, locale);
        }
        else {//TODO: prepare events for api v1
            result.put("result", legacyEventSystemMediator.findPackageEventsByUserAndPackage(requester, packageBag));
        }
        return new ResponseEntity<>(result, httpStatus);
    }
     
    /**
     * This method provides a package binary file to download.
     * @param id package ID
     * @param name package name
     * @param version package version
     * @throws GetFileInBytesException if package file cannot be provided
     */
    @RequestMapping(value="/{id}/download/{name}_{version}.tar.gz", method=RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> downloadPage(@PathVariable Integer id, 
    		@PathVariable String name, @PathVariable String version) {
    	RPackage packageBag = packageService.findById(id).orElse(null);
    	byte[] bytes = null;
    	HttpHeaders httpHeaders = new HttpHeaders();
    	HttpStatus httpStatus = HttpStatus.OK;
    	
    	if(packageBag == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	} else {
			try {
				bytes = rLocalStorage.getPackageInBytes(packageBag);
			} catch (SourceNotFoundException e) {
	    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			httpHeaders.set("Content-Type", "application/gzip");
			httpHeaders.set("Content-Disposition", "attachment; filename= \""
            		+ packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz\"");
			
			return new ResponseEntity<>(bytes, httpHeaders, httpStatus);
    	}    	
    }
     
    /**
     * This method provides reference manual for a specific package.
     * @param request
     * @param response stores manual file
     * @param id package ID
     * @param name manual filename
     * @throws GetReferenceManualException 
     * @throws GetFileInBytesException 
     * @throws ManualCreateException 
     * @throws IOException
     */
    @RequestMapping(value="/{id}/download/{name}.pdf", method=RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> downloadReferenceManual(
    		@PathVariable Integer id, @PathVariable String name) {
    	RPackage packageBag = packageService.findById(id).orElse(null);
    	byte[] bytes = null;
    	HttpHeaders httpHeaders = new HttpHeaders();
    	
    	if(packageBag == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	} else {
    		try {
				bytes = rLocalStorage.getReferenceManual(packageBag);
			} catch (GetReferenceManualException e) {
	    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
    		httpHeaders.setContentType(MediaType.APPLICATION_PDF);
    		httpHeaders.set("Content-Disposition",
    				"attachment; filename= \""+packageBag.getName()+".pdf\"");
    		
    		return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    	}
    }
    
    private ResponseEntity<byte[]> downloadVignette(Integer id, String filename, MediaType mediaType) 
    		throws ReadPackageVignetteException {
    	HttpHeaders httpHeaders = new HttpHeaders();
    	RPackage packageBag = packageService.findById(id).orElse(null);
    	byte[] bytes = null;
    	
    	if(packageBag == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	} else {
    		
    		try {
				bytes = rLocalStorage.readVignette(packageBag, filename);
			} catch (ReadPackageVignetteException e) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
    		
    		httpHeaders.setContentType(mediaType);
    		httpHeaders.set("Content-Disposition", "attachment; filename= \""+filename+"\"");
    		
    		return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    	}
    }
     
         
    /**
     * This method provides package vignette PDF file to download.
     * @param id Package ID
     * @param name filename
     * @return 
     * @throws ReadPackageVignetteException 
     */
    @RequestMapping(value="/{id}/vignettes/{name}.pdf", method=RequestMethod.GET, produces="application/pdf")
    public @ResponseBody ResponseEntity<byte[]> downloadVignettePdf(@PathVariable Integer id, 
    		@PathVariable String name) throws ReadPackageVignetteException {
    	return downloadVignette(id, name + ".pdf", MediaType.APPLICATION_PDF);
    }
    
    @RequestMapping(value="/{id}/vignettes/{name}.html", method=RequestMethod.GET, produces="text/html")
    public @ResponseBody ResponseEntity<byte[]> downloadVignetteHtml(
    		@PathVariable Integer id, @PathVariable String name) 
    		throws ReadPackageVignetteException {
    	return downloadVignette(id, name + ".html", MediaType.TEXT_HTML);
    }
     
    /**
     * This method activates a package.
     * @param id package ID
     * @param principal represents the user
     * @return response entity with JSON object containing success, error or warning message under the respective fields
     * @throws PackageNotFound when package of a given ID does not exist in the database
     * @throws UserNotAuthorized 
     * @throws UserUnauthorizedException when user did not provide necessary credentials
     * @throws PackageActivateException when package cannot be activated due to internal server error
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/activate", method=RequestMethod.PATCH, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<HashMap<String, String>> activatePackage(
    		@PathVariable Integer id, Principal principal) 
    				throws PackageNotFound, UserNotAuthorized {
    	HashMap<String, String> result = new HashMap<String, String>();
        RPackage packageBag = packageService.findById(id).orElse(null);
        User requester = userService.findByLogin(principal.getName()).orElse(null);
        
        if(packageBag == null) {
            throw new PackageNotFound(messageSource, locale);
        } else if(requester == null || !securityMediator.isAuthorizedToEdit(packageBag, requester)) {
            throw new UserNotAuthorized(messageSource, locale);
        } else {
            try {
            	RPackage updated = new RPackage(packageBag);
            	updated.setActive(true);
            	strategyFactory.updatePackageStrategy(packageBag, requester, updated).perform();
            	
	            result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_ACTIVATED, 
	            		null, MessageCodes.SUCCESS_PACKAGE_ACTIVATED, locale));
			} catch (StrategyFailure e) {
				logger.error(e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
        }
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
     
    /**
     * This method deactivates a package.
     * @param id package ID
     * @param principal represents the user
     * @return response entity with JSON object containing success, error or warning message under the respective fields
     * @throws PackageNotFound 
     * @throws UserNotAuthorized 
     * @throws UserUnauthorizedException 
     * @throws PackageDeactivateException 
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/deactivate", method=RequestMethod.PATCH, produces="application/json")
    public @ResponseBody ResponseEntity<HashMap<String, String>> deactivatePackage(
    		@PathVariable Integer id, Principal principal) throws PackageNotFound, UserNotAuthorized {
        HashMap<String, String> result = new HashMap<String, String>();
        RPackage packageBag = packageService.findById(id).orElse(null);
        User requester = userService.findByLogin(principal.getName()).orElse(null);
        HttpStatus httpStatus = HttpStatus.OK;
        
        if(packageBag == null) {
        	throw new PackageNotFound(messageSource, locale);
        } else if(!securityMediator.isAuthorizedToEdit(packageBag, requester)) {
        	throw new UserNotAuthorized(messageSource, locale);
        } else {
        	try {
        		RPackage updated = new RPackage(packageBag);
            	updated.setActive(false);
            	strategyFactory.updatePackageStrategy(packageBag, requester, updated).perform();
            	
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DEACTIVATED,  null, MessageCodes.SUCCESS_PACKAGE_ACTIVATED, locale));
        	} catch (StrategyFailure e) {
        		logger.error(e.getMessage(), e);
        		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
        }
        return new ResponseEntity<>(result, httpStatus);
         
    }
     
    /**
     * This method deletes a package. <br/>
     * If you also want to remove the deleted package from the file system, use
     * {@link #shiftDeletePackage(Integer, Principal)}.<br/>
     * @param id package ID
     * @param principal represents the user
     * @return response entity with JSON object containing success, error or warning message under the respective fields
     * @throws UserUnauthorizedException 
     * @throws PackageDeleteException 
     * @throws PackageNotFound 
     * @throws UserNotAuthorized 
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
    public @ResponseBody ResponseEntity<HashMap<String, String>> deletePackage(@PathVariable Integer id, 
    		Principal principal) throws PackageNotFound, UserNotAuthorized {
        HashMap<String, String> result = new HashMap<String, String>();
        RPackage packageBag = packageService.findById(id).orElse(null);
        User requester = userService.findByLogin(principal.getName()).orElse(null);
        HttpStatus httpStatus = HttpStatus.OK;
        
        if(packageBag == null) {
            throw new PackageNotFound(messageSource, locale);
        } else if(!securityMediator.isAuthorizedToEdit(packageBag, requester)) {
            throw new UserNotAuthorized(messageSource, locale);
        } else {
            try {
            	RPackage updated = new RPackage(packageBag);
            	updated.setDeleted(true);
            	strategyFactory.updatePackageStrategy(packageBag, requester, updated).perform();
            	
                result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DELETED, 
                		null, MessageCodes.SUCCESS_PACKAGE_DELETED, locale));
			} catch (StrategyFailure e) {
				logger.error(e.getMessage(), e);
        		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
        }
        return new ResponseEntity<>(result, httpStatus);
    }
     
    /**
     * This method deleted a package from the file system.<br/>
     * Before you can delete package files, you first need to use {@link #deletePackage(Integer, Principal)} method.<br/>
     * @param id package ID
     * @return response entity with JSON object containing success or error message under the respective fields
     * @throws PackageNotFound 
     * @throws PackageDeleteException 
     */
    @PreAuthorize("hasAuthority('admin')")
    @RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
    public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeletePackage(
    		@PathVariable Integer id) throws PackageNotFound {
        HashMap<String, String> result = new HashMap<String, String>();
        HttpStatus httpStatus = HttpStatus.OK;
        
        RPackage packageBag = packageService.findById(id).orElse(null);
        
        if(packageBag == null || !packageBag.isDeleted()) {
        	throw new PackageNotFound(messageSource, locale);
        } else {
        	try {
        		rPackageDeleter.delete(packageBag);
                result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DELETED, null, 
                		MessageCodes.SUCCESS_PACKAGE_DELETED, locale));
            }
            catch(DeleteEntityException e) {
            	logger.error(e.getMessage(), e);
        		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        return new ResponseEntity<>(result, httpStatus);
    }
    
    @RequestMapping(value="/{id}/vignettes", method=RequestMethod.GET, produces="application/json")
    public @ResponseBody ResponseEntity<List<Vignette>> getVignetteLinks(@PathVariable Integer id) {
    	List<Vignette> result = new ArrayList<Vignette>();
    	HttpStatus httpStatus = HttpStatus.OK;
    	
    	RPackage packageBag = packageService.findById(id).orElse(null);
    	
    	if(packageBag == null || packageBag.isDeleted()) {
    		httpStatus = HttpStatus.NOT_FOUND;
    	} else {
    		result = rLocalStorage.getAvailableVignettes(packageBag);
    	}
    	
    	return new ResponseEntity<>(result, httpStatus);
    }
    
    @RequestMapping(value="/{id}/manual", method=RequestMethod.GET, produces="application/json")
    public @ResponseBody ResponseEntity<Map<String, String>> getReferenceManualFilename(@PathVariable Integer id) {
    	Map<String, String> result = new HashMap<String, String>();
    	HttpStatus httpStatus = HttpStatus.OK;
    	
    	RPackage packageBag = packageService.findById(id).orElse(null);
    	
    	if(packageBag == null || packageBag.isDeleted()) {
    		httpStatus = HttpStatus.NOT_FOUND;
    	} else {
    		Boolean available = true;
    		try {
    			rLocalStorage.getReferenceManual(packageBag);
    		} catch(GetReferenceManualException e) {
    			available = false;
    		}
    		
    		Optional<String> filename = Optional.empty();
    		
    		if(available)
    			filename = Optional.of(packageBag.getName() + ".pdf");
    		
    		result.put("isAvailable", Boolean.toString(available));
    		result.put("filename", filename.orElse(""));
    	}
    	
    	return new ResponseEntity<>(result, httpStatus);
    }
 
}