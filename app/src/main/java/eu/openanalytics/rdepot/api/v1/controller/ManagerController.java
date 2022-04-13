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
package eu.openanalytics.rdepot.api.v1.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.openanalytics.rdepot.model.MultiUploadRequest;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;

/**
 * Controller providing content for "Upload Package" section.
 */
@Controller
@PreAuthorize("hasAuthority('user')")
@RequestMapping(value = {"/manager", "/api/manager"})
public class ManagerController {
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * This method provides content for "Upload Package" site.
	 * It provides the following elements:
	 * role - numeric value representing user's privileges
	 * repositories - list of available repositories to upload package to
	 * multiUploads - form containing packages which is then send back to @see SubmissionController
	 * @param model contains values sent to the user
	 * @param principal represents the user
	 * @return string which represents manager template
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String manager(Model model, Principal principal) {	
		User user = userService.findByLogin(principal.getName());
		
		model.addAttribute("role", user.getRole().getValue());
		model.addAttribute("repositories", repositoryService.findAll());
		model.addAttribute("multiUploads", new MultiUploadRequest());
		return "manager";
	}
}
