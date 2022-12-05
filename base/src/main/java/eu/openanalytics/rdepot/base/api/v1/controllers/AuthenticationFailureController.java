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
package eu.openanalytics.rdepot.base.api.v1.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthenticationFailureController extends SimpleUrlAuthenticationFailureHandler {	
	@Resource
	private Environment environment;
	
	@Resource
	MessageSource messageSource;
	
	@RequestMapping(value = "/authfailed", method=RequestMethod.GET)
	public String authFailed(HttpServletRequest request, Model model) throws ServletException {
		if(environment.getProperty("app.authentication").equals("openid")) {
			model.addAttribute("link", environment.getProperty("app.openid.baseUrl"));
		} else {
			model.addAttribute("link", environment.getProperty("app.keycloak.baseUrl") + "/manager");
		}						
		
		return "error_auth";
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {			
		
		if(environment.getProperty("app.authentication").equals("openid")) {
			
			List<String> cookies = new ArrayList<>();
			
			for(int i=0;;i++) {
				String cookie = environment.getProperty(String.format("app.openid.delete_cookies[%d].name", i));
				if (cookie == null) break;
				else cookies.add(cookie);
			}
			
			deleteCookies(request, response, cookies);
		}
				
		response.sendRedirect("/authfailed");
	}
		
	private void deleteCookies(HttpServletRequest request, HttpServletResponse response, List<String> names) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie: cookies) {
                if (names.contains(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }
}
