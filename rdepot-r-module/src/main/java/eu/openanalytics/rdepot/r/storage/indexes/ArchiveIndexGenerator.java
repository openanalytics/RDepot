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
package eu.openanalytics.rdepot.r.storage.indexes;

import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.indexes.RepositoryIndexGenerator;
import eu.openanalytics.rdepot.base.utils.PackageFilteringUtils;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.storage.indexes.resolvers.ArchivePackagePublicationURIResolver;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ArchiveIndexGenerator extends RepositoryIndexGenerator<RRepository, RPackage> {

    public ArchiveIndexGenerator(
            @Value("classpath:templates/r/archive_template.html") Resource archiveTemplate,
            @Value("classpath:templates/r/archive_anchor_template.html") Resource archiveAnchorTemplate,
            Storage<RPackage> storage)
            throws IOException {
        super(
                archiveTemplate.getContentAsString(Charset.defaultCharset()),
                archiveAnchorTemplate.getContentAsString(Charset.defaultCharset()),
                storage,
                new ArchivePackagePublicationURIResolver());
    }

    @Override
    protected String generatePackageAnchor(RPackage packageBag) {
        return anchorTemplate
                .replace("$package_id", String.valueOf(packageBag.getId()))
                .replace("$package_name", packageBag.getName())
                .replace("$package_publication_uri", packagePublicationURIResolver.resolvePackageUri(packageBag));
    }

    @Override
    public String generateIndex(RRepository repository, List<RPackage> packages, String path) throws IOException {
        return super.generateIndex(
                repository,
                packages.stream()
                        .filter(PackageFilteringUtils.distinctByKey(RPackage::getName))
                        .toList(),
                path);
    }
}
