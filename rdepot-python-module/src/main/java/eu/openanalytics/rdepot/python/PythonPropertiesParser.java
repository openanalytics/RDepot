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
package eu.openanalytics.rdepot.python;

import eu.openanalytics.rdepot.base.PropertiesParser;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PythonPropertiesParser extends PropertiesParser {

    @Serial
    private static final long serialVersionUID = 4587563926011934706L;

    public PythonPropertiesParser(File descriptionFile) throws IOException {
        super(descriptionFile);
    }

    @Override
    protected Optional<String> processValue(String value) {
        if (value == null) return Optional.empty();
        return Optional.of(stripIndent(value).stripLeading().replaceAll(" *(?:\\\\n)+ *$", ""));
    }

    private boolean isMultiLine(String value) {
        if (value == null) return false;
        return newLinePattern.matcher(value).results().count() > 1;
    }

    private String stripIndent(String value) {
        if (!isMultiLine(value)) return value;
        List<String> split = new ArrayList<>(List.of(newLinePattern.split(value)));
        String firstLine = split.remove(0);
        String rest = String.join("\n", split);
        String stripped = rest.stripIndent();
        String replaced = stripped.replaceAll("\\n", "\\\\n");
        return firstLine + "\\n" + replaced;
    }
}
