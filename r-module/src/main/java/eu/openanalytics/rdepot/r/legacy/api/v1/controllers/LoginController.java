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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController 
{
	@Value("${app.authentication}")
	private String mode;
	
	@RequestMapping(value={"/login"}, method=RequestMethod.GET)
	public String login()
	{
		return "login";
	}
	
	@RequestMapping(value={"/loginfailed"}, method=RequestMethod.GET)
	public String loginfailed(Model model) 
	{
		model.addAttribute("error", "true");
		return login();
	}
	
	@RequestMapping(value = {"/logout"}, method=RequestMethod.POST)
	public String logout(HttpServletRequest request) throws ServletException {
		request.logout();	
		if(mode.equals("keycloak")) {
			return "redirect:/manager";
		} else {
			return "redirect:/login";
		}
	}
}