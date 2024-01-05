/**
 * R Depot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
package eu.openanalytics.rdepot.r.legacy.api.v1.formatters;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.service.RoleService;

@Component
public class RoleFormatter implements Formatter<Role> {
	
	@Autowired
	private RoleService roleService;
	
	@Override
	public String print(Role role, Locale arg1) 
	{
	return role.getDescription();
	}
	
	@Override
	public Role parse(String source, Locale arg1) throws ParseException
	{
		Role role = roleService.findByDescription(source).orElseThrow(() -> new ParseException(0, "Role not found"));
		return role;
	}

}
