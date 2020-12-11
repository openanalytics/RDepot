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
 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.GetReferenceManualException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.ReadPackageVignetteException;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeactivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;

/**
 * Controller providing endpoints for package management
 */
@Controller
@RequestMapping(value= {"/manager/packages", "/api/manager/packages"})
public class PackageController {
	
    @Autowired
    private PackageService packageService;
     
    @Autowired
    private UserService userService;
     
    @Autowired
    private RepositoryService repositoryService;
     
    @Autowired
    private PackageEventService packageEventService;
 
    @Autowired
    private MessageSource messageSource;
    
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
        User requester = userService.findByLogin(principal.getName());
        List<Integer> maintained = new ArrayList<>();
        packageService.findMaintainedBy(requester).forEach(p -> maintained.add(p.getId()));
        model.addAttribute("maintained", maintained);
        model.addAttribute("packages", packageService.findAll());
        model.addAttribute("role", requester.getRole().getValue());
        return "packages";
    }
     
    /**
     * This method provides a list of all packages maintained by the user.
     * @param principal represents the user
     * @return list of maintained packages
     */
    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(value="/list", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Package> packages(Principal principal) {
        User requester = userService.findByLogin(principal.getName());
        return packageService.findMaintainedBy(requester);
    }
     
    /**
     * This method provides a list of all deleted packages.
     * @return list of all deleted packages
     */
    @PreAuthorize("hasAuthority('admin')")
    @RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Package> deletedPackages() {
        return packageService.findByDeleted(true);
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
        Package packageBag = packageService.findById(id);
        String address = "redirect:/manager";
        
        //In case the user is logged in, we need to provide role information to display
        //proper elements in the side panel
        if(principal != null && 
           !(principal.getName().isEmpty() || principal.getName().trim().isEmpty())) {
            User requester = userService.findByLogin(principal.getName());
            if(requester != null) {
                model.addAttribute("role", requester.getRole().getValue());
                if(requester.getRole().getValue() > 0)
                    address = "redirect:/manager/packages";
            }
             
        }
        if(packageBag == null) {
            redirectAttributes.addFlashAttribute("error",  
            		new PackageNotFound(messageSource, locale, id).getMessage());
            return address;
        }
        //TODO: set vignettes for packageBag
        
        model.addAttribute("packageBag", packageBag);
         
        Repository repository = repositoryService.findByName(packageBag.getRepository().getName());
        model.addAttribute("repository", repository);
         
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
    public @ResponseBody ResponseEntity<HashMap<String, Object>> events(@PathVariable Integer id, Principal principal) throws PackageNotFound, UserUnauthorizedException {
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        HashMap<String, Object> result = new HashMap<>();
        HttpStatus httpStatus = HttpStatus.OK;
        
        if(packageBag == null) {
            throw new PackageNotFound(messageSource, locale, id);
        }
        else if(requester == null || !userService.isAuthorizedToEdit(packageBag, requester)) {
            throw new UserUnauthorizedException(messageSource, locale);
        }
        else {
            HashMap<Date, TreeSet<PackageEvent>> packageEvents = new HashMap<Date, TreeSet<PackageEvent>>();
            List<Date> packageDates = packageEventService.getUniqueDatesByPackage(packageBag);
 
            for(Date date : packageDates) {
                List<PackageEvent> pEvents = packageEventService.findByDateAndPackage(date, packageBag);
                TreeSet<PackageEvent> sortedTime = new TreeSet<PackageEvent>(new Comparator<PackageEvent>() {
 
                    @Override
                    public int compare(PackageEvent lhs, PackageEvent rhs) {
                        if (lhs.getTime().getTime() > rhs.getTime().getTime())
                            return -1;
                        else if (lhs.getTime().getTime() == rhs.getTime().getTime())
                            return 0;
                        else
                            return 1;
                    }
                     
                });
                sortedTime.addAll(pEvents);
                packageEvents.put(date, sortedTime);
            }
 
            TreeMap<Date, TreeSet<PackageEvent>> sorted = new TreeMap<Date, TreeSet<PackageEvent>>(new Comparator<Date>() {
                @Override
                public int compare(Date lhs, Date rhs) {
                    if (lhs.getTime() > rhs.getTime())
                        return -1;
                    else if (lhs.getTime() == rhs.getTime())
                        return 0;
                    else
                        return 1;
                }
            });
            sorted.putAll(packageEvents);
            result.put("result", sorted);
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
    		@PathVariable String name, @PathVariable String version) throws GetFileInBytesException {
    	Package packageBag = packageService.findById(id);
    	byte[] bytes = null;
    	HttpHeaders httpHeaders = new HttpHeaders();
    	HttpStatus httpStatus = HttpStatus.OK;
    	
    	if(packageBag == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	} else {
			try {
				bytes = packageService.getPackageInBytes(packageBag);
			} catch (FileNotFoundException e) {
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
    		@PathVariable Integer id, @PathVariable String name) throws GetReferenceManualException {
    	Package packageBag = packageService.findById(id);
    	byte[] bytes = null;
    	HttpHeaders httpHeaders = new HttpHeaders();
    	
    	if(packageBag == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	} else {
    		try {
				bytes = packageService.getReferenceManualInBytes(packageBag);
			} catch (FileNotFoundException e) {
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
    	Package packageBag = packageService.findById(id);
    	byte[] bytes = null;
    	
    	if(packageBag == null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	} else {
    		
    		try {
				bytes = packageService.readVignette(packageBag, filename);
			} catch (FileNotFoundException e) {
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
     * @throws UserUnauthorizedException when user did not provide necessary credentials
     * @throws PackageActivateException when package cannot be activated due to internal server error
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/activate", method=RequestMethod.PATCH, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<HashMap<String, String>> activatePackage(
    		@PathVariable Integer id, Principal principal) 
    				throws PackageNotFound, UserUnauthorizedException, PackageActivateException {
    	HashMap<String, String> result = new HashMap<String, String>();
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        
        if(packageBag == null) {
            throw new PackageNotFound(messageSource, locale, id);
        } else if(requester == null || !userService.isAuthorizedToEdit(packageBag, requester)) {
            throw new UserUnauthorizedException(messageSource, locale);
        } else {
            try {
				packageService.activatePackage(packageBag, requester);
	            result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_ACTIVATED, 
	            		null, MessageCodes.SUCCESS_PACKAGE_ACTIVATED, locale));
			} catch (PackageAlreadyActivatedWarning e) {
				result.put("warning", e.getMessage());
			} catch (PackageActivateException e) {
				throw e;
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
     * @throws UserUnauthorizedException 
     * @throws PackageDeactivateException 
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/deactivate", method=RequestMethod.PATCH, produces="application/json")
    public @ResponseBody ResponseEntity<HashMap<String, String>> deactivatePackage(
    		@PathVariable Integer id, Principal principal) throws PackageNotFound, UserUnauthorizedException, PackageDeactivateException {
        HashMap<String, String> result = new HashMap<String, String>();
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        HttpStatus httpStatus = HttpStatus.OK;
        
        if(packageBag == null) {
        	throw new PackageNotFound(messageSource, locale, id);
        } else if(!userService.isAuthorizedToEdit(packageBag, requester)) {
        	throw new UserUnauthorizedException(messageSource, locale);
        } else {
        	try {
				packageService.deactivatePackage(packageBag, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DEACTIVATED,  null, MessageCodes.SUCCESS_PACKAGE_ACTIVATED, locale));
        	} catch (PackageDeactivateException e) {
				throw e;
			} catch (PackageAlreadyDeactivatedWarning w) {
				result.put("warning", w.getMessage());
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
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
    public @ResponseBody ResponseEntity<HashMap<String, String>> deletePackage(@PathVariable Integer id, 
    		Principal principal) throws UserUnauthorizedException, PackageDeleteException, PackageNotFound {
        HashMap<String, String> result = new HashMap<String, String>();
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        HttpStatus httpStatus = HttpStatus.OK;
        
        if(packageBag == null) {
            throw new PackageNotFound(messageSource, locale, id);
        } else if(!userService.isAuthorizedToEdit(packageBag, requester)) {
            throw new UserUnauthorizedException(messageSource, locale);
        } else {
            try {
				packageService.delete(packageBag, requester);
                result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DELETED, 
                		null, MessageCodes.SUCCESS_PACKAGE_DELETED, locale));
			} catch (PackageAlreadyDeletedWarning e) {
				result.put("warning", e.getMessage());
			} catch (PackageDeleteException e) {
				throw e;
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
    public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeletePackage(@PathVariable Integer id) throws PackageNotFound, PackageDeleteException {
        HashMap<String, String> result = new HashMap<String, String>();
        HttpStatus httpStatus = HttpStatus.OK;
        
        Package packageBag = packageService.findByIdAndDeleted(id, true);
        
        if(packageBag == null) {
        	throw new PackageNotFound(messageSource, locale, id);
        } else {
        	try {
                packageService.shiftDelete(packageBag);
                result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DELETED, null, 
                		MessageCodes.SUCCESS_PACKAGE_DELETED, locale));
            }
            catch(PackageDeleteException e) {
                throw e;
            }
        }
        
        return new ResponseEntity<>(result, httpStatus);
    }
 
}