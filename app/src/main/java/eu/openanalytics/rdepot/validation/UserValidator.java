/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.UserService;

@Component
public class UserValidator implements Validator
{
	@Autowired
	private UserService userService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) 
	{
		User user = (User) target;
		//TODO: These do not seem to do their job when using PUT request
		ValidationUtils.rejectIfEmpty(errors, "name", MessageCodes.ERROR_FORM_EMPTY_NAME);
		ValidationUtils.rejectIfEmpty(errors, "login", MessageCodes.ERROR_FORM_EMPTY_LOGIN);
		ValidationUtils.rejectIfEmpty(errors, "email", MessageCodes.ERROR_FORM_EMPTY_EMAIL);
		
		//This seems to work like a charm, though
		if(user.getEmail() == null)
			errors.rejectValue("email", MessageCodes.ERROR_FORM_EMPTY_EMAIL);
		if(user.getLogin() == null)
			errors.rejectValue("login", MessageCodes.ERROR_FORM_EMPTY_LOGIN);
		if(user.getName() == null)
			errors.rejectValue("name", MessageCodes.ERROR_FORM_EMPTY_NAME);
		else {
			Pattern emailPattern = Pattern.compile("^.+@.+$");
			if(!emailPattern.matcher(user.getEmail()).matches())
				errors.rejectValue("email", MessageCodes.ERROR_FORM_INVALID_EMAIL);
			
			if(user.getId() == 0)
			{
				//ValidationUtils.rejectIfEmpty(errors, "hashedPassword", MessageCodes.ERROR_FORM_EMPTY_PASSWORD);
				User loginCheck = userService.findByLogin(user.getLogin());
				if(loginCheck != null)
					errors.rejectValue("login", MessageCodes.ERROR_FORM_DUPLICATE_LOGIN);
				User emailCheck = userService.findByEmail(user.getEmail());
				if(emailCheck != null)
					errors.rejectValue("email", MessageCodes.ERROR_FORM_DUPLICATE_EMAIL);
			}
			else
			{
				User originalUser = userService.findById(user.getId());
				if(originalUser == null)
					errors.rejectValue("id", MessageCodes.ERROR_USER_NOT_FOUND);
				else
				{
					User loginCheck = userService.findByLogin(user.getLogin());
					if(loginCheck != null && loginCheck.getId() != originalUser.getId())
						errors.rejectValue("login", MessageCodes.ERROR_FORM_DUPLICATE_LOGIN);
					User emailCheck = userService.findByEmail(user.getEmail());
					if(emailCheck != null && emailCheck.getId() != originalUser.getId())
						errors.rejectValue("email", MessageCodes.ERROR_FORM_DUPLICATE_EMAIL);
				}
			}
		}
	}
}
