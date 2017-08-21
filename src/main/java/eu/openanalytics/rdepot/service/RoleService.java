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
package eu.openanalytics.rdepot.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.repository.RoleRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class RoleService
{	
	@Resource
	private RoleRepository roleRepository;
	
	public Role findById(int id) 
	{
		return roleRepository.findOne(id);
	}
	
	public Role findByValue(int value) 
	{
		return roleRepository.findByValue(value);
	}
	
	public Role findByName(String name) 
	{
		return roleRepository.findByName(name);
	}

	public List<Role> findAll() 
	{
		return roleRepository.findAll();
	}

	public Role findByDescription(String description) 
	{
		return roleRepository.findByDescription(description);
	}
}
