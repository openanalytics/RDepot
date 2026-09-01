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

import eu.openanalytics.rdepot.base.entities.HavingHashMethod;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.indexes.PackageIndexGenerator;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonFSLocalStorage;
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

    private final PythonFSLocalStorage storage;

    public PythonPackageIndexGenerator(
            @Value("classpath:templates/python/package_template.html") Resource packageTemplate,
            @Value("classpath:templates/python/package_anchor_template.html") Resource packageAnchorTemplate,
            PythonFSLocalStorage storage)
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
        String packageUri = packagePublicationURIResolver.resolvePackageUri(packageBag);

        String archiveUri = packageUri.substring(0, packageUri.lastIndexOf('/')) + packageBag.getName();
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
                .replace("$package_filename", packageBag.getPackageFilename())
                .replace("$package_maintainer", Objects.toString(packageBag.getMaintainer(), "N/A"))
                .replace("$package_archive_uri", archiveUri);
    }

    @Override
    protected String calculateChecksum(HavingHashMethod item, String path) throws CheckSumCalculationException {
        return storage.calculateChecksum(item.getHashMethod(), path);
    }
}
