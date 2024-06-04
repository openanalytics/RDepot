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
package eu.openanalytics.rdepot.python.storage.indexes;

import eu.openanalytics.rdepot.python.entities.PythonPackage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

public abstract class IndexesGenerator {

    protected final String separator = FileSystems.getDefault().getSeparator();
    private final String lineSeparator = System.lineSeparator();

    protected abstract String getPackageAnchor(PythonPackage packageBag);

    private Path getIndexPath(String path) {
        return Paths.get(path + separator + "index.html");
    }

    public void addPackageToIndex(PythonPackage packageBag, String path) throws IOException {
        Path indexPath = getIndexPath(path);
        String packagesIndexContent = Files.readString(indexPath);
        String packageString = getPackageAnchor(packageBag) + lineSeparator;
        int index = packagesIndexContent.indexOf("</body>");
        if (!packagesIndexContent.contains(packageString)) {
            packagesIndexContent = new StringBuilder(packagesIndexContent)
                    .insert(index - 1, packageString)
                    .toString();
            Files.writeString(
                    indexPath,
                    packagesIndexContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    protected String getTemplateString(Resource indexTemplate) throws IOException {
        return StreamUtils.copyToString(indexTemplate.getInputStream(), Charset.defaultCharset());
    }

    protected void writeTemplateStringToFile(String path, String template) throws IOException {
        Files.writeString(
                getIndexPath(path),
                template,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
