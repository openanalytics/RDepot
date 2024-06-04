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

import eu.openanalytics.rdepot.base.config.props.UserSettingsConfiguration;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserSettingsValidator implements Validator {

    private final UserSettingsConfiguration config;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(UserSettings.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        UserSettings userSettings = (UserSettings) target;
        if (!config.getSupportedLanguages().contains(userSettings.getLanguage()))
            errors.rejectValue("language", MessageCodes.LANGUAGE_NOT_SUPPORTED);
        if (!config.getSupportedThemes().contains(userSettings.getTheme()))
            errors.rejectValue("theme", MessageCodes.THEME_NOT_SUPPORTED);
        if (userSettings.getPageSize() < 1)
            errors.rejectValue("pageSize", MessageCodes.PAGE_SIZE_LOWER_THAN_ONE_ELEMENT);
        if (userSettings.getPageSize() > config.getPageSizeMaxLimit())
            errors.rejectValue("pageSize", MessageCodes.PAGE_SIZE_BIGGER_THAN_MAX_LIMIT);
    }
}
