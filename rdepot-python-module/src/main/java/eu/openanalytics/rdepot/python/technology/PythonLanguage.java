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
package eu.openanalytics.rdepot.python.technology;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.DeclaredRepositoryTechnologyMismatch;
import eu.openanalytics.rdepot.base.technology.Technology;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
public class PythonLanguage extends JsonDeserializer<Technology> implements Technology {

    public static PythonLanguage instance;

    static {
        instance = new PythonLanguage();
    }

    @Override
    public Technology getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "Python";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Boolean isCompatible(String version) {
        return true;
    }

    @Override
    public Technology deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String value = deserializationContext.readValue(jsonParser, String.class);
        if (StringUtils.isBlank(value) || !value.equalsIgnoreCase(instance.getName())) {
            throw new DeclaredRepositoryTechnologyMismatch(value);
        }
        return instance;
    }
}
