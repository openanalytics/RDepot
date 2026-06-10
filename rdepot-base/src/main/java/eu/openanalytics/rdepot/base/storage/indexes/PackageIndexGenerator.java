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
package eu.openanalytics.rdepot.base.storage.indexes;

import eu.openanalytics.rdepot.base.entities.Hashable;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.base.storage.indexes.utils.PackagePublicationURIResolver;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Entities;

@Slf4j
public abstract class PackageIndexGenerator<P extends Package> extends IndexGenerator<P, P> {
    public PackageIndexGenerator(
            String headerTemplate,
            String anchorTemplate,
            Storage<P> storage,
            PackagePublicationURIResolver<P> packagePublicationURIResolver) {
        super(headerTemplate, anchorTemplate, storage, packagePublicationURIResolver);
    }

    protected String generateHeader(P packageBag) {
        return headerTemplate
                .replace("$document_title", "Links for " + packageBag.getName())
                .replace("$title", "Links for " + packageBag.getName())
                .replace(
                        "$meta_name",
                        String.format(
                                "%s:repository-version",
                                Entities.escape(packageBag.getRepository().getName())))
                .replace(
                        "$meta_content", packageBag.getRepository().getVersion().toString());
    }

    @Override
    public String generateIndex(P packageBag, List<P> packages, String path) throws IOException {
        final String withHeaderResolved = generateHeader(packageBag);
        storage.appendText(withHeaderResolved, path);
        return addPackagesToList(packages, packageBag, path);
    }

    public String generateIndex(P packageBag, String path) throws IOException {
        return generateIndex(packageBag, List.of(), path);
    }

    @Override
    protected String calculateChecksum(Hashable item, String path) throws IOException {
        try {
            return storage.calculateMd5Sum(path);
        } catch (Md5SumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }
}
