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
package eu.openanalytics.rdepot.python.config.declarative;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclarativePythonRepository {
    protected String name;
    protected String publicationUri;
    protected String serverAddress;
    protected Boolean deleted = false;
    protected Boolean published = true;

    @JsonDeserialize(using = PythonLanguage.class)
    protected Technology technology;
}
