/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.validation;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.UserService;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {

    private final UserService userService;

    private final Pattern emailPattern = Pattern.compile("^.+@.+$");

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(User.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        User user = (User) target;

        if (user.getId() > 0) { // update
            Optional<User> existingUserOptional = userService.findById(user.getId());
            if (existingUserOptional.isEmpty()) errors.rejectValue("id", MessageCodes.ERROR_USER_NOT_FOUND);
            else {
                User existingUser = existingUserOptional.get();
                if (user.getLastLoggedInOn() != null
                        && existingUser.getLastLoggedInOn() != null
                        && !existingUser.getLastLoggedInOn().equals(user.getLastLoggedInOn()))
                    errors.rejectValue("lastLoggedInOn", MessageCodes.FORBIDDEN_UPDATE);
                if (user.getCreatedOn() != null && !existingUser.getCreatedOn().equals(user.getCreatedOn()))
                    errors.rejectValue("createdOn", MessageCodes.FORBIDDEN_UPDATE);
                if (user.getName() != null && !existingUser.getName().equals(user.getName()))
                    errors.rejectValue("name", MessageCodes.FORBIDDEN_UPDATE);
                if (user.getLogin() != null && !existingUser.getLogin().equals(user.getLogin()))
                    errors.rejectValue("login", MessageCodes.FORBIDDEN_UPDATE);
                if (user.getEmail() != null && !existingUser.getEmail().equals(user.getEmail()))
                    errors.rejectValue("email", MessageCodes.FORBIDDEN_UPDATE);
            }
        } else {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                errors.rejectValue("email", MessageCodes.ERROR_EMPTY_EMAIL);
            } else {
                if (!emailPattern.matcher(user.getEmail()).matches()) {
                    errors.rejectValue("email", MessageCodes.ERROR_INVALID_EMAIL);
                } else {
                    Optional<User> duplicateEmail = userService.findByEmail(user.getEmail());
                    if (duplicateEmail.isPresent() && duplicateEmail.get().getId() != user.getId()) {
                        errors.rejectValue("email", MessageCodes.ERROR_DUPLICATE_EMAIL);
                    }
                }
            }
            if (user.getLogin() == null || user.getLogin().isBlank()) {
                errors.rejectValue("login", MessageCodes.ERROR_EMPTY_LOGIN);
            } else {
                Optional<User> duplicateLogin = userService.findByLogin(user.getLogin());
                if (duplicateLogin.isPresent() && duplicateLogin.get().getId() != user.getId()) {
                    errors.rejectValue("login", MessageCodes.ERROR_DUPLICATE_LOGIN);
                }
            }
            if (user.getName() == null || user.getName().isBlank()) {
                errors.rejectValue("name", MessageCodes.ERROR_EMPTY_NAME);
            }
        }
    }
}
