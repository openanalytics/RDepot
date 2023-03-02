/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.validation;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.service.UserService;

@Component
public class UserValidator implements Validator {

	private final UserService userService;
	
	private final Pattern emailPattern = Pattern.compile("^.+@.+$");
	
	@Autowired
	public UserValidator(UserService userService) {
		this.userService = userService;
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(User.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		User user = (User)target;
		
		if(user.getId() > 0) { //update
			Optional<User> existingUserOptional = userService.findById(user.getId());
			if(existingUserOptional.isEmpty())
				errors.rejectValue("id", RefactoredMessageCodes.ERROR_USER_NOT_FOUND);
			else {
				User existingUser = existingUserOptional.get();
				if(user.getLastLoggedInOn() != null && existingUser.getLastLoggedInOn() != null 
						&& !existingUser.getLastLoggedInOn().equals(user.getLastLoggedInOn()))
					errors.rejectValue("lastLoggedInOn", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(user.getCreatedOn() != null && !existingUser.getCreatedOn().equals(user.getCreatedOn()))
					errors.rejectValue("createdOn", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(user.getName() != null && !existingUser.getName().equals(user.getName()))
					errors.rejectValue("name", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(user.getLogin() != null && !existingUser.getLogin().equals(user.getLogin())) 
					errors.rejectValue("login", RefactoredMessageCodes.FORBIDDEN_UPDATE);
				if(user.getEmail() != null && !existingUser.getEmail().equals(user.getEmail()))
					errors.rejectValue("email", RefactoredMessageCodes.FORBIDDEN_UPDATE);
			}			
		} else {		
			if(user.getEmail() == null || user.getEmail().isBlank()) {
				errors.rejectValue("email", RefactoredMessageCodes.ERROR_EMPTY_EMAIL);
			} else {			
				if(!emailPattern.matcher(user.getEmail()).matches()) {
					errors.rejectValue("email", RefactoredMessageCodes.ERROR_INVALID_EMAIL);
				}
				else {
					Optional<User> duplicateEmail = userService.findByEmail(user.getEmail());
					if(duplicateEmail.isPresent() && duplicateEmail.get().getId() != user.getId()) {
						errors.rejectValue("email", RefactoredMessageCodes.ERROR_DUPLICATE_EMAIL);
					}
				}
			}
			if(user.getLogin() == null || user.getLogin().isBlank()) {
				errors.rejectValue("login", RefactoredMessageCodes.ERROR_EMPTY_LOGIN);
			} else {
				Optional<User> duplicateLogin = userService.findByLogin(user.getLogin());
				if(duplicateLogin.isPresent() && duplicateLogin.get().getId() != user.getId()) {
					errors.rejectValue("login", RefactoredMessageCodes.ERROR_DUPLICATE_LOGIN);
				}
			}
			if(user.getName() == null || user.getName().isBlank()) {
				errors.rejectValue("name", RefactoredMessageCodes.ERROR_EMPTY_NAME);
			}
		}
	}

}
