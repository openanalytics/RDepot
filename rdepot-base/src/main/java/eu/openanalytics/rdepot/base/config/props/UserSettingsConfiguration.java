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
package eu.openanalytics.rdepot.base.config.props;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Defines default
 * {@link eu.openanalytics.rdepot.base.entities.UserSettings User Settings}.
 */
@Component
@Getter
@Setter
@ConfigurationProperties("default-user-configuration")
public class UserSettingsConfiguration {
    private String language = "en-US";
    private String theme = "light";
    private int pageSize = 10;
    private int pageSizeMaxLimit = 1000;
    private List<String> supportedLanguages;
    private List<String> supportedThemes;
}
