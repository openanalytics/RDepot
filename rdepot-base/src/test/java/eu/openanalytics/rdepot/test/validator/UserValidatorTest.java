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
package eu.openanalytics.rdepot.test.validator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.validation.UserValidator;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

@ExtendWith(MockitoExtension.class)
public class UserValidatorTest {

    @Mock
    UserService userService;

    private UserValidator userValidator;

    @Test
    public void validateUser_userNotFound() throws Exception {
        userValidator = new UserValidator(userService);

        User user = UserTestFixture.GET_REGULAR_USER();
        User updatedUser = new User(user);

        DataBinder dataBinder = new DataBinder(updatedUser);
        dataBinder.setValidator(userValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        when(userService.findById(user.getId())).thenReturn((Optional.ofNullable(null)));

        userValidator.validate(updatedUser, errors);
        verify(errors, times(1)).rejectValue("id", MessageCodes.ERROR_USER_NOT_FOUND);
    }

    @Test
    public void validateUser_allForbiddenUpdates() throws Exception {
        userValidator = new UserValidator(userService);

        User user = UserTestFixture.GET_REGULAR_USER();
        User updatedUser = new User(user);
        updatedUser.setLastLoggedInOn(LocalDate.of(1999, 1, 1));
        updatedUser.setCreatedOn(LocalDate.of(1999, 1, 1));
        updatedUser.setName("Test new name");
        updatedUser.setLogin("tnm");
        updatedUser.setEmail("tnm@eu");

        DataBinder dataBinder = new DataBinder(updatedUser);
        dataBinder.setValidator(userValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        when(userService.findById(user.getId())).thenReturn((Optional.of(user)));

        userValidator.validate(updatedUser, errors);
        verify(errors, times(1)).rejectValue("lastLoggedInOn", MessageCodes.FORBIDDEN_UPDATE);
        verify(errors, times(1)).rejectValue("createdOn", MessageCodes.FORBIDDEN_UPDATE);
        verify(errors, times(1)).rejectValue("name", MessageCodes.FORBIDDEN_UPDATE);
        verify(errors, times(1)).rejectValue("login", MessageCodes.FORBIDDEN_UPDATE);
        verify(errors, times(1)).rejectValue("email", MessageCodes.FORBIDDEN_UPDATE);
    }

    @Test
    public void validateUser_EmptyEmailLoginAndName() throws Exception {
        userValidator = new UserValidator(userService);

        User user = UserTestFixture.GET_REGULAR_USER();
        User updatedUser = new User(user);
        updatedUser.setId(0);
        updatedUser.setName("");
        updatedUser.setLogin("");
        updatedUser.setEmail("");

        DataBinder dataBinder = new DataBinder(updatedUser);
        dataBinder.setValidator(userValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        userValidator.validate(updatedUser, errors);
        verify(errors, times(1)).rejectValue("email", MessageCodes.ERROR_EMPTY_EMAIL);
        verify(errors, times(1)).rejectValue("login", MessageCodes.ERROR_EMPTY_LOGIN);
        verify(errors, times(1)).rejectValue("name", MessageCodes.ERROR_EMPTY_NAME);
    }

    @Test
    public void validateUser_NullEmailLoginAndName() throws Exception {
        userValidator = new UserValidator(userService);

        User user = UserTestFixture.GET_REGULAR_USER();
        User updatedUser = new User(user);
        updatedUser.setId(0);
        updatedUser.setName(null);
        updatedUser.setLogin(null);
        updatedUser.setEmail(null);

        DataBinder dataBinder = new DataBinder(updatedUser);
        dataBinder.setValidator(userValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        userValidator.validate(updatedUser, errors);
        verify(errors, times(1)).rejectValue("email", MessageCodes.ERROR_EMPTY_EMAIL);
        verify(errors, times(1)).rejectValue("login", MessageCodes.ERROR_EMPTY_LOGIN);
        verify(errors, times(1)).rejectValue("name", MessageCodes.ERROR_EMPTY_NAME);
    }

    @Test
    public void validateUser_InvalidEmailAndDuplicateLogin() throws Exception {
        userValidator = new UserValidator(userService);

        User user = UserTestFixture.GET_REGULAR_USER();
        User updatedUser = new User(user);
        updatedUser.setId(0);
        updatedUser.setName("testuser123");
        updatedUser.setEmail("tnm");

        DataBinder dataBinder = new DataBinder(updatedUser);
        dataBinder.setValidator(userValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        when(userService.findByLogin(user.getLogin())).thenReturn(Optional.of(user));

        userValidator.validate(updatedUser, errors);
        verify(errors, times(1)).rejectValue("email", MessageCodes.ERROR_INVALID_EMAIL);
        verify(errors, times(1)).rejectValue("login", MessageCodes.ERROR_DUPLICATE_LOGIN);
    }

    @Test
    public void validateUser_DuplicateEmail() throws Exception {
        userValidator = new UserValidator(userService);

        User user = UserTestFixture.GET_REGULAR_USER();
        User updatedUser = new User(user);
        updatedUser.setId(0);
        updatedUser.setEmail("testuser@example.org");

        DataBinder dataBinder = new DataBinder(updatedUser);
        dataBinder.setValidator(userValidator);
        Errors errors = Mockito.spy(dataBinder.getBindingResult());

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        userValidator.validate(updatedUser, errors);
        verify(errors, times(1)).rejectValue("email", MessageCodes.ERROR_DUPLICATE_EMAIL);
    }
}
