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
package eu.openanalytics.rdepot.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.service.RepositoryService;

@Component
public class RepositoryFormatter implements Formatter<Repository>
{

	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public String print(Repository object, Locale locale) 
	{
		return object.getName();
	}

	@Override
	public Repository parse(String text, Locale locale) throws ParseException
	{
		return repositoryService.findByName(text);
	}

}
