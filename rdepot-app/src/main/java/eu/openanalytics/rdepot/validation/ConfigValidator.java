/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.validation;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigValidator implements ApplicationRunner {

    private final Environment env;

    public ConfigValidator(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {

        final boolean authServer = env.getProperty("oauth2.authorizationserver", Boolean.class, false);

        String authentication = env.getProperty("app.authentication");

        if (authServer && "simple".equals(authentication)) {
            throw new IllegalStateException("Invalid config: oauth2.authorizationserver=true "
                    + "cannot be used with app.authentication=simple");
        }
    }
}
