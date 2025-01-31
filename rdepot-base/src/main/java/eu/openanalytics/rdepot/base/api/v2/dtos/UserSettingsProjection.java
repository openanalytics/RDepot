/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.entities.UserSettings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Simplified Data Transfer Object for {@link UserSettings User Settings}
 * to use in {@link UserDto User DTO}
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserSettingsProjection {
    private String language;
    private String theme;
    private int pageSize;

    public UserSettingsProjection(UserSettings userSettings) {
        this.language = userSettings.getLanguage();
        this.theme = userSettings.getTheme();
        this.pageSize = userSettings.getPageSize();
    }
}
