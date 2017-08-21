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

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageValidationException;
import eu.openanalytics.rdepot.exception.PackageValidationWarning;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.PackageValidator;

@Controller
@RequestMapping(value="/manager/packages")
public class PackageController 
{
	@Autowired
	private PackageService packageService;
	
	@Autowired
	private SubmissionService submissionService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private PackageValidator packageValidator;
	
	@Autowired
	private PackageEventService packageEventService;
	
	@Autowired
	private RepositoryEventService repositoryEventService;
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(method=RequestMethod.GET)
	public String packagesPage(Model model, Principal principal) 
	{
		User requester = userService.findByLogin(principal.getName());
		model.addAttribute("packages", packageService.findMaintainedBy(requester));
		model.addAttribute("role", requester.getRole().getValue());
		return "packages";
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/list", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Package> packages(Principal principal) 
	{
		User requester = userService.findByLogin(principal.getName());
		return packageService.findMaintainedBy(requester);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<Package> deletedPackages() 
	{
		return packageService.findByDeleted(true);
	}
	
	@RequestMapping(value="/{id}/published", method=RequestMethod.GET)
	public String publishedPage(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) 
	{
		Package packageBag = packageService.findById(id);
		if(packageBag == null)
		{
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			return "redirect:/manager/packages";
		}
		model.addAttribute("packageBag", packageBag);
		return "package-published";
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}/events", method=RequestMethod.GET)
	public @ResponseBody HashMap<String, Object> events(@PathVariable Integer id,	Principal principal) 
	{
		Package packageBag = packageService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		HashMap<String, Object> result = new HashMap<>();
		if(packageBag == null)
		{
			result.put("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		}
		else if(requester == null)
		{
			result.put("error", MessageCodes.ERROR_USER_NOT_FOUND);
		}
		else if(!UserService.isAuthorizedToEdit(packageBag, requester))
		{
			result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		}
		else
		{
			HashMap<Date, TreeSet<PackageEvent>> packageEvents = new HashMap<Date, TreeSet<PackageEvent>>();
			HashMap<Date, TreeSet<RepositoryEvent>> repositoryEvents = new HashMap<Date, TreeSet<RepositoryEvent>>();
			List<Date> packageDates = packageEventService.getUniqueDatesByPackage(packageBag);
			List<Date> repositoryDates = repositoryEventService.getUniqueDatesByPackage(packageBag);
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
				        else if (lhs.getTime() == rhs.getTime())
				            return 0;
				        else
				            return 1;
				    }
					
				});
				sortedTime.addAll(pEvents);
				packageEvents.put(date, sortedTime);
			}
			for(Date date : repositoryDates)
			{
				List<RepositoryEvent> rEvents = repositoryEventService.findByDateAndPackage(date, packageBag);
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
				repositoryEvents.put(date, sortedTime);
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
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public String packagePage(@PathVariable Integer id, Model model, 
			RedirectAttributes redirectAttributes, Principal principal) 
	{
		Package packageBag = packageService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		String address = "redirect:/manager/packages";
		if(packageBag == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		else if(requester == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
		else if(!UserService.isAuthorizedToEdit(packageBag, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			model.addAttribute("created", packageEventService.getCreatedOn(packageBag));
			model.addAttribute("packageBag", packageBag);
			model.addAttribute("role", requester.getRole().getValue());
			HashMap<Date, TreeSet<PackageEvent>> events = new HashMap<Date, TreeSet<PackageEvent>>();
			List<Date> dates = packageEventService.getUniqueDatesByPackage(packageBag);
			for(Date date : dates)
			{
				List<PackageEvent> pEvents = packageEventService.findByDateAndPackage(date, packageBag);
				for(PackageEvent pEvent : pEvents)
				{
					if(Objects.equals(pEvent.getChangedVariable(), "added"))
					{
						Package added = packageService.findByIdEvenDeleted(Integer.parseInt(pEvent.getValueAfter()));
						pEvent.setValueAfter(added.toString());
					}
					else if(Objects.equals(pEvent.getChangedVariable(), "submitted"))
					{
						Submission submitted = submissionService.findByIdEvenDeleted(Integer.parseInt(pEvent.getValueAfter()));
						pEvent.setValueAfter(submitted.toString());
					}
					else if(Objects.equals(pEvent.getChangedVariable(), "removed"))
					{
						Package removed = packageService.findByIdEvenDeleted(Integer.parseInt(pEvent.getValueAfter()));
						pEvent.setValueAfter(removed.toString());
					}
				}
				TreeSet<PackageEvent> sortedTime = new TreeSet<PackageEvent>(new Comparator<PackageEvent>()
				{

					@Override
				    public int compare(PackageEvent lhs, PackageEvent rhs) 
				    {
				        if (lhs.getTime().getTime() > rhs.getTime().getTime())
				            return -1;
				        else if (lhs.getTime() == rhs.getTime())
				            return 0;
				        else
				            return 1;
				    }
					
				});
				sortedTime.addAll(pEvents);
				events.put(date, sortedTime);
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
			sorted.putAll(events);
			model.addAttribute("events", sorted);
			address = "package";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.GET)
	public String editPackagePage(@PathVariable Integer id, Model model, 
			RedirectAttributes redirectAttributes, Principal principal) 
	{
		Package packageBag = packageService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		String address = "redirect:/manager/packages";
		if(packageBag == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
		else if(requester == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
		else if(!UserService.isAuthorizedToEdit(packageBag, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			model.addAttribute("packageBag", packageBag);
			// Can you change a repository? I'd rather not... 
			// otherwise add attributes based on role? repositories, users, etc.
			//model.addAttribute("repositories", repositoryService.packageBag);
			model.addAttribute("role", requester.getRole().getValue());
			address = "package-edit";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	public String updatePackage(@ModelAttribute("packageBag") @Valid Package packageBag, 
			RedirectAttributes redirectAttributes, @PathVariable Integer id, 
			Principal principal, BindingResult result) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		try 
		{
			if(packageBag.getId() != id)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			else if(requester == null)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!UserService.isAuthorizedToEdit(packageBag, requester))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageValidator.validate(packageBag);
				packageService.update(packageBag, requester);
				redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_PACKAGE_UPDATED);
			}
			return "redirect:/manager/packages";
		}
		catch (PackageValidationException | PackageValidationWarning | PackageEditException | RepositoryEditException e) 
		{
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addFlashAttribute("packageBag", packageBag);
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.packageBag", result);
			return "redirect:/manager/packages/{id}/edit";
		}
	}
	
	@RequestMapping(value="/{id}/download/{name}_{version}.tar.gz", method=RequestMethod.GET)
	public @ResponseBody void downloadPage(HttpServletRequest request, HttpServletResponse response, @PathVariable Integer id, @PathVariable String name, @PathVariable String version) throws IOException 
	{
		Package packageBag = packageService.findById(id);
		// HttpHeaders headers = new HttpHeaders();
		// HttpStatus httpStatus = HttpStatus.NOT_FOUND;
		byte[] bytes = null;
		if(packageBag != null)
		{
			File packageFile = new File(packageBag.getSource());
			if(packageFile != null && packageFile.exists())
			{
				FileSystemResource file = new FileSystemResource(packageFile);
		    	response.setContentType("application/gzip");
		    	//headers.set("Content-Encoding", "gzip");
		    	//headers.set("Content-Encoding", "x-gzip");
		    	response.setHeader("Content-Disposition", "attachment; filename= \""+packageBag.getName()+"_"+packageBag.getVersion()+".tar.gz\"");
				bytes = IOUtils.toByteArray(file.getInputStream());
				response.getOutputStream().write(bytes);
			    response.flushBuffer();
				// httpStatus = HttpStatus.OK;
			}
		}			
		// return new ResponseEntity<byte[]>(bytes, headers, httpStatus);
	}
	
	@RequestMapping(value="/{id}/download/{name}.pdf", method=RequestMethod.GET, produces="application/pdf")
	public @ResponseBody ResponseEntity<byte[]> downloadReferenceManual(@PathVariable Integer id, @PathVariable String name)
	{
		Package packageBag = packageService.findById(id);
		HttpHeaders headers = new HttpHeaders();
		HttpStatus httpStatus = HttpStatus.NOT_FOUND;
		byte[] bytes = null;
		try
		{
			if(packageBag != null)
			{
				String manualPath = new File(packageBag.getSource()).getParent() + "/" + packageBag.getName() + "/" + packageBag.getName() + ".pdf";
				File manualFile = new File(manualPath);
				if(manualFile == null || !manualFile.exists())
				{
					packageService.createManuals(packageBag);
					manualFile = new File(manualPath);
				}
				if(manualFile != null && manualFile.exists())
				{
					FileSystemResource file = new FileSystemResource(manualFile);
			    	headers.set("Content-Type", "application/pdf");
			    	headers.set("Content-Disposition", "attachment; filename= \""+packageBag.getName()+".pdf\"");
					bytes = IOUtils.toByteArray(file.getInputStream());
					httpStatus = HttpStatus.OK;
				}
			}
			return new ResponseEntity<byte[]>(bytes,headers,httpStatus);
		}
		catch(ManualCreateException | IOException e)
		{
			return new ResponseEntity<byte[]>(bytes,headers,httpStatus);
		}
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}/activate", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> activatePackage(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		Package packageBag = packageService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		try
		{
			if(packageBag == null)
				result.put("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			else if(packageBag.isActive())
				result.put("warning", MessageCodes.WARNING_PACKAGE_ALREADY_ACTIVATED);
			else if(!UserService.isAuthorizedToEdit(packageBag, requester))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageBag.setActive(true);
				packageService.update(packageBag, requester);
				repositoryService.publishRepository(packageBag.getRepository(), requester);
				result.put("success", MessageCodes.SUCCESS_PACKAGE_ACTIVATED);
			}
			return result;
		}
		catch(PackageEditException | RepositoryEditException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}/deactivate", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deactivatePackage(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		Package packageBag = packageService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		try
		{
			if(packageBag == null)
				result.put("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			else if(!packageBag.isActive())
				result.put("warning", MessageCodes.WARNING_PACKAGE_ALREADY_DEACTIVATED);
			else if(!UserService.isAuthorizedToEdit(packageBag, requester))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageBag.setActive(false);
				packageService.update(packageBag, requester);
				repositoryService.publishRepository(packageBag.getRepository(), requester);
				result.put("success", MessageCodes.SUCCESS_PACKAGE_DEACTIVATED);
			}
			return result;
		}	
		catch(PackageEditException | RepositoryEditException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
		
	}
	
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deletePackage(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		Package packageBag = packageService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		try
		{
			if(packageBag == null)
				result.put("error", MessageCodes.ERROR_PACKAGE_NOT_FOUND);
			else if(!UserService.isAuthorizedToEdit(packageBag, requester))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageService.delete(id, requester);
				repositoryService.publishRepository(packageBag.getRepository(), requester);
				result.put("success", MessageCodes.SUCCESS_PACKAGE_DELETED);
			}
			return result;
		}
		catch(PackageDeleteException | RepositoryEditException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> shiftDeletePackage(@PathVariable Integer id)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			packageService.shiftDelete(id);
			result.put("success", MessageCodes.SUCCESS_PACKAGE_DELETED);
			return result;
		}
		catch(PackageDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}

}
