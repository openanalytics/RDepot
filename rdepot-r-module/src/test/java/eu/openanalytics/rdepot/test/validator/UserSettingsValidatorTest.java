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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import eu.openanalytics.rdepot.base.config.props.UserSettingsConfiguration;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.validation.UserSettingsValidator;

@ExtendWith(MockitoExtension.class)
public class UserSettingsValidatorTest {

	@Mock
	private UserSettingsConfiguration config;
	
	private UserSettingsValidator validator;
	
	@Test
	public void validateUserSettings() {
		validator = new UserSettingsValidator(config);
		UserSettings settings = new UserSettings(false, "en-US", "light", 20);

		DataBinder dataBinder = new DataBinder(settings);
		dataBinder.setValidator(validator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		when(config.getSupportedLanguages()).thenReturn(List.of("en-US", "pl-PL"));
		when(config.getSupportedThemes()).thenReturn(List.of("dark", "light"));
		when(config.getPageSizeMaxLimit()).thenReturn(30);
		
		validator.validate(settings, errors);
		verify(errors, times(0)).rejectValue("language", MessageCodes.LANGUAGE_NOT_SUPPORTED);
		verify(errors, times(0)).rejectValue("theme", MessageCodes.THEME_NOT_SUPPORTED);
		verify(errors, times(0)).rejectValue("pageSize", MessageCodes.PAGE_SIZE_LOWER_THAN_ONE_ELEMENT);
		verify(errors, times(0)).rejectValue("pageSize", MessageCodes.PAGE_SIZE_BIGGER_THAN_MAX_LIMIT);
	}
	
	@Test
	public void validateUserSettings_incorrectLanguage_Theme_NegativePageSize() {
		validator = new UserSettingsValidator(config);
		UserSettings settings = new UserSettings(false, "de", "red", -1);

		DataBinder dataBinder = new DataBinder(settings);
		dataBinder.setValidator(validator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		when(config.getSupportedLanguages()).thenReturn(List.of("en-US", "pl-PL"));
		when(config.getSupportedThemes()).thenReturn(List.of("dark", "light"));
		when(config.getPageSizeMaxLimit()).thenReturn(30);
		
		validator.validate(settings, errors);
		verify(errors, times(1)).rejectValue("language", MessageCodes.LANGUAGE_NOT_SUPPORTED);
		verify(errors, times(1)).rejectValue("theme", MessageCodes.THEME_NOT_SUPPORTED);
		verify(errors, times(1)).rejectValue("pageSize", MessageCodes.PAGE_SIZE_LOWER_THAN_ONE_ELEMENT);
		verify(errors, times(0)).rejectValue("pageSize", MessageCodes.PAGE_SIZE_BIGGER_THAN_MAX_LIMIT);
	}
	
	@Test
	public void validateUserSettings_incorrectPageSize() {
		validator = new UserSettingsValidator(config);
		UserSettings settings = new UserSettings(false, "pl-PL", "light", 100);

		DataBinder dataBinder = new DataBinder(settings);
		dataBinder.setValidator(validator);
		Errors errors = Mockito.spy(dataBinder.getBindingResult());
		when(config.getSupportedLanguages()).thenReturn(List.of("en-US", "pl-PL"));
		when(config.getSupportedThemes()).thenReturn(List.of("dark", "light"));
		when(config.getPageSizeMaxLimit()).thenReturn(30);
		
		validator.validate(settings, errors);
		verify(errors, times(0)).rejectValue("language", MessageCodes.LANGUAGE_NOT_SUPPORTED);
		verify(errors, times(0)).rejectValue("theme", MessageCodes.THEME_NOT_SUPPORTED);
		verify(errors, times(0)).rejectValue("pageSize", MessageCodes.PAGE_SIZE_LOWER_THAN_ONE_ELEMENT);
		verify(errors, times(1)).rejectValue("pageSize", MessageCodes.PAGE_SIZE_BIGGER_THAN_MAX_LIMIT);
	}
}
