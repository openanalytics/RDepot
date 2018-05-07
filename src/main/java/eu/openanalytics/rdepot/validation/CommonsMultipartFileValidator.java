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
package eu.openanalytics.rdepot.validation;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import eu.openanalytics.rdepot.exception.CommonsMultipartFileValidationException;
import eu.openanalytics.rdepot.messaging.MessageCodes;

@Component
public class CommonsMultipartFileValidator
{
	
	public void validate(CommonsMultipartFile target) throws CommonsMultipartFileValidationException
	{
		validateContentType(target);
		validateSize(target);
		validateFilename(target);
	}
	
	private void validateContentType(CommonsMultipartFile target) throws CommonsMultipartFileValidationException
	{
		if(!Objects.equals(target.getContentType(), "application/gzip"))
			throw new CommonsMultipartFileValidationException(MessageCodes.ERROR_FORM_INVALID_CONTENTTYPE);
	}
	
	private void validateSize(CommonsMultipartFile target) throws CommonsMultipartFileValidationException
	{
		if(target.getSize() <= 0)
			throw new CommonsMultipartFileValidationException(MessageCodes.ERROR_FORM_EMPTY_FILE);
	}
	
	private void validateFilename(CommonsMultipartFile target) throws CommonsMultipartFileValidationException
	{
		String[] splitUnderscore = target.getOriginalFilename().split("_");
		if(splitUnderscore.length < 2)
			throw new CommonsMultipartFileValidationException(MessageCodes.ERROR_FORM_INVALID_FILENAME);
		String name = splitUnderscore[0];
		if(name == null || name.isEmpty() || name.trim().equals(""))
			throw new CommonsMultipartFileValidationException(MessageCodes.ERROR_FORM_INVALID_FILENAME);
	}

}
