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
 
import java.io.File;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.ReadPackageVignetteException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeactivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;
 
@Controller
@RequestMapping(value= {"/manager/packages", "/api/manager/packages"})
public class PackageController 
{
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
     
    @Autowired
    private PackageStorage packageStorage;
     
    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(method=RequestMethod.GET)
    public String packagesPage(Model model, Principal principal) {
        User requester = userService.findByLogin(principal.getName());
        List<Integer> maintained = new ArrayList<>();
        packageService.findMaintainedBy(requester).forEach(p -> maintained.add(p.getId()));
        model.addAttribute("maintained", maintained);
        model.addAttribute("packages", packageService.findAll());
        model.addAttribute("role", requester.getRole().getValue());
        model.addAttribute("username", requester.getName());
        return "packages";
    }
     
    @PreAuthorize("hasAuthority('user')")
    @RequestMapping(value="/list", method=RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Package> packages(Principal principal) {
        User requester = userService.findByLogin(principal.getName());
        return packageService.findMaintainedBy(requester);
    }
     
    @PreAuthorize("hasAuthority('admin')")
    @RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody List<Package> deletedPackages() {
        return packageService.findByDeleted(true);
    }
     
    @RequestMapping(value="/{id}/published", method=RequestMethod.GET)
    public String publishedPage(
        @PathVariable Integer id, 
        Model model, RedirectAttributes redirectAttributes, Principal principal) {
        Package packageBag = packageService.findById(id);
        String address = "redirect:/manager";
        
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
            redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
            return address;
        }
        model.addAttribute("packageBag", packageBag);
         
        Repository repository = repositoryService.findByName(packageBag.getRepository().getName());
        model.addAttribute("repository", repository);
         
        return "package-published";
    }
     
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/events", method=RequestMethod.GET)
    public @ResponseBody HashMap<String, Object> events(@PathVariable Integer id, Principal principal) 
    {
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        Locale locale = LocaleContextHolder.getLocale();
        HashMap<String, Object> result = new HashMap<>();
        if(packageBag == null)
        {
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND, null, locale));
        }
        else if(requester == null)
        {
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
        }
        else if(!userService.isAuthorizedToEdit(packageBag, requester))
        {
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
        }
        else
        {
            HashMap<Date, TreeSet<PackageEvent>> packageEvents = new HashMap<Date, TreeSet<PackageEvent>>();
            List<Date> packageDates = packageEventService.getUniqueDatesByPackage(packageBag);
 
            for(Date date : packageDates)
            {
                List<PackageEvent> pEvents = packageEventService.findByDateAndPackage(date, packageBag);
                TreeSet<PackageEvent> sortedTime = new TreeSet<PackageEvent>(new Comparator<PackageEvent>()
                {
 
                    @Override
                    public int compare(PackageEvent lhs, PackageEvent rhs) 
                    {
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
 
            TreeMap<Date, TreeSet<PackageEvent>> sorted = new TreeMap<Date, TreeSet<PackageEvent>>(new Comparator<Date>() 
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
            sorted.putAll(packageEvents);
            result.put("result", sorted);
        }
        return result;
    }
     
    @RequestMapping(value="/{id}/download/{name}_{version}.tar.gz", method=RequestMethod.GET)
    public @ResponseBody void downloadPage(HttpServletRequest request, HttpServletResponse response,
    		@PathVariable Integer id, @PathVariable String name, @PathVariable String version) 
    				throws PackageStorageException, IOException {
        Package packageBag = packageService.findById(id);
        byte[] bytes = null;
        if(packageBag != null) {
            File packageFile = new File(packageBag.getSource());
            if(packageFile != null && packageFile.exists()) {
                try {
					bytes = packageStorage.getPackageInBytes(packageBag);
				} catch (GetFileInBytesException e) {
					//TODO: Print error message, 
				}
                response.setContentType("application/gzip");
                response.setHeader("Content-Disposition", "attachment; filename= \""
                		+ packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz\"");
                response.getOutputStream().write(bytes);
                response.flushBuffer();
            }
        }           
    }
     
    @RequestMapping(value="/{id}/download/{name}.pdf", method=RequestMethod.GET)
    public @ResponseBody void downloadReferenceManual(
    		HttpServletRequest request, HttpServletResponse response, @PathVariable Integer id, @PathVariable String name)
    {
//        Package packageBag = packageService.findById(id);
//        HttpHeaders headers = new HttpHeaders();
//        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
//        byte[] bytes = null;
//        try
//        {
//            if(packageBag != null)
//            {
//                try {
//					bytes = packageStorage.getReferenceManualFileInBytes(packageBag);
//				} catch (GetFileInBytesException e) {
//					// TODO Print error message
//					e.printStackTrace();
//				}
//                headers.set("Content-Type", "application/pdf");
//                headers.set("Content-Disposition", "attachment; filename= \""+packageBag.getName()+".pdf\"");
//                httpStatus = HttpStatus.OK;
//            }
//            return new ResponseEntity<byte[]>(bytes,headers,httpStatus);
//        }
//        catch(PackageStorageException e)
//        {
//            return new ResponseEntity<byte[]>(bytes,headers,httpStatus);
//        }
    	Package packageBag = packageService.findById(id);
    	byte[] bytes = null;
    	if(packageBag != null) {
    		try {
				bytes = packageStorage.getReferenceManualFileInBytes(packageBag);
				response.setContentType("application/pdf");
	    		response.setHeader("Content-Disposition", "attachment; filename= \""+packageBag.getName()+".pdf\"");
	    		response.getOutputStream().write(bytes);
	    		response.flushBuffer();
			} catch (ManualCreateException | GetFileInBytesException | IOException e) {
				// TODO redirect to error page
				e.printStackTrace();
				return;
			}
    		
    	}
    }
         
    @RequestMapping(value="/{id}/vignettes/{name}.pdf", method=RequestMethod.GET, produces="application/pdf")
    public @ResponseBody ResponseEntity<byte[]> downloadVignettePdf(@PathVariable Integer id, @PathVariable String name)
    {
    	HttpHeaders headers = new HttpHeaders();
    	HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    	byte[] bytes;
    	try {
			bytes = packageService.readVignette(id, name + "pdf");
			if (bytes != null) {
				httpStatus = HttpStatus.OK;
				headers.set("Content-Type", "application/pdf");
				headers.set("Content-Disposition", "attachment; filename= \""+ name +".pdf\"");
			}
		} catch (ReadPackageVignetteException e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			bytes = null;
		}
		return new ResponseEntity<byte[]>(bytes, headers, httpStatus);

//        HttpHeaders headers = new HttpHeaders();
//        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
//        byte[] bytes = packageService.readVignette(id, name + ".pdf");
//        if (bytes != null)
//        {
//            httpStatus = HttpStatus.OK;
//            headers.set("Content-Type", "application/pdf");
//            headers.set("Content-Disposition", "attachment; filename= \""+name+".pdf\"");
//        }
//        return new ResponseEntity<byte[]>(bytes,headers,httpStatus);
    }
     
    @RequestMapping(value="/{id}/vignettes/{name}.html", method=RequestMethod.GET, produces="text/html")
    public @ResponseBody ResponseEntity<byte[]> getVignetteHtml(@PathVariable Integer id, @PathVariable String name) {
    	HttpHeaders headers = new HttpHeaders();
    	HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    	byte[] bytes;
    	try {
			bytes = packageService.readVignette(id, name + ".html");
			if(bytes != null) {
				httpStatus = HttpStatus.OK;
				headers.set("Content-Type", "text/html");
			}
    	} catch (ReadPackageVignetteException e) {
    		httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    		bytes = null;
		}
    	
    	return new ResponseEntity<byte[]>(bytes, headers, httpStatus);
    }
     
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/activate", method=RequestMethod.PUT, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody HashMap<String, String> activatePackage(@PathVariable Integer id, Principal principal) {
        HashMap<String, String> result = new HashMap<String, String>();
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        Locale locale = LocaleContextHolder.getLocale();
       
        if(packageBag == null)
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND, null, locale));
//        else if(packageBag.isActive())
//            result.put("warning", messageSource.getMessage(MessageCodes.WARNING_PACKAGE_ALREADY_ACTIVATED, null, locale));
        else if(!userService.isAuthorizedToEdit(packageBag, requester))
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
        else
        {
            try {
				packageService.activatePackage(packageBag, requester);
	            result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_ACTIVATED, null, locale));
			} catch (PackageAlreadyActivatedWarning e) {
				result.put("warning", e.getMessage());
			} catch (PackageActivateException e) {
				result.put("error", e.getMessage());
			}
        }
        return result;
        
    }
     
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/deactivate", method=RequestMethod.PUT, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody HashMap<String, String> deactivatePackage(@PathVariable Integer id, Principal principal) {
        HashMap<String, String> result = new HashMap<String, String>();
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        Locale locale = LocaleContextHolder.getLocale();
        
        if(packageBag == null)
        	result.put("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND, null, MessageCodes.ERROR_PACKAGE_NOT_FOUND, locale));
        else if(!userService.isAuthorizedToEdit(packageBag, requester))
        	result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, MessageCodes.ERROR_USER_NOT_AUTHORIZED, locale));
        else {
        	try {
				packageService.deactivatePackage(packageBag, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DEACTIVATED,  null, MessageCodes.SUCCESS_PACKAGE_ACTIVATED, locale));
			} catch (PackageDeactivateException e) {
				result.put("error", e.getMessage());
			} catch (PackageAlreadyDeactivatedWarning w) {
				result.put("warning", w.getMessage());
			}
        }
        return result;
         
    }
     
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody HashMap<String, String> deletePackage(@PathVariable Integer id, Principal principal) {
        HashMap<String, String> result = new HashMap<String, String>();
        Package packageBag = packageService.findById(id);
        User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
        Locale locale = LocaleContextHolder.getLocale();

        if(packageBag == null)
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND, null, locale));
        else if(!userService.isAuthorizedToEdit(packageBag, requester))
            result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
        else
        {
            try {
				packageService.delete(id, requester);
                result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DELETED, null, locale));
			} catch (PackageAlreadyDeletedWarning e) {
				result.put("warning", e.getMessage());
			} catch (PackageDeleteException | PackageNotFound e) {
				result.put("error", e.getMessage());
			}
        }
        return result;
    }
     
    @PreAuthorize("hasAuthority('admin')")
    @RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody HashMap<String, String> shiftDeletePackage(@PathVariable Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            packageService.shiftDelete(id);
            result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGE_DELETED, null, locale));
        }
        catch(PackageDeleteException | PackageNotFound e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
 
}