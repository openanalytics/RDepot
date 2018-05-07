/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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
package eu.openanalytics.rdepot.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.UserService;

@Component
public class UserFormatter implements Formatter<User>
{

	@Autowired
	private UserService userService;
	
	@Override
	public String print(User object, Locale locale) 
	{
		return object.getName();
	}

	@Override
	public User parse(String text, Locale locale) throws ParseException
	{
		int id = Integer.parseInt(text);
		return userService.findById(id);
	}

}
