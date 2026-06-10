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
package eu.openanalytics.rdepot.python.storage.indexes;

import eu.openanalytics.rdepot.base.entities.Hashable;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.indexes.PackageIndexGenerator;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonLocalStorage;
import eu.openanalytics.rdepot.python.storage.indexes.resolvers.PythonPackagePublicationURIResolver;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PythonPackageIndexGenerator extends PackageIndexGenerator<PythonPackage> {

    private final PythonLocalStorage storage;

    public PythonPackageIndexGenerator(
            @Value("classpath:templates/python/package_template.html") Resource packageTemplate,
            @Value("classpath:templates/python/package_anchor_template.html") Resource packageAnchorTemplate,
            PythonLocalStorage storage)
            throws IOException {
        super(
                packageTemplate.getContentAsString(Charset.defaultCharset()),
                packageAnchorTemplate.getContentAsString(Charset.defaultCharset()),
                storage,
                new PythonPackagePublicationURIResolver());
        this.storage = storage;
    }

    @Override
    protected String generatePackageAnchor(PythonPackage packageBag) {
        final String genericAnchor = super.generatePackageAnchor(packageBag);
        return genericAnchor
                .replace(
                        "$hash_method",
                        packageBag.getRepository().getHashMethod().getValue())
                .replace("$checksum", packageBag.getHash())
                .replace(
                        "$package_requires_python",
                        Objects.requireNonNullElse(
                                packageBag.getRequiresPython(),
                                "unknown")) // Currently the database allows null in here
                .replace("$package_filename", packageBag.getPackageFilename());
    }

    @Override
    protected String getPackageListEnding() {
        return "\n</body>\n</html>\n";
    }

    @Override
    protected String calculateChecksum(Hashable item, String path) throws IOException {
        try {
            return storage.calculateChecksum(item.getHashMethod(), path);
        } catch (CheckSumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }
}
